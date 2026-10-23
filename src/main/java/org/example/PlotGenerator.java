package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class PlotGenerator {
    private static class Row {
        String algo;
        String graph;
        int n;
        int m;
        long dsuOps;
        long edgeScans;
        long comparisons;

        long totalOps() {
            return dsuOps + edgeScans + comparisons;
        }
    }

    public static void main(String[] args) throws IOException {
        File plotsDir = new File("results/plots");
        if (!plotsDir.exists()) {
            plotsDir.mkdirs();
        }

        List<Row> rows = readCsv("results/results.csv");
        drawOpsVsN(rows, "results/plots/operations_vs_n.png");
        drawScansPerEdge(rows, "results/plots/scans_per_edge.png");
        drawKruskalShare(rows, "results/plots/kruskal_share.png");
        System.out.println("Plots generated in results/plots/");
    }

    private static List<Row> readCsv(String path) throws IOException {
        List<Row> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");
                Row r = new Row();
                r.algo = p[0];
                r.graph = p[1];
                r.n = Integer.parseInt(p[2]);
                r.m = Integer.parseInt(p[3]);
                r.dsuOps = Long.parseLong(p[5]);
                r.edgeScans = Long.parseLong(p[6]);
                r.comparisons = Long.parseLong(p[7]);
                list.add(r);
            }
        }
        return list;
    }

    private static void drawOpsVsN(List<Row> rows, String outPath) throws IOException {
        int w = 900, h = 600;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = initGraphics(img, w, h, "Operations vs n (Log-Log Axes)", "log10(n)", "log10(Total Operations)");

        Map<String, List<Row>> series = new LinkedHashMap<>();
        for (Row r : rows) {
            String key = r.algo + " (" + r.graph + ")";
            series.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
        }

        double minX = 3.0, maxX = 5.0;
        double minY = 3.5, maxY = 8.0;
        drawGrid(g, w, h, minX, maxX, minY, maxY);

        Color[] palette = {
                new Color(31, 119, 180), new Color(255, 127, 14),
                new Color(44, 160, 44), new Color(214, 39, 40),
                new Color(148, 103, 189), new Color(140, 86, 75),
                new Color(227, 119, 194), new Color(127, 127, 127),
                new Color(188, 189, 34), new Color(23, 190, 207)
        };

        int idx = 0;
        for (var entry : series.entrySet()) {
            Color c = palette[idx % palette.length];
            List<Point> pts = new ArrayList<>();
            for (Row r : entry.getValue()) {
                double lx = Math.log10(r.n);
                double ly = Math.log10(Math.max(1, r.totalOps()));
                pts.add(toScreen(lx, ly, minX, maxX, minY, maxY, w, h));
            }
            drawSeries(g, pts, c, entry.getKey(), idx, w);
            idx++;
        }
        g.dispose();
        ImageIO.write(img, "png", new File(outPath));
    }

    private static void drawScansPerEdge(List<Row> rows, String outPath) throws IOException {
        int w = 900, h = 600;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = initGraphics(img, w, h, "Scans per Edge (edgeScans / m vs log10(n))", "log10(n)", "edgeScans / m");

        Map<String, List<Row>> series = new LinkedHashMap<>();
        for (Row r : rows) {
            String key = r.algo + " (" + r.graph + ")";
            series.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
        }

        double minX = 3.0, maxX = 5.0;
        double minY = 0.0, maxY = 2.5;
        drawGrid(g, w, h, minX, maxX, minY, maxY);

        Color[] palette = {
                new Color(31, 119, 180), new Color(255, 127, 14),
                new Color(44, 160, 44), new Color(214, 39, 40),
                new Color(148, 103, 189), new Color(140, 86, 75),
                new Color(227, 119, 194), new Color(127, 127, 127),
                new Color(188, 189, 34), new Color(23, 190, 207)
        };

        int idx = 0;
        for (var entry : series.entrySet()) {
            Color c = palette[idx % palette.length];
            List<Point> pts = new ArrayList<>();
            for (Row r : entry.getValue()) {
                double lx = Math.log10(r.n);
                double ratio = (double) r.edgeScans / r.m;
                pts.add(toScreen(lx, ratio, minX, maxX, minY, maxY, w, h));
            }
            drawSeries(g, pts, c, entry.getKey(), idx, w);
            idx++;
        }
        g.dispose();
        ImageIO.write(img, "png", new File(outPath));
    }

    private static void drawKruskalShare(List<Row> rows, String outPath) throws IOException {
        int w = 900, h = 600;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = initGraphics(img, w, h, "Share of Edges Kruskal Examines (edgeScans / m)", "log10(n)", "edgeScans / m");

        Map<String, List<Row>> series = new LinkedHashMap<>();
        for (Row r : rows) {
            if (r.algo.equals("Kruskal")) {
                String key = "Kruskal (" + r.graph + ")";
                series.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
            }
        }

        double minX = 3.0, maxX = 5.0;
        double minY = 0.0, maxY = 1.0;
        drawGrid(g, w, h, minX, maxX, minY, maxY);

        Color[] palette = {new Color(31, 119, 180), new Color(214, 39, 40)};
        int idx = 0;
        for (var entry : series.entrySet()) {
            Color c = palette[idx % palette.length];
            List<Point> pts = new ArrayList<>();
            for (Row r : entry.getValue()) {
                double lx = Math.log10(r.n);
                double ratio = (double) r.edgeScans / r.m;
                pts.add(toScreen(lx, ratio, minX, maxX, minY, maxY, w, h));
            }
            drawSeries(g, pts, c, entry.getKey(), idx, w);
            idx++;
        }
        g.dispose();
        ImageIO.write(img, "png", new File(outPath));
    }

    private static Graphics2D initGraphics(BufferedImage img, int w, int h, String title, String xLabel, String yLabel) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString(title, 80, 35);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString(xLabel, w / 2 - 40, h - 20);
        g.drawString(yLabel, 20, 35);
        return g;
    }

    private static void drawGrid(Graphics2D g, int w, int h, double minX, double maxX, double minY, double maxY) {
        int left = 80, right = w - 220, top = 60, bottom = h - 60;
        g.setColor(new Color(230, 230, 230));
        for (int i = 0; i <= 5; i++) {
            int y = bottom - i * (bottom - top) / 5;
            g.drawLine(left, y, right, y);
            double val = minY + i * (maxY - minY) / 5.0;
            g.setColor(Color.DARK_GRAY);
            g.drawString(String.format(Locale.US, "%.2f", val), left - 45, y + 5);
            g.setColor(new Color(230, 230, 230));
        }
        for (int i = 0; i <= 4; i++) {
            int x = left + i * (right - left) / 4;
            g.drawLine(x, top, x, bottom);
            double val = minX + i * (maxX - minX) / 4.0;
            g.setColor(Color.DARK_GRAY);
            g.drawString(String.format(Locale.US, "%.2f", val), x - 15, bottom + 20);
            g.setColor(new Color(230, 230, 230));
        }
        g.setColor(Color.BLACK);
        g.drawRect(left, top, right - left, bottom - top);
    }

    private static Point toScreen(double x, double y, double minX, double maxX, double minY, double maxY, int w, int h) {
        int left = 80, right = w - 220, top = 60, bottom = h - 60;
        int sx = (int) (left + (x - minX) / (maxX - minX) * (right - left));
        int sy = (int) (bottom - (y - minY) / (maxY - minY) * (bottom - top));
        return new Point(sx, sy);
    }

    private static void drawSeries(Graphics2D g, List<Point> pts, Color c, String label, int idx, int w) {
        g.setColor(c);
        g.setStroke(new BasicStroke(2.2f));
        for (int i = 0; i < pts.size() - 1; i++) {
            g.drawLine(pts.get(i).x, pts.get(i).y, pts.get(i + 1).x, pts.get(i + 1).y);
        }
        for (Point p : pts) {
            g.fillOval(p.x - 4, p.y - 4, 8, 8);
        }
        int lx = w - 205;
        int ly = 80 + idx * 28;
        g.fillRect(lx, ly - 8, 18, 10);
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString(label, lx + 25, ly + 1);
    }
}