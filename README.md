# SandPP
Sandbox for testing path planning algorithms

## Project Overview

SandPP is a modular Java-based **Spring Boot Web Application** designed for testing, visualizing, and evaluating different path planning algorithms on 2D grid maps and Geographic (OSM) networks. The application strictly follows the **MVC (Model-View-Controller)** architecture pattern, uses an embedded **H2 Relational Database** for persistent history logging, and integrates multiple OO Design Patterns.

### Architecture (MVC Framework)
* **Model:** Handles data structures (`Grid`, `Coordinate`, `AdjacencyGraph`, and `PathResult`) and interacts with the H2 Database using the `PathExecutionLog` entity.
* **View:** Uses HTML/CSS templates served directly from the backend via **Thymeleaf**. Enables an interactive UI for submitting coordinates, displaying performance metrics, viewing past logs, and previewing generated maps.
* **Controller:** Built using Spring `@Controller` endpoints (`PathController.java`). It receives web requests, invokes the core engine via `PathService`, and pushes data payloads back to the Thymeleaf views.

### Design Patterns Used
1.  **Strategy Pattern (Behavioral):** The `PathPlanner` interface allows different pathfinding behaviors (`DijkstraPlanner` vs `RandomTraversalPlanner`) to be dynamically swapped during execution without modifying the core service logic.
2.  **Factory Pattern (Creational):** The map parsers (`OsmGraphBuilder` and `GridReader`) encapsulate the complex logic required to convert raw arbitrary JSON/XML inputs into cohesive unified `AdjacencyGraph` instances for the routing abstraction.
3.  **Adapter Pattern (Structural):** A universal `Coordinate` abstraction dynamically acts as an adapter, smoothing the logic between standard Cartesian integers (local grids) and Double precision floating Latitude/Longitude points (OSM mapping) so the core engine never knows the difference.
4.  **Singleton Pattern (Creational):** Handled intrinsically by the Spring Boot Framework. Core dependencies such as `Renderer`, `PathService`, and database repositories are injected as Singletons context-wide to share computational memory.

### Modules

The project is structured as a multi-module Maven build to cleanly separate concerns:
* **`app`**: The primary **Spring Boot Web Application**, Controllers, JPA Repositories, and Thymeleaf Views.
* **`core`**: Foundational domain models (`Grid`, `Coordinate`, `AdjacencyGraph`).
* **`planners`**: Implementations of various pathfinding algorithms.
* **`io`**: Parsing layers for JSON arrays and OSM XML representations.
* **`rendering`**: Generates visual outputs, HTML components, and GeoJSON standard links.
* **`metrics`**: Tracks performance data.
* **`examples`**: Sample grid configurations (`grid1.json`, `neighbour.osm`) used for testing.

### User Workflow & Database Persistence
Through the **Web Portal**, users can dynamically pick a map (or specify OSM), choose a Planner, and enter starting/ending locations.
Every generated map is immediately logged into the **H2 Database**, keeping an active history of:
*   Timestamp of path execution
*   Selected planner (Dijkstra vs Random)
*   Algorithm Metrics (nodes expanded, exec time, path length)
*   Direct link to the generated geographical map payload

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

### OpenStreetMap (OSM) Support
SandPP natively supports real-world `.osm` and `.xml` street maps alongside local integer grids!
* Input your map via `--input map.osm`
* The `--start` and `--goal` coordinates shift to **Latitude,Longitude** (e.g. `--start 12.975,77.670`). The application will scan millions of geographic nodes and hook to the nearest physical street intersection.
* The output automatically pivots to **GeoJSON**. Furthermore, a highly-convenient, clickable `http://geojson.io` link containing the encoded physical path will print out to your terminal when the program completes.

Run Locally
-----------

Compile and package the complete Spring Boot Web application using Maven:

```bash
mvn clean package -DskipTests
```
Clean all existing java programs 
```bash
Stop-Process -Name java -Force
```
Clean all existing java programs running on port 8080
```bash
#Get the Process ID listening on port 8080
Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess

#Stop that specific Process ID
Stop-Process -Id <PID> -Force```
```

### Direct Java Execution

You can run the web application directly using the packaged executable JAR.

Run the Server:
```bash
java -jar app/target/app.jar
```

This starts the Spring Boot Tomcat engine.

### Launching the Dashboard

Open your web browser and navigate to:
* **http://localhost:8080/** -> The primary Spring Path Planning Terminal UI.
* **http://localhost:8080/history** -> The Database History viewer for previous metrics.
* **http://localhost:8080/h2-console** -> The internal H2 relational database UI. When connecting, ensure your JDBC URL is `jdbc:h2:mem:sandppdb` and click Connect.

### Example Coordinates

In the Web GUI form fields, if you decide to load `examples/neighbour.osm`, remember that coordinates switch from Cartesian (X,Y) to Geographic (**Latitude, Longitude**):
* **Start Coordinate:** `12.975` (Latitude), `77.670` (Longitude)
* **Goal Coordinate:** `12.978` (Latitude), `77.676` (Longitude)

Docker
------

Build the image:

```bash
docker build -t pathsandbox:latest .
```

Run the Web Server Container (port binding 8080):

```bash
docker run -p 8080:8080 --rm -v $(pwd)/examples:/app/examples pathsandbox:latest
```

Then visit `http://localhost:8080/` in your browser.

CI
--

A GitHub Actions workflow is provided at `.github/workflows/ci.yml` which builds the project, builds the Docker image, runs a sample scenario and verifies output.

