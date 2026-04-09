package com.example.pathsandbox.planners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.example.pathsandbox.core.AdjacencyGraph;
import com.example.pathsandbox.core.Coordinate;
import com.example.pathsandbox.core.PathResult;

public class RandomTraversalPlanner implements PathPlanner {
    private long nodesExpanded = 0;
    // Provide a reasonable max to prevent infinite loops matching bad graphs
    private final int MAX_STEPS = 50000;

    @Override
    public PathResult findPath(AdjacencyGraph graph, Coordinate start, Coordinate goal) {
        nodesExpanded = 0;
        List<Coordinate> path = new ArrayList<>();
        Coordinate current = start;
        path.add(current);
        Random rand = new Random();
        
        double actualDistance = 0.0;

        while (!current.equals(goal) && nodesExpanded < MAX_STEPS) {
            nodesExpanded++;
            List<AdjacencyGraph.Edge> neighbors = graph.getNeighbors(current);
            
            if (neighbors.isEmpty()) {
                break; // Stuck, nowhere to go
            }
            
            // Simple random walk (can revisit old nodes easily)
            AdjacencyGraph.Edge nextEdge = neighbors.get(rand.nextInt(neighbors.size()));
            actualDistance += nextEdge.getWeight();
            current = nextEdge.getTarget();
            path.add(current);
        }

        if (!current.equals(goal)) {
            // Failed to find in max steps
            return new PathResult(Collections.emptyList(), 0.0, 0.0);
        }

        double straightD = Math.sqrt(Math.pow(goal.getX() - start.getX(), 2) + Math.pow(goal.getY() - start.getY(), 2));
        double ratio = (straightD > 0 && actualDistance > 0) ? (straightD / actualDistance) : 1.0;

        return new PathResult(path, actualDistance, ratio);
    }

    @Override
    public long getNodesExpanded() {
        return nodesExpanded;
    }
}
