package game;

import java.awt.*;
import java.util.*;
import java.util.List;

public class RoomDungeon extends Room {

    // Parámetros de generación
    private static final int NUM_CELLS = 30;              // menos celdas, más salas grandes
    private static final int MAP_WIDTH = 12000;
    private static final int MAP_HEIGHT = 12000;
    private static final int MAX_RADIUS = 1500;           // dispersión amplia
    private static final int MIN_CELL_W = 1000;
    private static final int MAX_CELL_W = 1800;
    private static final int MIN_CELL_H = 800;
    private static final int MAX_CELL_H = 1400;
    private static final double ASPECT_LIMIT = 2.2;
    private static final int SEPARATION_ITER = 600;
    private static final int ROOM_MIN_W = 900;
    private static final int ROOM_MIN_H = 700;
    private static final int K_NEIGHBORS = 3;
    private static final double LOOP_RATIO = 0.15;
    private static final int CORRIDOR_THICKNESS = 120;    // pasillos anchos
    private static final int WALL_THICKNESS = 60;         // paredes gruesas
    private static final int SEGMENT = 80;                // segmentación para carving

    private final List<Rectangle> cellRects = new ArrayList<>();
    private final List<Rectangle> roomFloors = new ArrayList<>();
    private final List<Edge> edgesAll = new ArrayList<>();
    private final List<Edge> edgesFinal = new ArrayList<>();
    private final List<Rectangle> corridors = new ArrayList<>();
    private final List<Rectangle> walls = new ArrayList<>();

    public RoomDungeon() {
        generate();
    }
    private void generate() {
        Random rng = new Random();
        // 1-2. Generar celdas con tamaño sesgado (más pequeñas que grandes)
        for (int i = 0; i < NUM_CELLS; i++) {
            int cx = MAP_WIDTH / 2 + (int)((rng.nextDouble() - 0.5) * 2 * MAX_RADIUS);
            int cy = MAP_HEIGHT / 2 + (int)((rng.nextDouble() - 0.5) * 2 * MAX_RADIUS);

            int w = skewed(rng, MIN_CELL_W, MAX_CELL_W);
            int h = skewed(rng, MIN_CELL_H, MAX_CELL_H);

            // limitar aspecto
            double aspect = Math.max((double)w / h, (double)h / w);
            if (aspect > ASPECT_LIMIT) {
                if (w > h) w = (int)(h * ASPECT_LIMIT);
                else       h = (int)(w * ASPECT_LIMIT);
            }
            cellRects.add(new Rectangle(cx - w / 2, cy - h / 2, w, h));
        }

        // 3. Separación simple para evitar solapes (relajación)
        for (int it = 0; it < SEPARATION_ITER; it++) {
            for (int a = 0; a < cellRects.size(); a++) {
                Rectangle A = cellRects.get(a);
                for (int b = a + 1; b < cellRects.size(); b++) {
                    Rectangle B = cellRects.get(b);
                    if (A.intersects(B)) {
                        Point push = resolveOverlap(A, B);
                        A.translate(-push.x, -push.y);
                        B.translate(push.x, push.y);
                    }
                }
            }
        }

        // 4-5. Selección de habitaciones por umbral
        for (Rectangle r : cellRects) {
            if (r.width >= ROOM_MIN_W && r.height >= ROOM_MIN_H) {
                roomFloors.add(r);
            }
        }
        if (roomFloors.isEmpty()) {
            // fallback mínimo
            roomFloors.add(cellRects.get(NUM_CELLS / 2));
        }

        // 6. Grafo aproximado: k vecinos por centro
        List<Point> centers = new ArrayList<>();
        for (Rectangle r : roomFloors) centers.add(centerOf(r));
        edgesAll.addAll(kNearestGraph(centers, K_NEIGHBORS));

        // 7. MST con Prim
        edgesFinal.addAll(minimumSpanningTree(centers, edgesAll));

        // 8. Añadir bucles (15% aristas restantes)
        List<Edge> remaining = new ArrayList<>(edgesAll);
        remaining.removeAll(edgesFinal);
        Collections.shuffle(remaining, rng);
        int addCount = (int)(remaining.size() * LOOP_RATIO);
        for (int i = 0; i < addCount && i < remaining.size(); i++) {
            edgesFinal.add(remaining.get(i));
        }

        // 9. Corredores en L y paredes negras
        for (Edge e : edgesFinal) {
            Rectangle a = roomFloors.get(e.a);
            Rectangle b = roomFloors.get(e.b);

            // generar corredor en L
            List<Rectangle> candidate = lCorridorBetween(a, b, CORRIDOR_THICKNESS);

            // añadir siempre el corredor
            corridors.addAll(candidate);

            // añadir paredes negras alrededor del pasillo
            for (Rectangle corridor : candidate) {
                walls.add(new Rectangle(corridor.x, corridor.y - WALL_THICKNESS, corridor.width, WALL_THICKNESS)); // arriba
                walls.add(new Rectangle(corridor.x, corridor.y + corridor.height, corridor.width, WALL_THICKNESS)); // abajo
                walls.add(new Rectangle(corridor.x - WALL_THICKNESS, corridor.y, WALL_THICKNESS, corridor.height)); // izquierda
                walls.add(new Rectangle(corridor.x + corridor.width, corridor.y, WALL_THICKNESS, corridor.height)); // derecha
            }
        }
        // Construir paredes alrededor de cada habitación, segmentadas
        for (Rectangle r : roomFloors) {
            addRoomWallsSegmented(r, WALL_THICKNESS, SEGMENT);
        }
        // Carvar huecos de corredores en las paredes
        carveCorridorOpenings(corridors);
        // Dimensiones del mundo y salida
        width = MAP_WIDTH;
        height = MAP_HEIGHT;
        // Puerta de salida: al borde norte de una habitación grande
        Rectangle biggest = roomFloors.stream()
                .max(Comparator.comparingInt(o -> o.width * o.height))
                .orElse(roomFloors.get(0));
        exitDoor = new Rectangle(biggest.x + biggest.width - 120,
                biggest.y + biggest.height / 2 - 50, 80, 100);

        // platforms = paredes sólidas para colisión
        platforms = new ArrayList<>(walls);
    }
    @Override
    public void draw(Graphics g, int cameraX, int cameraY) {
        // fondo
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0 - cameraX, 0 - cameraY, width, height);

        // habitaciones (suelo)
        g.setColor(new Color(80, 80, 120));
        for (Rectangle r : roomFloors) {
            g.fillRect(r.x - cameraX, r.y - cameraY, r.width, r.height);
        }

        // corredores (suelo)
        g.setColor(new Color(100, 100, 100));
        for (Rectangle c : corridors) {
            g.fillRect(c.x - cameraX, c.y - cameraY, c.width, c.height);
        }

        // paredes
        g.setColor(Color.BLACK);
        for (Rectangle w : walls) {
            g.fillRect(w.x - cameraX, w.y - cameraY, w.width, w.height);
        }

        // puerta salida
        if (exitDoor != null) {
            g.setColor(Color.MAGENTA);
            g.fillRect(exitDoor.x - cameraX, exitDoor.y - cameraY, exitDoor.width, exitDoor.height);
        }
    }
    @Override
    public List<Rectangle> getPlatforms() {
        return new ArrayList<>(platforms);
    }
    @Override
    public Rectangle getExitDoor() {
        return exitDoor;
    }
    // ---------- Helpers de generación ----------
    private int skewed(Random rng, int min, int max) {
        // sesgo hacia valores pequeños: promedio de varios randoms
        double t = (rng.nextDouble() + rng.nextDouble() + rng.nextDouble()) / 3.0;
        return min + (int)(t * (max - min));
    }
    private Point centerOf(Rectangle r) {
        return new Point(r.x + r.width / 2, r.y + r.height / 2);
    }
    private Point resolveOverlap(Rectangle A, Rectangle B) {
        int ax = A.x + A.width / 2;
        int ay = A.y + A.height / 2;
        int bx = B.x + B.width / 2;
        int by = B.y + B.height / 2;

        int dx = bx - ax;
        int dy = by - ay;

        int overlapX = (A.width + B.width) / 2 - Math.abs(dx);
        int overlapY = (A.height + B.height) / 2 - Math.abs(dy);

        int pushX = dx == 0 ? 1 : (dx > 0 ? 1 : -1);
        int pushY = dy == 0 ? 1 : (dy > 0 ? 1 : -1);

        if (overlapX > 0 && overlapY > 0) {
            if (overlapX < overlapY) {
                return new Point(pushX * overlapX / 2, 0);
            } else {
                return new Point(0, pushY * overlapY / 2);
            }
        }
        return new Point(0, 0);
    }
    private List<Edge> kNearestGraph(List<Point> pts, int k) {
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < pts.size(); i++) {
            Point a = pts.get(i);
            // ordenar vecinos por distancia
            List<Integer> idx = new ArrayList<>();
            for (int j = 0; j < pts.size(); j++) if (j != i) idx.add(j);
            idx.sort(Comparator.comparingDouble(j -> dist(a, pts.get(j))));
            for (int t = 0; t < Math.min(k, idx.size()); t++) {
                int j = idx.get(t);
                Edge e = new Edge(i, j, dist(a, pts.get(j)));
                if (!edges.contains(e)) edges.add(e);
            }
        }
        return edges;
    }
    private double dist(Point a, Point b) {
        int dx = a.x - b.x, dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private List<Edge> minimumSpanningTree(List<Point> pts, List<Edge> edges) {
        List<Edge> mst = new ArrayList<>();
        Set<Integer> inTree = new HashSet<>();
        inTree.add(0);
        while (inTree.size() < pts.size()) {
            Edge best = null;
            double bestW = Double.MAX_VALUE;
            for (Edge e : edges) {
                boolean aIn = inTree.contains(e.a);
                boolean bIn = inTree.contains(e.b);
                if (aIn ^ bIn) { // conecta árbol con un nuevo nodo
                    if (e.w < bestW) { bestW = e.w; best = e; }
                }
            }
            if (best == null) break; // desconectado (raro), salir
            mst.add(best);
            inTree.add(best.a);
            inTree.add(best.b);
        }
        return mst;
    }

    private List<Rectangle> lCorridorBetween(Rectangle A, Rectangle B, int thick) {
        List<Rectangle> out = new ArrayList<>();
        Point ca = centerOf(A);
        Point cb = centerOf(B);

        // Tramo horizontal primero, luego vertical (L)
        int x1 = Math.min(ca.x, cb.x);
        int x2 = Math.max(ca.x, cb.x);
        int yMid = ca.y;
        Rectangle h = new Rectangle(x1, yMid - thick / 2, x2 - x1, thick);

        int y1 = Math.min(ca.y, cb.y);
        int y2 = Math.max(ca.y, cb.y);
        int xMid = cb.x;
        Rectangle v = new Rectangle(xMid - thick / 2, y1, thick, y2 - y1);

        // Ajustes para evitar huecos y solapes mínimos
        out.add(expand(h, 10));
        out.add(expand(v, 10));
        return out;
    }

    private Rectangle expand(Rectangle r, int pad) {
        return new Rectangle(r.x - pad, r.y - pad, r.width + 2 * pad, r.height + 2 * pad);
    }

    private void addRoomWallsSegmented(Rectangle r, int thick, int seg) {
        // Top
        for (int x = r.x; x < r.x + r.width; x += seg) {
            int w = Math.min(seg, r.x + r.width - x);
            walls.add(new Rectangle(x, r.y, w, thick));
        }
        // Bottom
        for (int x = r.x; x < r.x + r.width; x += seg) {
            int w = Math.min(seg, r.x + r.width - x);
            walls.add(new Rectangle(x, r.y + r.height - thick, w, thick));
        }
        // Left
        for (int y = r.y; y < r.y + r.height; y += seg) {
            int h = Math.min(seg, r.y + r.height - y);
            walls.add(new Rectangle(r.x, y, thick, h));
        }
        // Right
        for (int y = r.y; y < r.y + r.height; y += seg) {
            int h = Math.min(seg, r.y + r.height - y);
            walls.add(new Rectangle(r.x + r.width - thick, y, thick, h));
        }
    }

    private void carveCorridorOpenings(List<Rectangle> corrs) {
        // Eliminar segmentos de pared que intersecten corredores
        List<Rectangle> kept = new ArrayList<>();
        for (Rectangle w : walls) {
            boolean hit = false;
            for (Rectangle c : corrs) {
                if (w.intersects(c)) { hit = true; break; }
            }
            if (!hit) kept.add(w);
        }
        walls.clear();
        walls.addAll(kept);
    }

    // Arista del grafo
    private static class Edge {
        int a, b;
        double w;
        Edge(int a, int b, double w) {
            // normalizar orden para equals/hashCode
            if (a < b) { this.a = a; this.b = b; }
            else { this.a = b; this.b = a; }
            this.w = w;
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Edge e)) return false;
            return a == e.a && b == e.b;
        }
        @Override public int hashCode() { return Objects.hash(a, b); }
    }
}
