package com.example.pathsandbox.core;

import java.util.Objects;

public class Coordinate {
    private final double x;
    private final double y;
    private final String id;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
        this.id = x + "," + y;
    }

    public Coordinate(String id, double x, double y) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getId() { return id; }

    public double calculateDistance(Coordinate other) {
        // Fast Euclidean distance - suitable for Grid and flat cartesian coordinates
        // For accurate Lat/Lon Earth spherical distance, Haversine would be used here dynamically.
        return Math.sqrt(Math.pow(other.x - this.x, 2) + Math.pow(other.y - this.y, 2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
