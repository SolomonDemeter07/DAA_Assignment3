package org.example;

import java.util.ArrayList;
import java.util.List;

public class Kruskal {
    public static class Result {
        public final long totalWeight;
        public final List<Graph.Edge> edges;
        public final int components;

        public Result(long totalWeight, List<Graph.Edge> edges, int components) {
            this.totalWeight = totalWeight;
            this.edges = edges;
            this.components = components;
        }
    }

    public static Result mst(Graph g, Metrics metrics) {
        int n = g.getN();
        List<Graph.Edge> sortedEdges = new ArrayList<>(g.getEdges());
        sortedEdges.sort((a, b) -> {
            if (metrics != null) {
                metrics.addComparison();
            }
            return Integer.compare(a.w, b.w);
        });

        DSU dsu = new DSU(n);
        List<Graph.Edge> mstEdges = new ArrayList<>();
        long totalWeight = 0;

        for (Graph.Edge e : sortedEdges) {
            if (dsu.components() == 1) {
                break;
            }
            if (metrics != null) {
                metrics.addEdgeScan();
            }
            if (dsu.find(e.u, metrics) != dsu.find(e.v, metrics)) {
                dsu.union(e.u, e.v, metrics);
                mstEdges.add(e);
                totalWeight += e.w;
            }
        }

        return new Result(totalWeight, mstEdges, dsu.components());
    }
}