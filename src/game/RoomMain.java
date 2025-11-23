package game;

import java.awt.*;
import java.util.ArrayList;

public class RoomMain extends Room {

    // Offset (desplazamiento de la sala)
    private final int offsetX = 0;
    private final int offsetY = 0;

    public RoomMain() {
        width = 2000;
        height = 1000;

        platforms = new ArrayList<>();
        platforms.add(new Rectangle(offsetX + 0, offsetY + 900, width, 100)); // piso

        exitDoor = new Rectangle(offsetX + 1800, offsetY + 800, 80, 100); // puerta mazmorra
    }

    @Override
    public void draw(Graphics g, int cameraX, int cameraY) {
        // fondo gris oscuro
        g.setColor(new Color(40, 40, 40));
        g.fillRect(offsetX - cameraX, offsetY - cameraY, width, height);

        // piso gris medio
        g.setColor(new Color(120, 120, 120));
        for (Rectangle p : platforms) {
            g.fillRect(p.x - cameraX, p.y - cameraY, p.width, p.height);
        }

        // borde superior
        g.setColor(Color.BLACK);
        g.fillRect(offsetX - cameraX, offsetY - cameraY, width, 30);

        // puerta mazmorra (temporal)
        g.setColor(Color.YELLOW);
        g.fillRect(exitDoor.x - cameraX, exitDoor.y - cameraY, exitDoor.width, exitDoor.height);

        g.setColor(Color.WHITE);
        g.drawString("ENTRAR A MAZMORRA", exitDoor.x - cameraX - 30, exitDoor.y - 10 - cameraY);
    }
}
