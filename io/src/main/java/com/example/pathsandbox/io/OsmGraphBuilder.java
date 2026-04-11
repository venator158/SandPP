package com.example.pathsandbox.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.pathsandbox.core.AdjacencyGraph;
import com.example.pathsandbox.core.Coordinate;

public class OsmGraphBuilder {

    public static AdjacencyGraph build(OsmParser.OsmGraph osmGraph) {
        Map<Coordinate, List<AdjacencyGraph.Edge>> adjList = new HashMap<>();

        Map<Long, Coordinate> mappedCoords = new HashMap<>();

        // Map OSM nodes to polymorphic Coordinates
        for (OsmParser.OsmNode osmNode : osmGraph.getNodes().values()) {
            Coordinate c = new Coordinate(String.valueOf(osmNode.getId()), osmNode.getLon(), osmNode.getLat());
            mappedCoords.put(osmNode.getId(), c);
        }

        // Generate the weighted graph edges using geospatial proximity logic
        for (Map.Entry<Long, List<Long>> entry : osmGraph.getAdjacencyList().entrySet()) {
            Coordinate source = mappedCoords.get(entry.getKey());
            if (source == null) continue;

            List<AdjacencyGraph.Edge> edges = new java.util.ArrayList<>();
            for (Long neighborId : entry.getValue()) {
                Coordinate target = mappedCoords.get(neighborId);
                if (target != null) {
                    double dist = source.calculateDistance(target);
                    // Standard Euclidean distance is acceptable for very local scaled graphs 
                    // To accurately reflect km/miles for geo maps, use Haversine conversions here
                    
                    edges.add(new AdjacencyGraph.Edge(target, dist));
                }
            }
            adjList.put(source, edges);
        }

        return new AdjacencyGraph(adjList);
    }
}
