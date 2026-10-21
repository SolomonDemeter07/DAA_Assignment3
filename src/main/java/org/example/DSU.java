package org.example;

public class DSU {
    private int[] parent;
    private int[] rank;
    private int components;

    public DSU(int n) {
        makeSet(n);
    }

    public void makeSet(int n) {
        parent = new int[n];
        rank = new int[n];
        components = n;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
    }

    public int find(int x, Metrics metrics) {
        if (metrics != null) {
            metrics.addFind();
        }
        int root = x;
        while (root != parent[root]) {
            root = parent[root];
        }
        int curr = x;
        while (curr != root) {
            int nxt = parent[curr];
            parent[curr] = root;
            curr = nxt;
        }
        return root;
    }

    public boolean union(int x, int y, Metrics metrics) {
        int rootX = find(x, metrics);
        int rootY = find(y, metrics);
        if (rootX == rootY) {
            return false;
        }
        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX;
        } else {
            parent[rootY] = rootX;
            rank[rootX]++;
        }
        components--;
        if (metrics != null) {
            metrics.addUnion();
        }
        return true;
    }

    public int components() {
        return components;
    }
}