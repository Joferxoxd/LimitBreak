package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.Rectangle;
import game.EnemyMelee;


public class Game extends JPanel implements ActionListener, KeyListener {

    // ==== variables de estado de teclas ====
    private boolean leftPressed  = false;
    private boolean rightPressed = false;
    private boolean attacking    = false;   // ← NUEVO
    private final Timer timer;
    private final Player player;

    // ==== Mundo y cámara ====
    private final int worldWidth = 3000;      // ancho total del nivel
    private int cameraX = 0;                  // desplazamiento horizontal
    private List<Rectangle> platforms = new ArrayList<>();

    // --- regeneración de vida ---
    private int regenCounter = 0;
    private final int regenDelay = 300; // 300 ticks ≈ 5 s si el timer es de 16 ms


    //ENEMY
    private EnemyMelee enemy;


    public Game() {
        // Ventana
        JFrame frame = new JFrame("LimitBreak Prototype");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.add(this);
        frame.addKeyListener(this);
        frame.setVisible(true);

        // Crear jugador
        player = new Player(380, 280, 40, 40, 8);

        // ======== plataformas =========
        platforms = new ArrayList<>();
        platforms.add(new Rectangle(100, 450, 200, 20));
        platforms.add(new Rectangle(400, 350, 150, 20));
        platforms.add(new Rectangle(600, 250, 120, 20));

        // ======== enemigo simple =========
        enemy = new EnemyMelee(
                200, 400,     // posición inicial (x, y)
                40, 40,       // tamaño
                200, 500,     // rango de patrulla izquierda y derecha
                2            // velocidad
        );

        // Timer ~60 fps
        timer = new Timer(16, this);
        timer.start();

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Fondo
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Dibujar plataformas con offset de cámara
        g.setColor(Color.DARK_GRAY);
        for (Rectangle p : platforms) {
            g.fillRect(p.x - cameraX, p.y, p.width, p.height);
        }
        // Dibujar enemigo y jugador
        enemy.draw(g, cameraX);
        player.draw(g, cameraX);

        // --- HUD de Vida ---
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Vida: " + player.getHealth(), 20, 30);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Mover horizontalmente mientras se mantiene la tecla
        if (leftPressed)  player.moveLeft();
        if (rightPressed) player.moveRight();

        // Actualizar gravedad y colisiones
        player.update(platforms, getHeight());

        // === Actualizar cámara ===
        cameraX = player.getX() - getWidth() / 2;
        if (cameraX < 0) cameraX = 0;
        if (cameraX > worldWidth - getWidth()) {
            cameraX = worldWidth - getWidth();
        }
        // === Actualizar enemigo ===
        enemy.update(player);
        // chequeo de ataque del jugador
        Rectangle atk = player.getAttackBox();
        if (atk != null) {                           // si está atacando
            Rectangle enemyBox = new Rectangle(
                    enemy.getX(), enemy.getY(),
                    enemy.getWidth(), enemy.getHeight()
            );
            if (atk.intersects(enemyBox)) {
                enemy.takeDamage(1);                 // le baja 1 punto de vida
            }
        }
        // === Regenerar vida lentamente ===
        if (player.getHealth() < player.getMaxHealth()) {
            regenCounter++;
            if (regenCounter >= regenDelay) {
                player.addHealth(1);  // +1 de vida
                regenCounter = 0;
            }
        }
        repaint();
    }

    // Controles
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT: leftPressed  = true; break;
            case KeyEvent.VK_RIGHT: rightPressed = true; break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_SPACE: player.jump(); break;
            case KeyEvent.VK_A:                 // ← NUEVO: tecla de ataque
                attacking = true;
                player.startAttack();            // (si tienes un método así)
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                leftPressed = false;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = false;
                break;
            case KeyEvent.VK_A:                 // ← NUEVO
                attacking = false;
                player.stopAttack();             // (si lo necesitas)
                break;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }
}