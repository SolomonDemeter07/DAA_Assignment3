package org.example;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class AlgorithmTest {

    @Test
    public void testDsuAgainstReference() {
        Random rnd = new Random(42);
        int n = 500;
        DSU dsu = new DSU(n);
        int[] refComp = new int[n];
        for (int i = 0; i < n; i++) {
            refComp[i] = i;
        }
        int expectedComponents = n;
        Metrics metrics = new Metrics();

        for (int step = 0; step < 1000; step++) {
            int u = rnd.nextInt(n);
            int v = rnd.nextInt(n);
            boolean merged = dsu.union(u, v, metrics);
            int cu = refComp[u];
            int cv = refComp[v];
            if (cu != cv) {
                assertTrue(merged);
                expectedComponents--;
                for (int i = 0; i < n; i++) {
                    if (refComp[i] == cv) {
                        refComp[i] = cu;
                    }
                }
            } else {
                assertFalse(merged);
            }
            assertEquals(expectedComponents, dsu.components());
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                boolean sameDsu = dsu.find(i, null) == dsu.find(j, null);
                boolean sameRef = refComp[i] == refComp[j];
                assertEquals(sameRef, sameDsu);
            }
        }
    }

    @Test
    public void testMstAgreementOn100RandomGraphs() {
        Random rnd = new Random(123);
        for (int t = 0; t < 100; t++) {
            int n = 20 + rnd.nextInt(80);
            int m = n - 1 + rnd.nextInt(n * 3);
            Graph g = new Graph(n, false);
            for (int i = 1; i < n; i++) {
                g.addEdge(rnd.nextInt(i), i, 1 + rnd.nextInt(500));
            }
            for (int i = n - 1; i < m; i++) {
                g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(500));
            }

            Kruskal.Result kr = Kruskal.mst(g, new Metrics());
            Kruskal.Result pr = Prim.mst(g, new Metrics());

            assertEquals(1, kr.components);
            assertEquals(1, pr.components);
            assertEquals(n - 1, kr.edges.size());
            assertEquals(n - 1, pr.edges.size());
            assertEquals(kr.totalWeight, pr.totalWeight);
        }
    }

    @Test
    public void testSccAgainstBruteForceBfs() {
        Random rnd = new Random(77);
        for (int t = 0; t < 30; t++) {
            int n = 20 + rnd.nextInt(150);
            int m = rnd.nextInt(n * 3);
            Graph g = new Graph(n, true);
            for (int i = 0; i < m; i++) {
                g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1);
            }

            Kosaraju.Result scc = Kosaraju.scc(g, new Metrics());
            boolean[][] reach = new boolean[n][n];
            for (int s = 0; s < n; s++) {
                Queue<Integer> q = new ArrayDeque<>();
                q.add(s);
                reach[s][s] = true;
                while (!q.isEmpty()) {
                    int u = q.poll();
                    for (Graph.Edge e : g.getNeighbors(u)) {
                        if (!reach[s][e.v]) {
                            reach[s][e.v] = true;
                            q.add(e.v);
                        }
                    }
                }
            }

            for (int u = 0; u < n; u++) {
                for (int v = u + 1; v < n; v++) {
                    boolean mutual = reach[u][v] && reach[v][u];
                    boolean sameScc = scc.componentOf[u] == scc.componentOf[v];
                    assertEquals(mutual, sameScc);
                }
            }
        }
    }

    @Test
    public void testTopologicalSortAndCycleDetection() throws IOException {
        Graph dag = Graph.fromFile("data/small_dag.txt");
        Kahn.Result dagRes = Kahn.topologicalSort(dag, new Metrics());
        assertFalse(dagRes.hasCycle);
        assertTrue(Kahn.isValidOrder(dag, dagRes.order));

        Graph cyclic = Graph.fromFile("data/cyclic.txt");
        Kahn.Result cycRes = Kahn.topologicalSort(cyclic, new Metrics());
        assertTrue(cycRes.hasCycle);

        Graph disconnected = Graph.fromFile("data/disconnected.txt");
        Kruskal.Result mstDisc = Kruskal.mst(disconnected, new Metrics());
        assertEquals(3, mstDisc.components);
        assertEquals(Prim.mst(disconnected, new Metrics()).totalWeight, mstDisc.totalWeight);
    }

    @Test
    public void testDagShortestPathAgainstBellmanFordWithNegativeWeights() {
        Random rnd = new Random(99);
        for (int t = 0; t < 50; t++) {
            int n = 15 + rnd.nextInt(60);
            int m = n + rnd.nextInt(n * 3);
            Graph g = new Graph(n, true);
            int[] rank = new int[n];
            for (int i = 0; i < n; i++) rank[i] = i;
            for (int i = n - 1; i > 0; i--) {
                int j = rnd.nextInt(i + 1);
                int tmp = rank[i]; rank[i] = rank[j]; rank[j] = tmp;
            }
            for (int i = 0; i < m; i++) {
                int a = rnd.nextInt(n);
                int b = rnd.nextInt(n);
                if (a == b) continue;
                if (rank[a] > rank[b]) { int tmp = a; a = b; b = tmp; }
                int w = -50 + rnd.nextInt(150);
                g.addEdge(a, b, w);
            }

            DagShortestPath.Result dagRes = DagShortestPath.shortestPaths(g, new Metrics());

            int[] inDeg = new int[n];
            for (Graph.Edge e : g.getEdges()) inDeg[e.v]++;
            long[] bfDist = new long[n];
            Arrays.fill(bfDist, Long.MAX_VALUE);
            for (int i = 0; i < n; i++) {
                if (inDeg[i] == 0) bfDist[i] = 0;
            }
            for (int iter = 0; iter < n - 1; iter++) {
                for (Graph.Edge e : g.getEdges()) {
                    if (bfDist[e.u] != Long.MAX_VALUE && bfDist[e.u] + e.w < bfDist[e.v]) {
                        bfDist[e.v] = bfDist[e.u] + e.w;
                    }
                }
            }

            assertArrayEquals(bfDist, dagRes.dist);
            for (int v = 0; v < n; v++) {
                if (dagRes.dist[v] != Long.MAX_VALUE) {
                    var path = dagRes.restorePath(v);
                    assertFalse(path.isEmpty());
                    assertEquals(v, path.get(path.size() - 1));
                    assertEquals(0, inDeg[path.get(0)]);
                }
            }
        }
    }

    @Test
    public void testStackSafetyOn100kPath() {
        int n = 100000;
        Graph pathGraph = new Graph(n, true);
        for (int i = 0; i < n - 1; i++) {
            pathGraph.addEdge(i, i + 1, 1);
        }
        Metrics metrics = new Metrics();
        Kosaraju.Result res = Kosaraju.scc(pathGraph, metrics);
        assertEquals(n, res.count);
        assertEquals(n, metrics.getMaxDepth());
    }
}