package org.example;

public class Kosaraju {
    public static class Result {
        public final int count;
        public final int[] componentOf;

        public Result(int count, int[] componentOf) {
            this.count = count;
            this.componentOf = componentOf;
        }
    }

    public static Result scc(Graph g, Metrics metrics) {
        int n = g.getN();
        boolean[] visited = new boolean[n];
        int[] finishOrder = new int[n];
        int finishIdx = 0;

        int[] stack = new int[n];
        int[] nextEdge = new int[n];

        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }
            visited[start] = true;
            int top = 0;
            stack[top++] = start;
            if (metrics != null) {
                metrics.updateMaxDepth(top);
            }

            while (top > 0) {
                int u = stack[top - 1];
                var neighbors = g.getNeighbors(u);
                if (nextEdge[u] < neighbors.size()) {
                    Graph.Edge e = neighbors.get(nextEdge[u]++);
                    if (metrics != null) {
                        metrics.addEdgeScan();
                    }
                    int v = e.v;
                    if (!visited[v]) {
                        visited[v] = true;
                        stack[top++] = v;
                        if (metrics != null) {
                            metrics.updateMaxDepth(top);
                        }
                    }
                } else {
                    top--;
                    finishOrder[finishIdx++] = u;
                }
            }
        }

        Graph rev = g.reverse();
        boolean[] visitedRev = new boolean[n];
        int[] componentOf = new int[n];
        int[] nextEdgeRev = new int[n];
        int compCount = 0;

        for (int i = n - 1; i >= 0; i--) {
            int start = finishOrder[i];
            if (visitedRev[start]) {
                continue;
            }
            visitedRev[start] = true;
            componentOf[start] = compCount;
            int top = 0;
            stack[top++] = start;
            if (metrics != null) {
                metrics.updateMaxDepth(top);
            }

            while (top > 0) {
                int u = stack[top - 1];
                var neighbors = rev.getNeighbors(u);
                if (nextEdgeRev[u] < neighbors.size()) {
                    Graph.Edge e = neighbors.get(nextEdgeRev[u]++);
                    if (metrics != null) {
                        metrics.addEdgeScan();
                    }
                    int v = e.v;
                    if (!visitedRev[v]) {
                        visitedRev[v] = true;
                        componentOf[v] = compCount;
                        stack[top++] = v;
                        if (metrics != null) {
                            metrics.updateMaxDepth(top);
                        }
                    }
                } else {
                    top--;
                }
            }
            compCount++;
        }

        return new Result(compCount, componentOf);
    }
}