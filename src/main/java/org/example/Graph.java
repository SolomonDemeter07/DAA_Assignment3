package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Graph {
    public static class Edge {
        public final int u;
        public final int v;
        public final int w;

        public Edge(int u, int v, int w) {
            this.u = u;
            this.v = v;
            this.w = w;
        }
    }

    private final int n;
    private final boolean directed;
    private final List<List<Edge>> adj;
    private final List<Edge> edges;

    public Graph(int n, boolean directed) {
        this.n = n;
        this.directed = directed;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            this.adj.add(new ArrayList<>());
        }
        this.edges = new ArrayList<>();
    }

    public void addEdge(int u, int v, int w) {
        if (u < 0 || u >= n || v < 0 || v >= n) {
            return;
        }
        Edge forward = new Edge(u, v, w);
        edges.add(forward);
        adj.get(u).add(forward);
        if (!directed && u != v) {
            adj.get(v).add(new Edge(v, u, w));
        }
    }

    public int getN() { return n; }
    public int getM() { return edges.size(); }
    public boolean isDirected() { return directed; }
    public List<Edge> getNeighbors(int u) { return adj.get(u); }
    public List<Edge> getEdges() { return edges; }

    public Graph reverse() {
        Graph rev = new Graph(n, true);
        for (Edge e : edges) {
            rev.addEdge(e.v, e.u, e.w);
        }
        return rev;
    }

    public static Graph fromFile(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            while (header != null && header.trim().isEmpty()) {
                header = br.readLine();
            }
            if (header == null) {
                throw new IllegalArgumentException("Empty graph file");
            }
            String[] parts = header.trim().split("\\s+");
            int n = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            boolean directed = parts[2].equalsIgnoreCase("directed");

            Graph g = new Graph(n, directed);
            for (int i = 0; i < m; i++) {
                String line = br.readLine();
                while (line != null && line.trim().isEmpty()) {
                    line = br.readLine();
                }
                if (line == null) break;
                String[] edgeParts = line.trim().split("\\s+");
                int u = Integer.parseInt(edgeParts[0]);
                int v = Integer.parseInt(edgeParts[1]);
                int w = Integer.parseInt(edgeParts[2]);
                g.addEdge(u, v, w);
            }
            return g;
        }
    }
}