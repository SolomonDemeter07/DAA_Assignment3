# Assignment 3 — Graphs: DSU, MST, SCC and Shortest Paths

## Project Overview
This project implements a custom graph library and core network/scheduling algorithms in Java:
- **Graph Core:** Unified adjacency-list and edge-list representation supporting both directed and undirected weighted graphs, file input (`data/`), and graph transposition.
- **Disjoint Set Union (`DSU`):** Union by rank and path compression with operation counters.
- **Minimum Spanning Tree (`Kruskal`, `Prim`):** Cross-checked MST/forest algorithms handling disconnected graphs.
- **Strongly Connected Components & Topological Sort (`Kosaraju`, `Kahn`):** Stack-safe iterative DFS surviving 100,000 vertices and cycle-detecting topological sorting.
- **Shortest Paths in DAGs (`DagShortestPath`):** Single-pass Θ(V + E) topological relaxation supporting negative weights and path restoration.

## How to Build the Project
Run in terminal:
mvn clean test-compile

Or in IntelliJ IDEA: **Build -> Build Project** (`Ctrl + F9`).

## How to Run the Tests
Run in terminal:
mvn test

Or open `src/test/java/org/example/AlgorithmTest.java` in IntelliJ IDEA and click the green **Run** icon.

## How to Run the Benchmark & Generate Plots
1. Run `src/main/java/org/example/Benchmark.java` — verifies reference values, cross-checks algorithms, prints the reference table, and outputs `results/results.csv`.
2. Run `src/main/java/org/example/PlotGenerator.java` — reads `results/results.csv` and generates the three PNG charts in `results/plots/`.

 