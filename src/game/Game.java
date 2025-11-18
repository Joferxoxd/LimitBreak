package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;


public class Game extends JPanel implements ActionListener, KeyListener, MouseWheelListener {


    // entradas
    private boolean leftPressed  = false;
    private boolean rightPressed = false;
    private boolean attacking    = false;
    private final Timer timer;
    private final Player player;

    // cámara mundo
    private int cameraX = 1000;
    private int cameraY = 1000;
    private final float cameraLerp = 0.35f;
    private boolean cameraInitialized = false;

    // launcher (pausa)
    private final StartMenuLauncher launcher;
    private int zoom = 2;
    private double zoomFactor = 1.0;



    // enemigo de prueba
    private EnemyMelee enemy;

    // inventario y menús (si ya los tienes)
    private final Inventory inventory;
    private final InventoryMenu inventoryMenu;

    // sala actual
    private Room currentRoom;

    // HUD
    private Image heartImage;
    private int regenCounter = 0;
    private final int regenDelay = 300;

    public Game(StartMenuLauncher launcher, Dimension resolution, boolean fullscreen, int cameraOffsetX, int cameraOffsetY) {
        this.launcher = launcher;
        setPreferredSize(resolution);
        setFocusable(true);
        setRequestFocusEnabled(true);
        addKeyListener(this);
        requestFocusInWindow();
        addMouseWheelListener(this);

        // sala inicial
        currentRoom = new RoomMain();

        // crear player: colocarlo sobre el piso de RoomMain (centro)
        int startX = 1200; // valor razonable dentro de RoomMain
        int startY = 1800; // encima del piso de RoomMain
        player = new Player(startX, startY, 40, 40, 8);
        // aplicar offset inicial a la cámara
        cameraX = player.getX() - (getWidth() / 2) + player.getWidth() / 2 + cameraOffsetX;
        cameraY = player.getY() - (getHeight() / 2) + player.getHeight() / 2 + cameraOffsetY;

        cameraInitialized = true; // evitar que se vuelva a centrar automáticamente

        // centrar cámara en el jugador al iniciar


        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        // load heart quietly
        try {
            Image raw = ImageIO.read(getClass().getResourceAsStream("/items/heart.png"));
            heartImage = raw.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            heartImage = null;
        }

        // inventario y menu
        inventory = new Inventory();
        List<Passive> passives = new ArrayList<>();
        passives.add(new Passive("Vivir en bucles", "Alma", "“Tu alma recuerda lo que tu mente \nolvidó.\nEstás atrapado en un ciclo que no \npuedes romper.”", "Activo", "+1 regen/s"));
        inventoryMenu = new InventoryMenu(inventory, passives);

        // enemigo de prueba
        enemy = new EnemyMelee(400, 820, 40, 40, 200, 500, 2);

        // iniciar timer
        timer = new Timer(16, this);
        timer.start();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // inicializar cámara una sola vez, cuando el panel ya tiene tamaño real
        if (!cameraInitialized && player != null) {
            cameraX = player.getX() - (getWidth() / 2) + player.getWidth() / 2;
            cameraY = player.getY() - (getHeight() / 2) + player.getHeight() / 2;
            cameraInitialized = true;
        }

        Graphics2D g2d = (Graphics2D) g.create();

        // aplicar zoom real
        g2d.scale(zoomFactor, zoomFactor);

        // fondo
        // Fondo negro que cubre toda el área visible, incluso si el zoom revela zonas fuera de la sala
        g.setColor(Color.BLACK);
        g.fillRect(-cameraX, -cameraY, getWidth() * 2, getHeight() * 2); // multiplicado por 2 por seguridad


        // si inventario visible
        if (inventoryMenu.isVisible()) {
            inventoryMenu.draw(g2d);
            g2d.dispose();
            return;
        }

        // dibujar sala
        if (currentRoom != null) currentRoom.draw(g2d, cameraX, cameraY);

        // enemigo / jugador
        if (enemy != null) enemy.draw(g2d, cameraX);
        player.draw(g2d, cameraX, cameraY);

        // HUD corazones (sin zoom)
        g2d.scale(1/zoomFactor, 1/zoomFactor); // HUD no debe escalarse
        int maxHearts = player.getMaxHealth();
        int currentHearts = player.getHealth();
        for (int i = 0; i < maxHearts; i++) {
            int hx = 20 + i * 40;
            int hy = 20;
            if (heartImage != null && i < currentHearts) {
                g2d.drawImage(heartImage, hx, hy, 32, 32, null);
            } else {
                g2d.setColor(new Color(60, 60, 60));
                g2d.fillRect(hx, hy, 32, 32);
            }
        }

        g2d.dispose();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        // pausa si menu abierto
        if (inventoryMenu.isVisible()) {
            repaint();
            return;
        }

        // actualizar player con colisiones de la sala actual
        List<Rectangle> solids = currentRoom != null ? currentRoom.getPlatforms() : new ArrayList<>();
        player.update(solids);

        // actualizar cámara (suave) para seguir player en X e Y
        // --- Cámara suave tipo Hollow Knight / Terraria ---
// === Actualizar cámara con suavizado y límites que respetan el zoom ===

// === Actualizar cámara con suavizado y límites que respetan el zoom ===

        int targetX = player.getX() - (int)((getWidth() / zoomFactor) / 2) + player.getWidth() / 2;
        int targetY = player.getY() - (int)((getHeight() / zoomFactor) / 2) + player.getHeight() / 2;

        cameraX += Math.round((targetX - cameraX) * cameraLerp);
        cameraY += Math.round((targetY - cameraY) * cameraLerp);

        int visibleWidth  = (int)(getWidth()  / zoomFactor);
        int visibleHeight = (int)(getHeight() / zoomFactor);

        if (currentRoom != null) {
            cameraX = Math.max(0, Math.min(cameraX, currentRoom.getWidth()  - visibleWidth));
            cameraY = Math.max(0, Math.min(cameraY, currentRoom.getHeight() - visibleHeight));
        }


        // enemigo simple
        if (enemy != null) enemy.update(player);

        // ataque vs enemigo
        Rectangle atk = player.getAttackBox();
        if (atk != null && enemy != null) {
            Rectangle enemyBox = new Rectangle(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());
            if (atk.intersects(enemyBox)) enemy.takeDamage(1);
        }

        // regen vida lento
        if (player.getHealth() < player.getMaxHealth()) {
            regenCounter++;
            if (regenCounter >= regenDelay) {
                player.addHealth(1);
                regenCounter = 0;
            }
        }

        // detección puerta -> cambiar a dungeon (ejemplo)
        if (currentRoom.getExitDoor() != null && player.getBounds().intersects(currentRoom.getExitDoor())) {
            // entrar a la mazmorra: crear sala dungeon y reubicar jugador dentro
            currentRoom = new RoomDungeon();
            // pon al jugador en el centro de la primera habitación pequeña dentro de RoomDungeon (p. ej. 100,100)
            player.setPosition(100, 100);
            // reset cámara para que empiece a seguir a la nueva posición
            cameraX = 0; cameraY = 0;
        }

        repaint();
    }

    // controles
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ESCAPE) launcher.showPause(this);

        if (inventoryMenu.isVisible()) {
            inventoryMenu.handleInput(key, player);
            return;
        }

        switch (key) {
            case KeyEvent.VK_LEFT -> player.pressLeft(true);
            case KeyEvent.VK_RIGHT -> player.pressRight(true);
            case KeyEvent.VK_SPACE, KeyEvent.VK_W -> player.pressJump();
            case KeyEvent.VK_A -> {
                attacking = true;
                player.startAttack();
            }
            case KeyEvent.VK_I -> inventoryMenu.toggle();
        }

        if (key == KeyEvent.VK_PLUS || key == KeyEvent.VK_EQUALS) zoomFactor *= 1.1;
        if (key == KeyEvent.VK_MINUS) zoomFactor /= 1.1;
        zoomFactor = Math.max(0.25, Math.min(zoomFactor, 8.0));
        repaint();

    }


    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (inventoryMenu.isVisible()) return;

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

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        if (notches < 0) zoomFactor *= 1.1;  // acercar
        else zoomFactor /= 1.1;             // alejar

        zoomFactor = Math.max(0.25, Math.min(zoomFactor, 8.0));
        repaint();
    }


}
