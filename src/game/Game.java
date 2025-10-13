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

    private List<Item> items = new ArrayList<>();

    private final Inventory inventory;
    private final InventoryMenu inventoryMenu; // ← nuevo menú visual


    public Game() {
        // Ventana
        JFrame frame = new JFrame("LimitBreak Prototype");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.add(this);
        frame.addKeyListener(this);
        frame.setResizable(false);
        frame.setVisible(true);

        // Crear jugador
        player = new Player(380, 280, 40, 40, 8);

        // Inventario y menú
        inventory = new Inventory();

        // Crear lista de pasivos
        List<Passive> passives = new ArrayList<>();
        passives.add(new Passive("Vivir en bucles", "Alma", "“Tu alma recuerda lo que tu mente \nolvidó.\nEstás atrapado en un ciclo que no \npuedes romper.”", "Activo", "+1 regen/s"));
        //tipo:"Pasiva Encadenada"

        // ✅ Prueba: Ver cuántas pasivas se cargaron
        System.out.println("Pasivas cargadas: " + passives.size());
        for (Passive p : passives) {
            System.out.println("- " + p.getNombre());
        }

        // Crear el menú de inventario con los pasivos
        //inventoryMenu = new InventoryMenu(inventory);
        inventoryMenu = new InventoryMenu(inventory, passives);


        // Añadir ítems al inventario (sin usarlos todavía)
        inventory.addItem(new HeartItem());
        inventory.addItem(new HeartItem());

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

        // Si el inventario está visible, bloqueatodo y muestra el menú
        if (inventoryMenu.isVisible()) {
            inventoryMenu.draw(g);
            return; // ← no dibujar nada más
        }

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
        // Si el inventario está abierto, pausa la lógica del juego
        if (inventoryMenu.isVisible()) {
            repaint();
            return;
        }

        // Mover horizontalmente mientras se mantiene la tecla
        //if (leftPressed)  player.moveLeft();
        //if (rightPressed) player.moveRight();

        // Actualizar gravedad y colisiones
        player.update(platforms, getHeight());

        // === Actualizar cámara ===
        cameraX = player.getX() - getWidth() / 2;
        if (cameraX < 0) cameraX = 0;
        if (cameraX > worldWidth - getWidth()) {
            cameraX = worldWidth - getWidth();
        }
        enemy.update(player);
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
                enemy.takeDamage(1);                 // le baje 1 punto de vida
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
        int key = e.getKeyCode();

        // Si el inventario está abierto, las teclas van al menú
        if (inventoryMenu.isVisible()) {
            inventoryMenu.handleInput(key, player);
            return;
        }

        switch (key) {
            case KeyEvent.VK_LEFT -> player.pressLeft(true);
            case KeyEvent.VK_RIGHT -> player.pressRight(true);
            case KeyEvent.VK_SPACE -> player.pressJump();
            case KeyEvent.VK_A -> {
                attacking = true;
                player.startAttack();
            }
            case KeyEvent.VK_I -> inventoryMenu.toggle(); // abrir/cerrar menú
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (inventoryMenu.isVisible()) return; // no hacer nada si el inventario está abierto

        switch (key) {
            case KeyEvent.VK_LEFT -> player.pressLeft(false);
            case KeyEvent.VK_RIGHT -> player.pressRight(false);
            case KeyEvent.VK_A -> {
                attacking = false;
                player.stopAttack();
            }
        }
    }

    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }
}