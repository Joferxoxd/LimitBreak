package game;

import java.awt.*;
import java.util.List;

public abstract class Room {
    protected int width;
    protected int height;
    protected List<Rectangle> platforms;
    protected Rectangle exitDoor;

    // ahora la sala recibe cameraX y cameraY para dibujar correctamente
    public abstract void draw(Graphics g, int cameraX, int cameraY);

    public List<Rectangle> getPlatforms() {
        return platforms;
    }

    public Rectangle getExitDoor() {
        return exitDoor;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
