package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Prim {
    public static Kruskal.Result mst(Graph g, Metrics metrics) {
        int n = g.getN();
        boolean[] inTree = new boolean[n];
        List<Graph.Edge> mstEdges = new ArrayList<>();
        long totalWeight = 0;
        int components = 0;

        PriorityQueue<Graph.Edge> pq = new PriorityQueue<>((a, b) -> {
            if (metrics != null) {
                metrics.addComparison();
            }
            return Integer.compare(a.w, b.w);
        });

        for (int start = 0; start < n; start++) {
            if (inTree[start]) {
                continue;
            }
            components++;
            inTree[start] = true;

            for (Graph.Edge e : g.getNeighbors(start)) {
                if (metrics != null) {
                    metrics.addEdgeScan();
                }
                if (!inTree[e.v]) {
                    pq.add(e);
                }
            }

            while (!pq.isEmpty()) {
                Graph.Edge current = pq.poll();
                if (inTree[current.v]) {
                    continue;
                }
                inTree[current.v] = true;
                mstEdges.add(current);
                totalWeight += current.w;

                for (Graph.Edge next : g.getNeighbors(current.v)) {
                    if (metrics != null) {
                        metrics.addEdgeScan();
                    }
                    if (!inTree[next.v]) {
                        pq.add(next);
                    }
                }
            }
        }

        return new Kruskal.Result(totalWeight, mstEdges, components);
    }
}