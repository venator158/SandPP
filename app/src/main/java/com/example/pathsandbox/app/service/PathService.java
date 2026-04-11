package com.example.pathsandbox.app.service;

import com.example.pathsandbox.app.model.PathExecutionLog;
import com.example.pathsandbox.app.repository.PathExecutionLogRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PathService {

    @Autowired
    private PathExecutionLogRepository logRepository;

    public PathExecutionLog solvePath(String input, String startStr, String goalStr, String plannerName, boolean allowDiagonals) throws Exception {
        String[] startParts = startStr.split(",");
        String[] goalParts = goalStr.split(",");
        if (startParts.length != 2 || goalParts.length != 2) {
            throw new IllegalArgumentException("Start and goal must be in the format x,y or lat,lon");
        }

        double startX = Double.parseDouble(startParts[0].trim());
        double startY = Double.parseDouble(startParts[1].trim());
        double goalX = Double.parseDouble(goalParts[0].trim());
        double goalY = Double.parseDouble(goalParts[1].trim());

        Coordinate start;
        Coordinate goal;
        AdjacencyGraph graph;
        Grid grid = null;

        if (input.endsWith(".osm") || input.endsWith(".xml")) {
            OsmParser.OsmGraph osmGraph = OsmParser.parse(Path.of(input));
            graph = OsmGraphBuilder.build(osmGraph);

            OsmParser.OsmNode sNode = osmGraph.findNearestNode(startX, startY);
            OsmParser.OsmNode gNode = osmGraph.findNearestNode(goalX, goalY);

            if (sNode == null || gNode == null) {
                throw new IllegalStateException("Failed to find valid nodes in the loaded OSM map");
            }
            start = new Coordinate(String.valueOf(sNode.getId()), sNode.getLon(), sNode.getLat());
            goal = new Coordinate(String.valueOf(gNode.getId()), gNode.getLon(), gNode.getLat());
        } else {
            start = new Coordinate((int)startX, (int)startY);
            goal = new Coordinate((int)goalX, (int)goalY);

            grid = GridReader.read(Path.of(input));
            BasicGridGraphBuilder graphBuilder = new BasicGridGraphBuilder(allowDiagonals);
            graph = graphBuilder.build(grid);
        }

        PathPlanner planner;
        if ("random".equalsIgnoreCase(plannerName)) {
            planner = new RandomTraversalPlanner();
        } else {
            planner = new DijkstraPlanner();
        }

        long startTime = System.currentTimeMillis();
        PathResult result = planner.findPath(graph, start, goal);
        long execTime = System.currentTimeMillis() - startTime;

        Path outDir = Path.of("output");
        Files.createDirectories(outDir);
        Renderer.renderToPathJson(result, outDir.resolve("path.json"));

        String finalUrl = "";
        if (grid != null) {
            Renderer.renderToHtml(grid, result, outDir.resolve("index.html"));
            finalUrl = "index.html"; // We could host it from Spring, but keeping output for now
        } else {
            Renderer.renderGeoJSON(result, outDir.resolve("path.geojson"));
            String geoJsonData = Files.readString(outDir.resolve("path.geojson"));
            String minimalJson = geoJsonData.replaceAll("\\s+", "");
            finalUrl = "http://geojson.io/#data=data:application/json," + java.net.URLEncoder.encode(minimalJson, "UTF-8");
        }

        PathExecutionLog log = new PathExecutionLog();
        log.setPlannerName(plannerName);
        log.setInputMap(input);
        log.setStartCoord(startStr);
        log.setGoalCoord(goalStr);
        log.setPathLength(result.getLength());
        log.setActualDistance(result.getActualDistance());
        log.setNodesExpanded(planner.getNodesExpanded());
        log.setExecutionTimeMs(execTime);
        log.setGeneratedUrl(finalUrl);

        return logRepository.save(log);
    }
}
