package com.example.pathsandbox.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a generic Graph constructed from a Grid, allowing pluggable traversal strategies.
 */
public class AdjacencyGraph {
    // Map from Node Coordinate to list of neighbor Coordinates
    private final Map<Coordinate, List<Edge>> adjacencyList;

    public AdjacencyGraph(Map<Coordinate, List<Edge>> adjacencyList) {
        this.adjacencyList = adjacencyList;
    }

    public List<Edge> getNeighbors(Coordinate node) {
        return adjacencyList.getOrDefault(node, Collections.emptyList());
    }

    public static class Edge {
        private final Coordinate target;
        private final double weight;

        public Edge(Coordinate target, double weight) {
            this.target = target;
            this.weight = weight;
        }

        public Coordinate getTarget() { return target; }
        public double getWeight() { return weight; }
    }
}
