package com.example.pathsandbox.planners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.example.pathsandbox.core.AdjacencyGraph;
import com.example.pathsandbox.core.Coordinate;
import com.example.pathsandbox.core.PathResult;

public class DijkstraPlanner implements PathPlanner {

    private long nodesExpanded = 0;

    @Override
    public PathResult findPath(AdjacencyGraph graph, Coordinate start, Coordinate goal) {
        nodesExpanded = 0;
        
        Map<Coordinate, Double> dist = new HashMap<>();
        Map<Coordinate, Coordinate> prev = new HashMap<>();
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));

        dist.put(start, 0.0);
        pq.add(new NodeDistance(start, 0.0));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            Coordinate currCoord = current.coord;

            if (currCoord.equals(goal)) {
                break; // Found the goal
            }
            
            // Wait, we don't want to recount nodes if expanded again, but standard Dijkstra
            // pops each reachable node. We'll count every poll as an expansion.
            nodesExpanded++;

            if (current.distance > dist.getOrDefault(currCoord, Double.MAX_VALUE)) {
                continue; // Already found a shorter path to this node
            }

            for (AdjacencyGraph.Edge edge : graph.getNeighbors(currCoord)) {
                Coordinate neighbor = edge.getTarget();
                double altDist = current.distance + edge.getWeight();

                if (altDist < dist.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    dist.put(neighbor, altDist);
                    prev.put(neighbor, currCoord);
                    pq.add(new NodeDistance(neighbor, altDist));
                }
            }
        }

        if (!prev.containsKey(goal) && !start.equals(goal)) {
            // No path found
            return new PathResult(Collections.emptyList(), 0.0, 0.0);
        }

        // Reconstruct path
        List<Coordinate> path = new ArrayList<>();
        Coordinate current = goal;
        while (current != null) {
            path.add(current);
            current = prev.get(current);
        }
        Collections.reverse(path);
        
        double actualDistance = dist.getOrDefault(goal, 0.0);
        
        // Calculate abstract straight line distance (for ratio metric)
        double straightD = start.calculateDistance(goal);
        double ratio = (straightD > 0 && actualDistance > 0) ? (straightD / actualDistance) : 1.0;

        return new PathResult(path, actualDistance, ratio);
    }

    @Override
    public long getNodesExpanded() {
        return nodesExpanded;
    }

    private static class NodeDistance {
        Coordinate coord;
        double distance;

        NodeDistance(Coordinate coord, double distance) {
            this.coord = coord;
            this.distance = distance;
        }
    }
}
