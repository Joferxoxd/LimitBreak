package game;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

        // crear paredes exteriores (4 bordes) — grosor 40 px
        int wallThickness = 40;
        walls.add(new Rectangle(0, 0, width, wallThickness)); // techo
        walls.add(new Rectangle(0, height - wallThickness, width, wallThickness)); // suelo
        walls.add(new Rectangle(0, 0, wallThickness, height)); // izquierda
        walls.add(new Rectangle(width - wallThickness, 0, wallThickness, height)); // derecha

        // crear paredes interiores por cada habitación: pared interior inferior (para que cada sala sea caja)
        // además añadimos algunos muros internos en la división entre habitaciones para simular puertas laterales si quieres.
        for (Rectangle room : rooms) {
            // paredes internas de la habitación (bordes)
            walls.add(new Rectangle(room.x, room.y, room.width, wallThickness)); // top
            walls.add(new Rectangle(room.x, room.y + room.height - wallThickness, room.width, wallThickness)); // bottom
            walls.add(new Rectangle(room.x, room.y, wallThickness, room.height)); // left
            walls.add(new Rectangle(room.x + room.width - wallThickness, room.y, wallThickness, room.height)); // right
        }

        // opcional: crear huecos (puertas) entre habitaciones — por ahora dejamos cerradas
        // Exit door placeholder: la colocamos en la esquina de la sala central
        Rectangle center = rooms.get(4); // índice 4 == centro en 3x3
        exitDoor = new Rectangle(center.x + center.width - 120, center.y + center.height/2 - 50, 80, 100);

        // transform walls -> platforms (lo que devolverá getPlatforms)
        platforms = new ArrayList<>(walls);
    }

    @Override
    public void draw(Graphics g, int cameraX, int cameraY) {
        boolean alt = false;
        for (Rectangle r : rooms) {
            g.setColor(alt ? Color.DARK_GRAY : Color.GRAY);
            g.fillRect(r.x - cameraX, r.y - cameraY, r.width, r.height);
            alt = !alt;
        }

        // Dibujar paredes sólidas (negro)
        g.setColor(Color.BLACK);
        for (Rectangle wall : walls) {
            g.fillRect(wall.x - cameraX, wall.y - cameraY, wall.width, wall.height);
        }

        // Dibujar la puerta de salida (si quieres mostrarla)
        g.setColor(Color.MAGENTA);
        g.fillRect(exitDoor.x - cameraX, exitDoor.y - cameraY, exitDoor.width, exitDoor.height);
    }

    @Override
    public List<Rectangle> getPlatforms() {
        // devolvemos las paredes como plataformas sólidas para las colisiones
        return new ArrayList<>(platforms);
    }

    @Override
    public Rectangle getExitDoor() {
        return exitDoor;
    }
}
