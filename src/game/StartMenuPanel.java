package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class StartMenuPanel extends JPanel {
    private final String[] options = {"Jugar", "Continuar", "Opciones", "Salir"};
    private int selectedIndex = 0;
    public boolean inOptions = false;
    public int resolutionIndex = 1;
    public int languageIndex = 0;
    public int volume = 60;
    public int optionIndex = 0;
    private final Random rand = new Random();

    private final String[] resolutions = {"800x600", "1280x720", "1366x768", "1920x1080", "Pantalla completa"};
    private final String[] languages = {"Español", "Inglés"};

    private boolean blinkState = true;
    private Timer blinkTimer;

    private StartMenuLauncher launcher;
    @Override
    public void addNotify() {
        super.addNotify();
        requestFocus(); // ← asegura el foco de teclado al mostrarse
    }


    public StartMenuPanel(StartMenuLauncher launcher) {
        this.launcher = launcher;
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.BLACK);

        blinkTimer = new Timer(500, e -> {
            blinkState = !blinkState;
            repaint();
        });
        blinkTimer.start();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInput(e.getKeyCode());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(new Color(100, 100, 255, 80));
        for (int i = 0; i < 60; i++) {
            int x1 = rand.nextInt(getWidth());
            int y1 = rand.nextInt(getHeight());
            int x2 = x1 + rand.nextInt(40) - 20;
            int y2 = y1 + rand.nextInt(40) - 20;
            g.drawLine(x1, y1, x2, y2);
        }

        if (!inOptions) {
            drawMainMenu(g);
        } else {
            drawOptionsMenu(g);
        }
    }

    private void drawMainMenu(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.BOLD, 48));
        g.drawString("LimitBreak", getWidth() / 2 - 140, 100);

        g.setFont(new Font("Consolas", Font.PLAIN, 24));
        for (int i = 0; i < options.length; i++) {
            boolean selected = (i == selectedIndex);
            String label = options[i];
            String text = selected ? "< " + label + " >" : "   " + label;

            g.setColor(selected ? (blinkState ? Color.YELLOW : Color.WHITE) : Color.WHITE);
            g.drawString(text, getWidth() / 2 - 100, 200 + i * 50);
        }

        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        g.setColor(Color.GRAY);
        g.drawString(" ↑ ↓ para moverse | J para seleccionar", getWidth() / 2 - 150, 450);
    }

    private void drawOptionsMenu(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 24));
        g.drawString("OPCIONES", getWidth() / 2 - 70, 60);

        g.setFont(new Font("Consolas", Font.PLAIN, 20));

        g.setColor(optionIndex == 0 ? Color.YELLOW : Color.WHITE);
        g.drawString("[Resolución]", 100, 120);
        g.drawString("< " + resolutions[resolutionIndex] + " >", 300, 120);

        g.setColor(optionIndex == 1 ? Color.YELLOW : Color.WHITE);
        g.drawString("[Idioma]", 100, 170);
        g.drawString("< " + languages[languageIndex] + " >", 300, 170);

        g.setColor(optionIndex == 2 ? Color.YELLOW : Color.WHITE);
        g.drawString("[Volumen]", 100, 220);

        int barX = 300;
        int barY = 205;
        int barWidth = 200;
        int barHeight = 20;
        int fillWidth = (int) (barWidth * (volume / 100.0));

        g.setColor(new Color(30, 30, 30));
        g.fillRect(barX, barY, barWidth, barHeight);
        g.setColor(new Color(80, 160, 200));
        g.fillRect(barX, barY, fillWidth, barHeight);
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(barX, barY, barWidth, barHeight);
        g.drawString(volume + "%", barX + barWidth + 10, barY + 17);

        g.setColor(Color.GRAY);
        g.setFont(new Font("Consolas", Font.ITALIC, 14));
        g.drawString("Versión: 1.0.0", 100, 300);

        g.setColor(optionIndex == 3 ? Color.YELLOW : Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 20));
        g.drawString("[Volver]", 100, 350);
    }

    public void handleInput(int keyCode) {
        if (!inOptions) {
            switch (keyCode) {
                case KeyEvent.VK_UP -> selectedIndex = (selectedIndex - 1 + options.length) % options.length;
                case KeyEvent.VK_DOWN -> selectedIndex = (selectedIndex + 1) % options.length;
                case KeyEvent.VK_ENTER, KeyEvent.VK_J -> {
                    switch (selectedIndex) {
                        case 0 -> {
                            String res = resolutions[resolutionIndex];
                            Dimension size;

                            if (res.equals("Pantalla completa")) {
                                size = Toolkit.getDefaultToolkit().getScreenSize();
                            } else {
                                String[] parts = res.split("x");
                                int w = Integer.parseInt(parts[0]);
                                int h = Integer.parseInt(parts[1]);
                                size = new Dimension(w, h);
                            }

                            boolean fullscreen = res.equals("Pantalla completa");
                            launcher.startGame(size, fullscreen);
                        }
                        case 1 -> System.out.println("Continuar (aún no implementado)");
                        case 2 -> {
                            inOptions = true;
                            optionIndex = 0;
                        }
                        case 3 -> launcher.exitGame();
                    }
                }
            }
        } else {
            switch (keyCode) {
                case KeyEvent.VK_UP -> optionIndex = (optionIndex - 1 + 4) % 4;
                case KeyEvent.VK_DOWN -> optionIndex = (optionIndex + 1) % 4;
                case KeyEvent.VK_LEFT -> {
                    switch (optionIndex) {
                        case 0 -> resolutionIndex = (resolutionIndex - 1 + resolutions.length) % resolutions.length;
                        case 1 -> languageIndex = (languageIndex - 1 + languages.length) % languages.length;
                        case 2 -> volume = Math.max(0, volume - 5);
                    }
                }
                case KeyEvent.VK_RIGHT -> {
                    switch (optionIndex) {
                        case 0 -> resolutionIndex = (resolutionIndex + 1) % resolutions.length;
                        case 1 -> languageIndex = (languageIndex + 1) % languages.length;
                        case 2 -> volume = Math.min(100, volume + 5);
                    }
                }
                case KeyEvent.VK_ENTER, KeyEvent.VK_J -> {
                    if (optionIndex == 3) {
                        inOptions = false;
                    }
                }
                case KeyEvent.VK_ESCAPE -> inOptions = false;
            }
        }
        repaint();
        requestFocusInWindow();
    }
}
