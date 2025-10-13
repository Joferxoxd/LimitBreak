package game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class PassiveMenu {
    private final List<Passive> passives;
    private int selectedIndex = 0;
    private boolean visible = true;

    public PassiveMenu(List<Passive> passives) {
        this.passives = passives;
    }

    public boolean isVisible() {
        return visible;
    }

    public void toggle() {
        visible = !visible;
    }

    public void hide() {
        visible = false;
    }

    public void draw(Graphics g) {
        if (!visible) return;

        // Marco superior
        g.setColor(new Color(180, 160, 220)); // violeta claro
        g.setFont(new Font("Consolas", Font.BOLD, 20));

        // Menú de navegación
        g.setFont(new Font("Consolas", Font.PLAIN, 16));

        // Cuadro del fragmento (izquierda)
        g.drawRect(100, 120, 320, 320); // espacio para sprite 16x16 escalado
        g.drawString("[ Imagen del fragmento ]", 160, 260);

        // Panel de información (derecha)
        Passive selected = passives.get(selectedIndex);
        int infoX = 450;
        int infoY = 160;

        g.drawString("Nombre: " + selected.getNombre(), infoX, infoY);
        g.drawString("Tipo: " + selected.getTipo(), infoX, infoY + 30);
        g.drawString("Descripción:", infoX, infoY + 60);

        // Texto multilinea
        FontMetrics fm = g.getFontMetrics();
        String[] descLines = selected.getDescripcion().split("\n");
        for (int i = 0; i < descLines.length; i++) {
            g.drawString(descLines[i], infoX, infoY + 90 + (i * fm.getHeight()));
        }

        //g.drawString("Estado: " + selected.getEstado(), infoX, infoY + 180);
        //g.drawString("Efecto: " + selected.getEfecto(), infoX, infoY + 210);

        // Contemplacion visual
        g.setFont(new Font("Consolas", Font.ITALIC, 14));
        g.setColor(new Color(200, 180, 240));
        g.drawString("Este fragmento representa una parte rota del alma.", 100, 480);
        g.drawString("No se puede quitar. No se puede ignorar. Solo observar.", 100, 500);

        // Controles
        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        g.setColor(Color.GRAY);
        //g.drawString("↑↓ Mover / J Usar / K Cancelar / I Cerrar", 220, 360);
    }

    public void handleInput(int keyCode) {
        if (!visible) return;
        if (passives.isEmpty()) return;

        switch (keyCode) {
            case KeyEvent.VK_UP:
                selectedIndex = (selectedIndex - 1 + passives.size()) % passives.size();
                break;
            case KeyEvent.VK_DOWN:
                selectedIndex = (selectedIndex + 1) % passives.size();
                break;
            case KeyEvent.VK_J:
                passives.get(selectedIndex).activar(); // lógica de activación
                break;
            case KeyEvent.VK_K:
            case KeyEvent.VK_I:
                hide();
                break;
        }
    }
}