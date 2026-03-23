# SandPP
Sandbox for testing path planning algorithms

Docker
------

Build the image:

```bash
docker build -t pathsandbox:latest .
```

Run an example (mounts `./examples` into `/data` in the container):

```bash
docker run --rm -v $(pwd)/examples:/data pathsandbox:latest --input /data/grid1.json --planner dijkstra --output /data/out
```

CI
--

A GitHub Actions workflow is provided at `.github/workflows/ci.yml` which builds the project, builds the Docker image, runs a sample scenario and verifies output.

