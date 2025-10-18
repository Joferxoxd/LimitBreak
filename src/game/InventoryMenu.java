package game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class InventoryMenu {
    private final Inventory inventory;
    private final PassiveMenu passiveMenu;
    private int selectedIndex = 0;
    private boolean visible = false;
    private int currentTab = 0;
    private boolean onTabs = false;
    private long blinkTimer = 0;
    private boolean blinkState = true;
    // Scroll y cuadrícula
    private final int cols = 4;
    private final int rows = 6;
    private final int visibleSlots = cols * rows; // 24
    private final int totalSlots = 100; // capacidad total
    private int scrollOffset = 0;

    public InventoryMenu(Inventory inventory, List<Passive> passives) {
        this.inventory = inventory;
        this.passiveMenu = new PassiveMenu(passives);
    }
    public boolean isVisible() {
        return visible;
    }
    public void toggle() {
        visible = !visible;
        onTabs = false;
        if (visible) {
            currentTab = 0;         // vuelve a pestaña Inventario
            selectedIndex = 0;      // cursor en primer ítem
            scrollOffset = 0;       // vista desde arriba
            passiveMenu.hide(); // ✅ oculta pasivas al abrir
        } else {
            passiveMenu.hide(); // ✅ oculta pasivas al cerrar
        }
    }
    public void hide() {
        visible = false;
        onTabs = false;
    }
    public void draw(Graphics g) {
        if (!visible) return;

        Graphics2D g2d = (Graphics2D) g;

        // Fondo general
        g2d.setColor(new Color(10, 10, 10, 230));
        g2d.fillRoundRect(40, 30, 710, 500, 20, 20);

        // Título
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Consolas", Font.BOLD, 20));
        g2d.drawString("[ MENÚ DEL JUGADOR ]", 290, 70);

        // Pestañas
        g2d.setFont(new Font("Consolas", Font.PLAIN, 16));
        int tabY = 110;
        for (int i = 0; i < 3; i++) {
            String name = switch (i) {
                case 0 -> "Inventario";
                case 1 -> "Habilidades";
                case 2 -> "Pasivas";
                default -> "";
            };
            int x = 180 + (i * 180);
            if (onTabs && currentTab == i) {
                g2d.setColor(Color.YELLOW);
                g2d.drawString("< " + name + " >", x, tabY);
            } else if (!onTabs && currentTab == i) {
                g2d.setColor(Color.WHITE);
                g2d.drawString("[ " + name + " ]", x, tabY);
            } else {
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawString(name, x, tabY);
            }
        }
        // Controles
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Consolas", Font.PLAIN, 14));
        g2d.drawString("← → Mover / Cambiar pestaña | ↑↓ Navegar | J Usar | K / I Cerrar", 120, 550);

        if (currentTab == 2 && !passiveMenu.isVisible()) {
            passiveMenu.toggle(); // asegura que pasivas estén visibles al dibujar pestaña 2
        }

        switch (currentTab) {
            case 0 -> {
                // Marco del inventario SOLO en pestana 0
                int frameX = 90;
                int frameY = 140;
                int frameWidth = 360;
                int frameHeight = 350;

                g2d.setColor(new Color(20, 20, 20, 200));
                g2d.fillRoundRect(frameX, frameY, frameWidth, frameHeight, 20, 20);
                g2d.setColor(Color.GRAY);
                g2d.drawRoundRect(frameX, frameY, frameWidth, frameHeight, 20, 20);

                drawInventory(g2d, frameX, frameY, frameWidth, frameHeight);
            }
            case 1 -> drawAbilitiesPlaceholder(g2d);
            case 2 -> passiveMenu.draw(g2d);
        }
    }
    private void drawInventory(Graphics g, int frameX, int frameY, int frameWidth, int frameHeight) {
        Graphics2D g2d = (Graphics2D) g;
        List<Item> items = inventory.getItems();

        int iconSize = 70;
        int spacing = 10;

        int startX = frameX + 20;
        int startY = frameY + 20;

        if (System.currentTimeMillis() - blinkTimer > 400) {
            blinkTimer = System.currentTimeMillis();
            blinkState = !blinkState;
        }
        // Recorte visual limitar dibujo al área del marco
        Shape originalClip = g2d.getClip();
        g2d.setClip(new Rectangle(frameX + 5, frameY + 5, frameWidth - 10, frameHeight - 10));

        int startIndex = scrollOffset;
        int endIndex = Math.min(startIndex + visibleSlots, totalSlots);

        for (int i = startIndex; i < endIndex; i++) {
            int localIndex = i - startIndex;
            int row = localIndex / cols;
            int col = localIndex % cols;
            int x = startX + col * (iconSize + spacing);
            int y = startY + row * (iconSize + spacing);

            Item item = i < items.size() ? items.get(i) : null;

            g2d.setColor(new Color(30, 30, 30));
            g2d.fillRoundRect(x, y, iconSize, iconSize, 10, 10);

            if (!onTabs && i == selectedIndex && blinkState) {
                Color rareColor = item != null ? item.getRareza().getColor() : Color.WHITE;
                g2d.setColor(rareColor);
                int thickness = 3;
                int length = 12;

                g2d.fillRect(x - thickness, y - thickness, length, thickness);
                g2d.fillRect(x - thickness, y - thickness, thickness, length);
                g2d.fillRect(x + iconSize - length + thickness, y - thickness, length, thickness);
                g2d.fillRect(x + iconSize, y - thickness, thickness, length);
                g2d.fillRect(x - thickness, y + iconSize, length, thickness);
                g2d.fillRect(x - thickness, y + iconSize - length + thickness, thickness, length);
                g2d.fillRect(x + iconSize - length + thickness, y + iconSize, length, thickness);
                g2d.fillRect(x + iconSize, y + iconSize - length + thickness, thickness, length);
            }

            if (item != null && item.getIcon() != null) {
                g2d.drawImage(item.getIcon(), x + 4, y + 4, 56, 56, null);
            } //else if (item != null) {
                //g2d.setColor(Color.WHITE);
                //g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                //g2d.drawString(item.getNombre(), x - 5, y + 80);
            //} else {
            //    g2d.setColor(Color.DARK_GRAY);
            //    g2d.setFont(new Font("Consolas", Font.PLAIN, 12));
            //    g2d.drawString("[ Vacío ]", x + 8, y + 80);
            //}
        }
        // Restaurar el area de dibujo original
        g2d.setClip(originalClip);

        // Flechas de scroll
        if (scrollOffset > 0) {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("▲", frameX + frameWidth - 20, frameY + 20);
        }
        if (scrollOffset + visibleSlots < totalSlots) {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("▼", frameX + frameWidth - 20, frameY + frameHeight - 10);
        }

        // Descripcion del ítem
        if (!onTabs && selectedIndex < items.size()) {
            Item selected = items.get(selectedIndex);
            int infoX = 500;
            int infoY = 200;

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Consolas", Font.BOLD, 18));
            g2d.drawString(selected.getNombre(), infoX, infoY);
            g2d.setFont(new Font("Consolas", Font.PLAIN, 14));
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("Descripción:", infoX, infoY + 30);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.ITALIC, 14));
            drawMultilineText(g2d, selected.getDescripcion(), infoX, infoY + 50, 250, 18);
        }
    }
    private void drawAbilitiesPlaceholder(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Consolas", Font.PLAIN, 18));
        g.drawString("[ En desarrollo ]", 330, 300);
    }

    public void handleInput(int keyCode, Player player) {
        if (!visible) return;

        if (onTabs) {
            switch (keyCode) {
                case KeyEvent.VK_LEFT -> {
                    currentTab = (currentTab - 1 + 3) % 3;
                    passiveMenu.hide(); // ✅ oculta pasivas si sales de pestaña 2
                }
                case KeyEvent.VK_RIGHT -> {
                    currentTab = (currentTab + 1) % 3;
                    passiveMenu.hide(); // ✅ oculta pasivas si sales de pestaña 2
                }
                case KeyEvent.VK_DOWN -> {
                    onTabs = false;
                    if (currentTab == 2 && !passiveMenu.isVisible()) {
                        passiveMenu.toggle(); // ✅ muestra pasivas al entrar a pestaña 2
                    }
                }
                case KeyEvent.VK_K, KeyEvent.VK_I -> {
                    passiveMenu.hide(); // ✅ oculta pasivas si cierras desde pestañas
                    hide();             // ✅ cierra inventario
                }
            }
            return; // ✅ este return va FUERA del switch
        }


        if (currentTab == 0) {
            int maxIndex = totalSlots - 1;

            switch (keyCode) {
                case KeyEvent.VK_UP -> {
                    if (selectedIndex >= cols) selectedIndex -= cols;
                    else onTabs = true;
                }
                case KeyEvent.VK_DOWN -> {
                    if (selectedIndex + cols <= maxIndex) selectedIndex += cols;
                }
                case KeyEvent.VK_LEFT -> {
                    if (selectedIndex > 0) selectedIndex--;
                }
                case KeyEvent.VK_RIGHT -> {
                    if (selectedIndex < maxIndex) selectedIndex++;
                }
                case KeyEvent.VK_J -> {
                    List<Item> items = inventory.getItems();
                    if (selectedIndex < items.size()) {
                        inventory.useItem(items.get(selectedIndex), player);
                    }
                }
                case KeyEvent.VK_K, KeyEvent.VK_I -> hide();
            }

            if (selectedIndex < scrollOffset) {
                scrollOffset = selectedIndex;
            } else if (selectedIndex >= scrollOffset + visibleSlots) {
                scrollOffset = selectedIndex - visibleSlots + 1;
            }
        } else if (currentTab == 2) {
            passiveMenu.handleInput(keyCode);

            if (keyCode == KeyEvent.VK_UP) onTabs = true;

            if (keyCode == KeyEvent.VK_I || keyCode == KeyEvent.VK_K) {
                passiveMenu.hide(); // ✅ oculta pasivas
                hide();             // ✅ oculta inventario
            }
        }
        if (currentTab == 2) {
            passiveMenu.toggle(); // ✅ muestra pasivas si la pestaña activa es 2
        }


    }
    private void drawMultilineText(Graphics g, String text, int x, int y, int width, int lineHeight) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int drawY = y;

        for (String word : words) {
            String testLine = line + word + " ";
            if (fm.stringWidth(testLine) > width) {
                g.drawString(line.toString(), x, drawY);
                line = new StringBuilder(word + " ");
                drawY += lineHeight;
            } else {
                line.append(word).append(" ");
            }
        }
        g.drawString(line.toString(), x, drawY);
    }
}