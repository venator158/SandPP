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
            System.err.println("Start and goal must be in the format x,y");
            System.exit(2);
        }

        int startX = Integer.parseInt(startParts[0].trim());
        int startY = Integer.parseInt(startParts[1].trim());
        int goalX = Integer.parseInt(goalParts[0].trim());
        int goalY = Integer.parseInt(goalParts[1].trim());

        Coordinate start = new Coordinate(startX, startY);
        Coordinate goal = new Coordinate(goalX, goalY);

        boolean allowDiagonals = Boolean.parseBoolean(allowDiagonalsStr);

        Path outDir = Path.of(output);
        Files.createDirectories(outDir);

        System.out.println("Reading grid from " + input);
        Grid grid = GridReader.read(Path.of(input));
        
        System.out.println("Building graph...");
        BasicGridGraphBuilder graphBuilder = new BasicGridGraphBuilder(allowDiagonals);
        AdjacencyGraph graph = graphBuilder.build(grid);

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
        Renderer.renderToHtml(grid, result, outDir.resolve("index.html"));
        System.out.println("Wrote path visualization to " + outDir.resolve("index.html").toAbsolutePath());

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
