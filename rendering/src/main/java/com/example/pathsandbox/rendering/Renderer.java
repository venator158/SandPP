package com.example.pathsandbox.rendering;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.example.pathsandbox.core.PathResult;
import com.example.pathsandbox.core.Grid;

public class Renderer {
    private static final Gson GSON = new Gson();

    public static void renderToPathJson(PathResult result, Path outputPath) throws IOException {
        try (FileWriter fw = new FileWriter(outputPath.toFile())) {
            GSON.toJson(result.getPath(), fw);
        }
    }

    public static void renderToHtml(Grid grid, PathResult result, Path outputPath) throws IOException {
        String gridJson = GSON.toJson(grid);
        String pathJson = GSON.toJson(result.getPath());

        String html = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <title>PathRenderer</title>\n" +
            "    <style>\n" +
            "        body { font-family: sans-serif; display: flex; flex-direction: column; align-items: center; background: #f0f0f0; margin-top: 20px; }\n" +
            "        #grid { display: grid; gap: 2px; background: #333; padding: 2px; border: 2px solid #222; }\n" +
            "        .cell { width: 40px; height: 40px; background: white; display: flex; justify-content: center; align-items: center; font-size: 24px; }\n" +
            "        .obstacle { background: #555; }\n" +
            "        .path { background: #b3e5fc; }\n" +
            "        .start { background: #c8e6c9; }\n" +
            "        .goal { background: #ffcdd2; }\n" +
            "        #controls { margin-top: 20px; }\n" +
            "        button { padding: 10px 20px; font-size: 16px; cursor: pointer; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <h2>Path Animation (🤖 -> 🏁)</h2>\n" +
            "    <div id=\"grid\"></div>\n" +
            "    <div id=\"controls\">\n" +
            "        <button onclick=\"startAnimation()\">Play</button>\n" +
            "        <button onclick=\"resetAnimation()\">Reset</button>\n" +
            "    </div>\n" +
            "    <script>\n" +
            "        const gridData = " + gridJson + ";\n" +
            "        const pathData = " + pathJson + ";\n" +
            "        const gridEl = document.getElementById('grid');\n" +
            "        let animInterval = null;\n" +
            "\n" +
            "        // Render Grid initial state\n" +
            "        function renderGrid() {\n" +
            "            gridEl.innerHTML = '';\n" +
            "            gridEl.style.gridTemplateColumns = `repeat(${gridData.width}, 40px)`;\n" +
            "            for (let y = 0; y < gridData.height; y++) {\n" +
            "                for (let x = 0; x < gridData.width; x++) {\n" +
            "                    const cell = document.createElement('div');\n" +
            "                    cell.className = 'cell';\n" +
            "                    cell.id = `cell-${x}-${y}`;\n" +
            "                    if (gridData.occupancy[y * gridData.width + x] === 1) {\n" +
            "                        cell.classList.add('obstacle');\n" +
            "                    }\n" +
            "                    gridEl.appendChild(cell);\n" +
            "                }\n" +
            "            }\n" +
            "\n" +
            "            if (pathData.length > 0) {\n" +
            "               document.getElementById(`cell-${pathData[0].x}-${pathData[0].y}`).classList.add('start');\n" +
            "               const goalCell = document.getElementById(`cell-${pathData[pathData.length-1].x}-${pathData[pathData.length-1].y}`);\n" +
            "               goalCell.classList.add('goal');\n" +
            "               goalCell.textContent = '🏁';\n" +
            "               \n" +
            "               // Place avatar at start\n" +
            "               document.getElementById(`cell-${pathData[0].x}-${pathData[0].y}`).textContent = '🤖';\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        function startAnimation() {\n" +
            "            resetAnimation();\n" +
            "            if (pathData.length === 0) return;\n" +
            "            \n" +
            "            let step = 0;\n" +
            "            animInterval = setInterval(() => {\n" +
            "                // Remove avatar from previous\n" +
            "                if (step > 0) {\n" +
            "                    const prev = pathData[step - 1];\n" +
            "                    const prevCell = document.getElementById(`cell-${prev.x}-${prev.y}`);\n" +
            "                    prevCell.textContent = prevCell.classList.contains('goal') ? '🏁' : '';\n" +
            "                    prevCell.classList.add('path');\n" +
            "                }\n" +
            "                \n" +
            "                // Place avatar at current step\n" +
            "                const curr = pathData[step];\n" +
            "                document.getElementById(`cell-${curr.x}-${curr.y}`).textContent = '🤖';\n" +
            "                \n" +
            "                step++;\n" +
            "                if (step >= pathData.length) {\n" +
            "                    clearInterval(animInterval);\n" +
            "                }\n" +
            "            }, 200);\n" +
            "        }\n" +
            "\n" +
            "        function resetAnimation() {\n" +
            "            if (animInterval) clearInterval(animInterval);\n" +
            "            renderGrid();\n" +
            "        }\n" +
            "\n" +
            "        renderGrid();\n" +
            "    </script>\n" +
            "</body>\n" +
            "</html>";

        try (FileWriter fw = new FileWriter(outputPath.toFile())) {
            fw.write(html);
        }
    }
}
