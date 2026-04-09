package com.example.pathsandbox.core;

import java.util.Collections;
import java.util.List;

public class PathResult {
    private final List<Coordinate> path;
    private final double actualDistance;
    // Euclidean distance / actual path distance ratio
    private final double distanceRatio; 

    public PathResult(List<Coordinate> path, double actualDistance, double distanceRatio) {
        this.path = Collections.unmodifiableList(path);
        this.actualDistance = actualDistance;
        this.distanceRatio = distanceRatio;
    }

    public List<Coordinate> getPath() {
        return path;
    }

    public double getActualDistance() {
        return actualDistance;
    }

    public double getDistanceRatio() {
        return distanceRatio;
    }

    public int getLength() {
        return path.size();
    }
}
