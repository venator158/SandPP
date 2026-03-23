#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)
docker build -t pathsandbox:latest "$ROOT_DIR"
docker run --rm -v "$ROOT_DIR/examples":/data pathsandbox:latest "$@"
