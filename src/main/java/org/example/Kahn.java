package org.example;

import java.util.ArrayDeque;
import java.util.Queue;

public class Kahn {
    public static class Result {
        public final boolean hasCycle;
        public final int[] order;

        public Result(boolean hasCycle, int[] order) {
            this.hasCycle = hasCycle;
            this.order = order;
        }
    }

    public static Result topologicalSort(Graph g, Metrics metrics) {
        int n = g.getN();
        int[] inDegree = new int[n];
        for (Graph.Edge e : g.getEdges()) {
            inDegree[e.v]++;
        }

        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.add(i);
            }
        }

        int[] order = new int[n];
        int idx = 0;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            order[idx++] = u;

            for (Graph.Edge e : g.getNeighbors(u)) {
                if (metrics != null) {
                    metrics.addEdgeScan();
                }
                inDegree[e.v]--;
                if (inDegree[e.v] == 0) {
                    queue.add(e.v);
                }
            }
        }

        if (idx < n) {
            int[] partial = new int[idx];
            System.arraycopy(order, 0, partial, 0, idx);
            return new Result(true, partial);
        }

        return new Result(false, order);
    }

    public static boolean isValidOrder(Graph g, int[] order) {
        int n = g.getN();
        if (order == null || order.length != n) {
            return false;
        }
        int[] pos = new int[n];
        boolean[] seen = new boolean[n];
        for (int i = 0; i < n; i++) {
            int u = order[i];
            if (u < 0 || u >= n || seen[u]) {
                return false;
            }
            seen[u] = true;
            pos[u] = i;
        }
        for (Graph.Edge e : g.getEdges()) {
            if (pos[e.u] >= pos[e.v]) {
                return false;
            }
        }
        return true;
    }
}