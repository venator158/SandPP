package com.example.pathsandbox.app;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import com.example.pathsandbox.core.AdjacencyGraph;
import com.example.pathsandbox.core.BasicGridGraphBuilder;
import com.example.pathsandbox.core.Coordinate;
import com.example.pathsandbox.core.Grid;
import com.example.pathsandbox.core.PathResult;
import com.example.pathsandbox.io.GridReader;
import com.example.pathsandbox.io.OsmGraphBuilder;
import com.example.pathsandbox.io.OsmParser;
import com.example.pathsandbox.planners.DijkstraPlanner;
import com.example.pathsandbox.planners.PathPlanner;
import com.example.pathsandbox.planners.RandomTraversalPlanner;
import com.example.pathsandbox.rendering.Renderer;

public class MainCli {
    public static void main(String[] args) throws Exception {
        Map<String, String> parsed = parseArgs(args);

        if (parsed.containsKey("healthcheck")) {
            System.out.println("ok");
            System.exit(0);
        }

        String input = parsed.getOrDefault("input", null);
        String plannerName = parsed.getOrDefault("planner", "dijkstra");
        String output = parsed.getOrDefault("output", null);
        String startStr = parsed.getOrDefault("start", null);
        String goalStr = parsed.getOrDefault("goal", null);
        String allowDiagonalsStr = parsed.getOrDefault("diagonals", "false");

        if (input == null || output == null || startStr == null || goalStr == null) {
            System.err.println("Usage: --input <path> --start <x,y> --goal <x,y> [--planner <name>] [--diagonals <true/false>] --output <dir>");
            System.exit(2);
        }

        String[] startParts = startStr.split(",");
        String[] goalParts = goalStr.split(",");
        if (startParts.length != 2 || goalParts.length != 2) {
            System.err.println("Start and goal must be in the format x,y or lat,lon");
            System.exit(2);
        }

        double startX = Double.parseDouble(startParts[0].trim());
        double startY = Double.parseDouble(startParts[1].trim());
        double goalX = Double.parseDouble(goalParts[0].trim());
        double goalY = Double.parseDouble(goalParts[1].trim());
        
        boolean allowDiagonals = Boolean.parseBoolean(allowDiagonalsStr);

        Path outDir = Path.of(output);
        Files.createDirectories(outDir);

        Coordinate start;
        Coordinate goal;
        AdjacencyGraph graph;
        Grid grid = null; // Remains null if OSM map

        System.out.println("Processing map from " + input);

        if (input.endsWith(".osm") || input.endsWith(".xml")) {
            // Process Geographical Model
            OsmParser.OsmGraph osmGraph = OsmParser.parse(Path.of(input));
            System.out.println("Building Map from " + osmGraph.getNodes().size() + " geo nodes...");
            graph = OsmGraphBuilder.build(osmGraph);

            OsmParser.OsmNode sNode = osmGraph.findNearestNode(startX, startY);
            OsmParser.OsmNode gNode = osmGraph.findNearestNode(goalX, goalY);

            if (sNode == null || gNode == null) {
                System.err.println("Failed to find valid nodes in the loaded OSM tile nearest to coordinate inputs");
                System.exit(1);
            }

            System.out.println("Closest start node mapped: " + sNode.getId());
            System.out.println("Closest goal node mapped: " + gNode.getId());
            
            start = new Coordinate(String.valueOf(sNode.getId()), sNode.getLon(), sNode.getLat());
            goal = new Coordinate(String.valueOf(gNode.getId()), gNode.getLon(), gNode.getLat());

        } else {
            // Process Native Integer Grid Array 
            start = new Coordinate((int)startX, (int)startY);
            goal = new Coordinate((int)goalX, (int)goalY);

            grid = GridReader.read(Path.of(input));
            System.out.println("Building Grid Graph...");
            BasicGridGraphBuilder graphBuilder = new BasicGridGraphBuilder(allowDiagonals);
            graph = graphBuilder.build(grid);
        }

        PathPlanner planner;
        if ("dijkstra".equalsIgnoreCase(plannerName)) {
            planner = new DijkstraPlanner();
        } else if ("random".equalsIgnoreCase(plannerName)) {
            planner = new RandomTraversalPlanner();
        } else {
            System.err.println("Unknown planner: " + plannerName);
            System.exit(1);
            return;
        }

        System.out.println("Finding path from " + start + " to " + goal + " using " + plannerName);
        long startTime = System.currentTimeMillis();
        PathResult result = planner.findPath(graph, start, goal);
        long execTime = System.currentTimeMillis() - startTime;

        Renderer.renderToPathJson(result, outDir.resolve("path.json"));

        if (grid != null) {
            Renderer.renderToHtml(grid, result, outDir.resolve("index.html"));
            System.out.println("Wrote JSON grid visualization to " + outDir.resolve("index.html").toAbsolutePath());
        } else {
            Renderer.renderGeoJSON(result, outDir.resolve("path.geojson"));
            System.out.println("Wrote geographic path visualization to " + outDir.resolve("path.geojson").toAbsolutePath());
            
            // Read generated JSON, remove whitespace and newlines for a compact URL
            String geoJsonData = Files.readString(outDir.resolve("path.geojson"));
            String minimalJson = geoJsonData.replaceAll("\\s+", "");
            String encoded = java.net.URLEncoder.encode(minimalJson, "UTF-8");
            
            System.out.println("View on map: http://geojson.io/#data=data:application/json," + encoded);
        }

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("status", result.getPath().isEmpty() ? "no_path" : "success");
        metrics.put("planner", plannerName);
        metrics.put("input", input);
        metrics.put("pathLength", result.getLength());
        metrics.put("actualDistance", result.getActualDistance());
        metrics.put("distanceRatio", result.getDistanceRatio());
        metrics.put("nodesExpanded", planner.getNodesExpanded());
        metrics.put("executionTimeMs", execTime);

        Gson g = new Gson();
        try (FileWriter fw = new FileWriter(outDir.resolve("metrics.json").toFile())) {
            fw.write(g.toJson(metrics));
        }

        System.out.println("Wrote metrics to " + outDir.resolve("metrics.json").toAbsolutePath());
        System.exit(0);
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if ("--healthcheck".equals(a)) {
                m.put("healthcheck", "true");
            } else if (a.startsWith("--") && i + 1 < args.length) {
                String key = a.substring(2);
                String val = args[++i];
                m.put(key, val);
            }
        }
        return m;
    }
}
