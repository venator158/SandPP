package com.example.pathsandbox.planners;

import com.example.pathsandbox.core.AdjacencyGraph;
import com.example.pathsandbox.core.Coordinate;
import com.example.pathsandbox.core.PathResult;

public interface PathPlanner {
    PathResult findPath(AdjacencyGraph graph, Coordinate start, Coordinate goal);
    long getNodesExpanded(); // To retrieve metric
}
