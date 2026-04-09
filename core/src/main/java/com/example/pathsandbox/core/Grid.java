package com.example.pathsandbox.core;

public class Grid {
    private final int width;
    private final int height;
    private final int[] occupancy;

    public Grid(int width, int height, int[] occupancy) {
        this.width = width;
        this.height = height;
        this.occupancy = occupancy;
        
        if (occupancy.length != width * height) {
            throw new IllegalArgumentException("Occupancy array size must match width * height");
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int[] getOccupancy() { return occupancy; }

    public boolean isObstacle(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return true; // Out of bounds is treated as an obstacle
        }
        return occupancy[y * width + x] == 1;
    }
}
