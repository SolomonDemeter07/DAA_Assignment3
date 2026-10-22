package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DagShortestPath {
    public static class Result {
        public final long[] dist;
        public final int[] parent;

        public Result(long[] dist, int[] parent) {
            this.dist = dist;
            this.parent = parent;
        }

        public long sumOfDistances() {
            long sum = 0;
            for (long d : dist) {
                if (d != Long.MAX_VALUE) {
                    sum += d;
                }
            }
            return sum;
        }

        public List<Integer> restorePath(int target) {
            if (target < 0 || target >= dist.length || dist[target] == Long.MAX_VALUE) {
                return Collections.emptyList();
            }
            List<Integer> path = new ArrayList<>();
            for (int curr = target; curr != -1; curr = parent[curr]) {
                path.add(curr);
            }
            Collections.reverse(path);
            return path;
        }
    }

    public static Result shortestPaths(Graph g, Metrics metrics) {
        Kahn.Result topo = Kahn.topologicalSort(g, null);
        if (topo.hasCycle) {
            throw new IllegalArgumentException("Graph is not a DAG");
        }

        int n = g.getN();
        int[] inDegree = new int[n];
        for (Graph.Edge e : g.getEdges()) {
            inDegree[e.v]++;
        }

        long[] dist = new long[n];
        int[] parent = new int[n];
        Arrays.fill(dist, Long.MAX_VALUE);
        Arrays.fill(parent, -1);

        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                dist[i] = 0;
            }
        }

        for (int u : topo.order) {
            if (dist[u] == Long.MAX_VALUE) {
                continue;
            }
            for (Graph.Edge e : g.getNeighbors(u)) {
                if (metrics != null) {
                    metrics.addEdgeScan();
                    metrics.addComparison();
                }
                if (dist[u] + e.w < dist[e.v]) {
                    dist[e.v] = dist[u] + e.w;
                    parent[e.v] = u;
                }
            }
        }

        return new Result(dist, parent);
    }
}