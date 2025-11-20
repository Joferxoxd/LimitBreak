package game;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class RoomDungeon extends Room {

    private static final int ROOM_WIDTH = 800;
    private static final int ROOM_HEIGHT = 600;
    private static final int GRID_SIZE = 3; // 3x3 habitaciones

    private final List<Rectangle> rooms;   // habitaciones (áreas dibujables)
    private final List<Rectangle> walls;   // rectángulos sólidos para colisión

    public RoomDungeon() {
        rooms = new ArrayList<>();
        walls = new ArrayList<>();
        generateDungeon();
    }

    private void generateDungeon() {
        // generar habitaciones en grilla 3x3
        for (int ry = 0; ry < GRID_SIZE; ry++) {
            for (int rx = 0; rx < GRID_SIZE; rx++) {
                int px = rx * ROOM_WIDTH;
                int py = ry * ROOM_HEIGHT;
                rooms.add(new Rectangle(px, py, ROOM_WIDTH, ROOM_HEIGHT));
            }
        }

        // mundo completo
        width = ROOM_WIDTH * GRID_SIZE;
        height = ROOM_HEIGHT * GRID_SIZE;

        int wallThickness = 40;

        // crear paredes exteriores (4 bordes)
        walls.add(new Rectangle(0, 0, width, wallThickness)); // techo
        walls.add(new Rectangle(0, height - wallThickness, width, wallThickness)); // suelo
        walls.add(new Rectangle(0, 0, wallThickness, height)); // izquierda
        walls.add(new Rectangle(width - wallThickness, 0, wallThickness, height)); // derecha

        // paredes internas de cada habitación
        for (Rectangle room : rooms) {
            // top
            walls.add(new Rectangle(room.x, room.y, room.width, wallThickness));
            // bottom
            walls.add(new Rectangle(room.x, room.y + room.height - wallThickness, room.width, wallThickness));
            // left
            walls.add(new Rectangle(room.x, room.y, wallThickness, room.height));
            // right
            walls.add(new Rectangle(room.x + room.width - wallThickness, room.y, wallThickness, room.height));
        }

        // === Crear pasillos entre habitaciones ===
        int doorWidth = 120;
        int doorHeight = 120;

        for (Rectangle room : rooms) {
            // Pasillo a la derecha (si existe sala vecina)
            Rectangle rightNeighbor = findRoomAt(room.x + ROOM_WIDTH, room.y);
            if (rightNeighbor != null) {
                int doorY = room.y + room.height / 2 - doorHeight / 2;
                // quitar pared derecha y crear hueco
                walls.removeIf(w -> w.intersects(new Rectangle(room.x + room.width - wallThickness, doorY, wallThickness, doorHeight)));
            }

            // Pasillo abajo (si existe sala vecina)
            Rectangle bottomNeighbor = findRoomAt(room.x, room.y + ROOM_HEIGHT);
            if (bottomNeighbor != null) {
                int doorX = room.x + room.width / 2 - doorWidth / 2;
                // quitar pared inferior y crear hueco
                walls.removeIf(w -> w.intersects(new Rectangle(doorX, room.y + room.height - wallThickness, doorWidth, wallThickness)));
            }
        }

        // puerta de salida en la sala central
        Rectangle center = rooms.get(4); // índice 4 == centro en 3x3
        exitDoor = new Rectangle(center.x + center.width - 120, center.y + center.height / 2 - 50, 80, 100);

        platforms = new ArrayList<>(walls);
    }

    // Buscar sala vecina en coordenadas
    private Rectangle findRoomAt(int x, int y) {
        for (Rectangle r : rooms) {
            if (r.x == x && r.y == y) return r;
        }
        return null;
    }

    @Override
    public void draw(Graphics g, int cameraX, int cameraY) {
        boolean alt = false;
        for (Rectangle r : rooms) {
            g.setColor(alt ? Color.DARK_GRAY : Color.GRAY);
            g.fillRect(r.x - cameraX, r.y - cameraY, r.width, r.height);
            alt = !alt;
        }

        // Dibujar paredes sólidas
        g.setColor(Color.BLACK);
        for (Rectangle wall : walls) {
            g.fillRect(wall.x - cameraX, wall.y - cameraY, wall.width, wall.height);
        }

        // Dibujar la puerta de salida
        g.setColor(Color.MAGENTA);
        g.fillRect(exitDoor.x - cameraX, exitDoor.y - cameraY, exitDoor.width, exitDoor.height);
    }

    @Override
    public List<Rectangle> getPlatforms() {
        return new ArrayList<>(platforms);
    }

    @Override
    public Rectangle getExitDoor() {
        return exitDoor;
    }
}
