package com.example.pathsandbox.io;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.example.pathsandbox.core.Grid;

public class GridReader {
    private static final Gson GSON = new Gson();

    public static Grid read(Path path) throws Exception {
        try (Reader reader = Files.newBufferedReader(path)) {
            GridData data = GSON.fromJson(reader, GridData.class);
            if (!"grid".equals(data.type)) {
                throw new IllegalArgumentException("Invalid grid file type: " + data.type);
            }
            return new Grid(data.width, data.height, data.occupancy);
        }
    }

    private static class GridData {
        String type;
        int width;
        int height;
        int[] occupancy;
    }
}
