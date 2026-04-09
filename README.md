# SandPP
Sandbox for testing path planning algorithms

## Project Overview

SandPP is a modular Java-based CLI application designed for testing, visualizing, and evaluating different path planning algorithms on 2D grid maps.

### Modules

The project is structured as a multi-module Maven build to cleanly separate concerns:
* **`app`**: The main executable module and command-line interface.
* **`core`**: Foundational domain models (`Grid`, `Coordinate`, `AdjacencyGraph`) and Graph builders.
* **`planners`**: Implementations of various pathfinding algorithms.
* **`io`**: Logic for parsing and reading input maps from JSON.
* **`rendering`**: Generates visual outputs, including an interactive HTML page to animate the computed paths.
* **`metrics`**: Tracks performance data like execution time, nodes visited, and distance ratios.
* **`examples`**: Sample JSON grid configurations (`grid1.json`, `grid2.json`, `grid3.json`) used for testing.

### Inputs & Outputs

**Inputs:** 
Map files are provided in JSON format, defining grid dimensions (`width`, `height`) and an `occupancy` array (0 = free space, 1 = obstacle). CLI arguments provide the start and goal coordinates.

**Outputs:** 
Upon successful execution, the tool generates three files in the specified output directory:
* `path.json`: The raw array of coordinate steps.
* `metrics.json`: Performance stats (execution time, actual path distance, nodes expanded, success status).
* `index.html`: A self-contained, interactive web page that visually animates an avatar traversing the solved path across the grid.

### Supported Planners
* `dijkstra`: Finds the shortest path using Dijkstra's algorithm.
* `random`: A naive random walk traversal simulator (useful as a baseline or for testing threshold limits and failure states).

Run Locally
-----------

Compile and package the application using Maven:

```bash
mvn clean package
```

### Direct Java Execution

You can run the application directly using the packaged JAR. Here are a few examples:

Run **Dijkstra** on the 5x5 grid (Grid 1):
```bash
java -jar app/target/app.jar --input examples/grid1.json --start 0,0 --goal 4,4 --planner dijkstra --output output
```

Run **Dijkstra** with 8-way diagonal movement on the 10x10 maze (Grid 2):
```bash
java -jar app/target/app.jar --input examples/grid2.json --start 0,0 --goal 9,9 --planner dijkstra --diagonals true --output output
```

Run **Random Traversal** on the 15x15 maze (Grid 3):
```bash
java -jar app/target/app.jar --input examples/grid3.json --start 0,0 --goal 14,14 --planner random --output output
```

Optional flags:
* `--diagonals true` enables 8-way movement (default is 4-way `false`).
* `--planner <name>` allows selecting different planners (e.g. `dijkstra`, `random`).

Docker
------

Build the image:

```bash
docker build -t pathsandbox:latest .
```

Run an example (mounts `./examples` to read the grid, and `./output` to get the generated `index.html`):

```bash
docker run --rm -v $(pwd)/examples:/data -v $(pwd)/output:/out pathsandbox:latest --input /data/grid1.json --start 0,0 --goal 4,4 --planner dijkstra --output /out
```

CI
--

A GitHub Actions workflow is provided at `.github/workflows/ci.yml` which builds the project, builds the Docker image, runs a sample scenario and verifies output.

