package com.example.pathsandbox.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicGridGraphBuilder implements GraphBuilder {

    private final boolean allowDiagonals;

    public BasicGridGraphBuilder(boolean allowDiagonals) {
        this.allowDiagonals = allowDiagonals;
    }

    @Override
    public AdjacencyGraph build(Grid grid) {
        Map<Coordinate, List<AdjacencyGraph.Edge>> adjList = new HashMap<>();

        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                if (grid.isObstacle(x, y)) {
                    continue; // Skip processing obstacles
                }
                
                Coordinate curr = new Coordinate(x, y);
                List<AdjacencyGraph.Edge> edges = new ArrayList<>();
                
                addIfValid(grid, x - 1, y, edges, 1.0); // West
                addIfValid(grid, x + 1, y, edges, 1.0); // East
                addIfValid(grid, x, y - 1, edges, 1.0); // North
                addIfValid(grid, x, y + 1, edges, 1.0); // South

                if (allowDiagonals) {
                    addIfValid(grid, x - 1, y - 1, edges, Math.sqrt(2)); // NW
                    addIfValid(grid, x + 1, y - 1, edges, Math.sqrt(2)); // NE
                    addIfValid(grid, x - 1, y + 1, edges, Math.sqrt(2)); // SW
                    addIfValid(grid, x + 1, y + 1, edges, Math.sqrt(2)); // SE
                }
                
                adjList.put(curr, edges);
            }
        }

        return new AdjacencyGraph(adjList);
    }

    private void addIfValid(Grid grid, int x, int y, List<AdjacencyGraph.Edge> edges, double cost) {
        if (!grid.isObstacle(x, y)) {
            edges.add(new AdjacencyGraph.Edge(new Coordinate(x, y), cost));
        }
    }
}
