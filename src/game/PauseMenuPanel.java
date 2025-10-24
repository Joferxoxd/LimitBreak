package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class PauseMenuPanel extends JPanel {
    private final String[] options = {"Regresar al juego", "Volver al menú principal", "Salir"};
    private int selectedIndex = 0;
    private final StartMenuLauncher launcher;
    private final Game previousGame;
    private final Random rand = new Random();

    private boolean blinkState = true;
    private Timer blinkTimer;

    public PauseMenuPanel(StartMenuLauncher launcher, Game previousGame) {
        this.launcher = launcher;
        this.previousGame = previousGame;

        setFocusable(true);
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
    public void addNotify() {
        super.addNotify();
        requestFocus();
    }

    private void handleInput(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP -> selectedIndex = (selectedIndex - 1 + options.length) % options.length;
            case KeyEvent.VK_DOWN -> selectedIndex = (selectedIndex + 1) % options.length;
            case KeyEvent.VK_ENTER, KeyEvent.VK_J -> {
                switch (selectedIndex) {
                    case 0 -> launcher.resumeGame(previousGame); // Regresar al juego
                    case 1 -> launcher.showMenu();               // Volver al menú principal
                    case 2 -> launcher.exitGame();               // Salir
                }
            }
            case KeyEvent.VK_ESCAPE -> launcher.resumeGame(previousGame); // ESC también regresa al juego
        }
        repaint();
        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Fondo negro
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Líneas azules como en StartMenuPanel
        g.setColor(new Color(100, 100, 255, 80));
        for (int i = 0; i < 60; i++) {
            int x1 = rand.nextInt(getWidth());
            int y1 = rand.nextInt(getHeight());
            int x2 = x1 + rand.nextInt(40) - 20;
            int y2 = y1 + rand.nextInt(40) - 20;
            g.drawLine(x1, y1, x2, y2);
        }

        // Título
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.BOLD, 48));
        g.drawString("PAUSA", getWidth() / 2 - 80, 100);

        // Opciones
        g.setFont(new Font("Consolas", Font.PLAIN, 24));
        for (int i = 0; i < options.length; i++) {
            boolean selected = (i == selectedIndex);
            String label = options[i];
            String text = selected ? "< " + label + " >" : "   " + label;

            g.setColor(selected ? (blinkState ? Color.YELLOW : Color.WHITE) : Color.WHITE);
            g.drawString(text, getWidth() / 2 - 150, 200 + i * 50);
        }

        // Instrucciones
        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        g.setColor(Color.GRAY);
        g.drawString(" ↑ ↓ para moverse | J para seleccionar | ESC para regresar", getWidth() / 2 - 200, 450);
    }
}
