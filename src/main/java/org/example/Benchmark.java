package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Benchmark {
    private static final int[] SIZES = {1000, 3162, 10000, 31623, 100000};
    private static final int[] DENSITIES = {4, 20};
    private static final int RUNS = 5;

    public static void main(String[] args) throws IOException {
        File dir = new File("results");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        System.out.printf("%-18s | %-16s | %-10s | %-20s%n", "Graph (n/m)", "MST total weight", "SCC count", "Sum of DAG distances");
        System.out.println("-------------------------------------------------------------------------");

        try (FileWriter writer = new FileWriter("results/results.csv")) {
            writer.write("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result\n");

            for (int n : SIZES) {
                for (int factor : DENSITIES) {
                    int m = n * factor;
                    String densityName = (factor == 4) ? "sparse" : "dense";

                    Graph undirected = generateUndirected(n, m);
                    Graph directed = generateDirected(n, m);
                    Graph dag = generateDag(n, m);

                    RunOutcome kruskalOut = measure(() -> {
                        Metrics met = new Metrics();
                        Kruskal.Result res = Kruskal.mst(undirected, met);
                        return new TaskResult(met, res.totalWeight);
                    });
                    writeRow(writer, "Kruskal", densityName, n, m, kruskalOut);

                    RunOutcome primOut = measure(() -> {
                        Metrics met = new Metrics();
                        Kruskal.Result res = Prim.mst(undirected, met);
                        return new TaskResult(met, res.totalWeight);
                    });
                    writeRow(writer, "Prim", densityName, n, m, primOut);

                    if (kruskalOut.result != primOut.result) {
                        throw new IllegalStateException("MST weight mismatch: Kruskal=" + kruskalOut.result + ", Prim=" + primOut.result);
                    }

                    RunOutcome kosarajuOut = measure(() -> {
                        Metrics met = new Metrics();
                        Kosaraju.Result res = Kosaraju.scc(directed, met);
                        return new TaskResult(met, res.count);
                    });
                    writeRow(writer, "Kosaraju", densityName, n, m, kosarajuOut);

                    RunOutcome kahnOut = measure(() -> {
                        Metrics met = new Metrics();
                        Kahn.Result res = Kahn.topologicalSort(dag, met);
                        if (res.hasCycle || !Kahn.isValidOrder(dag, res.order)) {
                            throw new IllegalStateException("Invalid topological order on DAG");
                        }
                        return new TaskResult(met, res.order.length);
                    });
                    writeRow(writer, "Kahn", densityName, n, m, kahnOut);

                    RunOutcome dagSpOut = measure(() -> {
                        Metrics met = new Metrics();
                        DagShortestPath.Result res = DagShortestPath.shortestPaths(dag, met);
                        return new TaskResult(met, res.sumOfDistances());
                    });
                    writeRow(writer, "DagShortestPath", densityName, n, m, dagSpOut);

                    System.out.printf("%-18s | %-16d | %-10d | %-20d%n",
                            n + "/" + m, kruskalOut.result, kosarajuOut.result, dagSpOut.result);
                }
            }
        }
    }

    public static Graph generateUndirected(int n, int m) {
        Random rnd = new Random(42);
        Graph g = new Graph(n, false);
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) {
            perm[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = perm[i];
            perm[i] = perm[j];
            perm[j] = tmp;
        }
        for (int i = 1; i < n; i++) {
            int u = perm[rnd.nextInt(i)];
            int v = perm[i];
            int w = 1 + rnd.nextInt(1000);
            g.addEdge(u, v, w);
        }
        for (int i = n - 1; i < m; i++) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);
            while (b == a) {
                b = rnd.nextInt(n);
            }
            int w = 1 + rnd.nextInt(1000);
            g.addEdge(a, b, w);
        }
        return g;
    }

    public static Graph generateDirected(int n, int m) {
        Random rnd = new Random(42);
        Graph g = new Graph(n, true);
        for (int i = 0; i < m; i++) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);
            while (b == a) {
                b = rnd.nextInt(n);
            }
            int w = 1 + rnd.nextInt(1000);
            g.addEdge(a, b, w);
        }
        return g;
    }

    public static Graph generateDag(int n, int m) {
        Random rnd = new Random(42);
        Graph g = new Graph(n, true);
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = rank[i];
            rank[i] = rank[j];
            rank[j] = tmp;
        }
        for (int i = 0; i < m; i++) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);
            while (b == a) {
                b = rnd.nextInt(n);
            }
            if (rank[a] > rank[b]) {
                int tmp = a;
                a = b;
                b = tmp;
            }
            int w = 1 + rnd.nextInt(1000);
            g.addEdge(a, b, w);
        }
        return g;
    }

    private interface Task {
        TaskResult run();
    }

    private static class TaskResult {
        final Metrics metrics;
        final long result;

        TaskResult(Metrics metrics, long result) {
            this.metrics = metrics;
            this.result = result;
        }
    }

    private static class RunOutcome {
        final double timeMs;
        final Metrics metrics;
        final long result;

        RunOutcome(double timeMs, Metrics metrics, long result) {
            this.timeMs = timeMs;
            this.metrics = metrics;
            this.result = result;
        }
    }

    private static RunOutcome measure(Task task) {
        RunOutcome[] outcomes = new RunOutcome[RUNS];
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            TaskResult tr = task.run();
            long end = System.nanoTime();
            outcomes[i] = new RunOutcome((end - start) / 1_000_000.0, tr.metrics, tr.result);
        }
        Arrays.sort(outcomes, (a, b) -> Double.compare(a.timeMs, b.timeMs));
        return outcomes[RUNS / 2];
    }

    private static void writeRow(FileWriter writer, String algo, String graph, int n, int m, RunOutcome out) throws IOException {
        Metrics met = out.metrics;
        writer.write(algo + "," + graph + "," + n + "," + m + "," +
                out.timeMs + "," + met.getDsuOps() + "," + met.getEdgeScans() + "," +
                met.getComparisons() + "," + met.getMaxDepth() + "," + out.result + "\n");
    }
}