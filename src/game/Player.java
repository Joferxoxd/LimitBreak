package game;

import java.awt.*;
import java.util.List;
import java.awt.Rectangle;
public class Player {
    private int x, y;
    private int width, height;
    private int speed;
    // ---- salto / gravedad ----
    private double velX = 0;
    private double velY = 0;          // velocidad vertical
    private final double moveSpeed = 5;
    private final double gravity = 0.6;
    private final double jumpStrength = -14;
    private boolean onGround = false;
    // Para doble salto
    private boolean canDoubleJump = false;
    private boolean doubleJumpUsed = false;

    // === Vida del jugador ===
    private int health = 5;
    private int maxHealth = 5;

    // === Ataque ===
    private boolean attacking = false;   // true mientras el jugador ataca
    private long attackStart = 0;        // para controlar duración
    private final long attackDuration = 300; // milisegundos que dura el golpe


    // ==== Animación Idle ====
    private Image[] idleFrames;      // sprites para la animación
    private int currentFrame = 0;    // índice del sprite actual
    private long lastFrameTime = 0;  // control de tiempo de cambio
    private final int frameDelay = 150; // milisegundos entre frames
    // Constructor
    public Player(int x, int y, int width, int height, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        // --- Carga de sprites Idle ---
        idleFrames = new Image[3];
        for (int i = 0; i < idleFrames.length; i++) {
            // Ajustar ruta de las imágenes del proyecto
            idleFrames[0] = Toolkit.getDefaultToolkit().getImage(
                    getClass().getResource("/player/idle1.png")
            );
            idleFrames[1] = Toolkit.getDefaultToolkit().getImage(
                    getClass().getResource("/player/idle2.png")
            );
            idleFrames[2] = Toolkit.getDefaultToolkit().getImage(
                    getClass().getResource("/player/idle3.png")
            );

        }
    }
    // === Getters ===
    public int getX() { return x; }
    public int getY() { return y; }
    public int getHeight() { return height; }
    public int getWidth()  { return width; }
    // Movimiento horizontal
    public void moveLeft()  { x -= speed; }
    public void moveRight() { x += speed; }
    public void stop()      { velX = 0; }

    // Salto
    public void jump() {
        if (onGround) {
            // Primer salto normal
            velY = jumpStrength;
            canDoubleJump = true;     // habilitamos el segundo salto
            doubleJumpUsed = false;
        } else if (canDoubleJump && !doubleJumpUsed) {
            // Segundo salto
            velY = jumpStrength;
            doubleJumpUsed = true;    // ya lo usamos
        }
    }
    // ---- Daño y empuje ----
    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) health = 0;
    }

    public void knockBack(int offsetX) {
        x += offsetX;
    }
    // ======= Actualiza posicion con gravedad + animacion =======
    // Llama a este metodo en Game.actionPerformed
    public void update(int groundY) {
        // --- física ---
        velY += gravity;
        y += velY;
        // --- control de suelo ---
        if (y + height >= groundY) {
            y = groundY - height;
            velY = 0;
            onGround = true;
        } else {
            onGround = false;
        }
        // --- animación Idle ---
        if (System.currentTimeMillis() - lastFrameTime > 150) { // cada 150 ms
            currentFrame = (currentFrame + 1) % idleFrames.length;
            lastFrameTime = System.currentTimeMillis();
        }
    }
    // ======= Dibuja al jugador con su sprite actual =======
    public void draw(Graphics g) {
        // Si cargamos bien las imágenes, mostrarlas
        if (idleFrames != null && idleFrames.length > 0) {
            g.drawImage(
                    idleFrames[currentFrame], // frame actual
                    x,
                    y,
                    width,
                    height,
                    null
            );
        } else {
            // fallback: rectángulo cian si algo falla
            g.setColor(Color.CYAN);
            g.fillRect(x, y, width, height);
        }


    }
    // ======== Actualizar física y colisiones =========
    public void update(List<Rectangle> platforms, int panelHeight) {
        // gravedad
        velY += 1;
        y += velY;
        // Suelo
        if (y + height >= panelHeight - 100) { // 500 es el suelo en Game
            y = panelHeight - 100 - height;
            velY = 0;
            onGround = true;

            canDoubleJump = false;
            doubleJumpUsed = false;
        } else {
            onGround = false;
        }
        // Colisión con plataformas
        for (Rectangle p : platforms) {
            Rectangle playerRect = new Rectangle(x, y, width, height);
            if (playerRect.intersects(p) && velY >= 0) {
                y = p.y - height;
                velY = 0;
                onGround = true;

                canDoubleJump = false;
                doubleJumpUsed = false;
            }
        }
        // animación Idle
        if (System.currentTimeMillis() - lastFrameTime > frameDelay) {
            currentFrame = (currentFrame + 1) % idleFrames.length;
            lastFrameTime = System.currentTimeMillis();
        }
        // --- terminar ataque si ya pasó el tiempo ---
        if (attacking && System.currentTimeMillis() - attackStart > attackDuration) {
            attacking = false;
        }

    }
    // === metodo para dibujar con offset de camara ===
    public void draw(Graphics g, int cameraX) {
        if (idleFrames != null && idleFrames.length > 0) {
            g.drawImage(
                    idleFrames[currentFrame], // frame actual
                    x - cameraX,              // posición X con cámara
                    y,                        // posición Y
                    width,                    // ancho deseado
                    height,                   // alto deseado
                    null
            );
        } else {
            g.setColor(Color.CYAN);
            g.fillRect(x - cameraX, y, width, height);
        }

        // ---- EFECTO DE ATAQUE ----
        if (attacking) {
            g.setColor(new Color(255, 255, 0, 128)); // amarillo semitransparente
            g.fillRect((x + width) - cameraX, y + height/4, 20, height/2);
        }
    }
    public int getMaxHealth() {
        return maxHealth;
    }
    public int getHealth() {
        return health;
    }
    // === Regenerar vida ===
    public void addHealth(int amount) {
        health += amount;
        if (health > maxHealth) {
            health = maxHealth;   // no pasarse del máximo
        }
    }
    public void startAttack() {
        attacking = true;
        attackStart = System.currentTimeMillis();
    }

    public void stopAttack() {
        attacking = false;
    }

    public boolean isAttacking() {
        return attacking;
    }
    // Zona de ataque para las colisiones
    public Rectangle getAttackBox() {
        if (!attacking) return null;
        // Aquí el rectángulo sale hacia la derecha; ajusta si tu personaje
        // mira a la izquierda o si quieres un rango mayor
        return new Rectangle(x + width, y + height/4, 20, height/2);
    }

}