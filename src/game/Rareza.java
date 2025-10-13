package game;

import java.awt.Color;

public enum Rareza {
    COMUN(Color.WHITE),
    RARO(new Color(100, 200, 255)),
    EPICO(new Color(180, 80, 255)),
    LEGENDARIO(new Color(255, 180, 0));

    private final Color color;

    Rareza(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
