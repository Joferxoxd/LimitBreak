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
    private double velY = 0;
    private final double moveSpeed = 7;
    private final double gravity = 0.8;
    private final double jumpStrength = -12;
    private boolean onGround = false;

    // --- Movimiento suave / física avanzada ---
    private double ax = 2.0;                 // fuerza lateral
    private double accelX = 2.0;             // aceleración horizontal
    private double friction = 0.8;           // ↓ más baja = frena más rápido
    private double maxSpeedX = 12;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    // --- Jump buffer & coyote time --
    private long lastGroundedTime = 0;
    private long jumpPressedTime = 0;
    private final long coyoteTime = 120;
    private final long jumpBuffer = 120;

    // Doble salto
    private boolean canDoubleJump = false;
    private boolean doubleJumpUsed = false;

    // Vida
    private int health = 5;
    private int maxHealth = 5;
    //public int getMaxHealth() { return maxHealth; }
    //public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
    //public int getHealth() { return health; }
    //public void setHealth(int health) { this.health = health; }

    // Ataque
    private boolean attacking = false;
    private long attackStart = 0;
    private final long attackDuration = 300;

    // Animacion Idle
    private Image[] idleFrames;
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private final int frameDelay = 150;

    public Player(int x, int y, int width, int height, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;

        // Carga de sprites Idle
        idleFrames = new Image[3];
        idleFrames[0] = Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/player/idle1.png"));
        idleFrames[1] = Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/player/idle2.png"));
        idleFrames[2] = Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/player/idle3.png"));
    }

    // === Getters ===
    public int getX() { return x; }
    public int getY() { return y; }
    public int getHeight() { return height; }
    public int getWidth()  { return width; }

    // Salto
    public void jump() {
        if (onGround) {
            velY = jumpStrength;
            canDoubleJump = true;
            doubleJumpUsed = false;
        } else if (canDoubleJump && !doubleJumpUsed) {
            velY = jumpStrength;
            doubleJumpUsed = true;
        }
    }

    public void pressLeft(boolean pressed)  { movingLeft  = pressed; }
    public void pressRight(boolean pressed) { movingRight = pressed; }
    public void pressJump() { jumpPressedTime = System.currentTimeMillis(); }

    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) health = 0;
    }

    public void knockBack(int offsetX) { x += offsetX; }

    // ======= Actualiza posición con fisica =======
    public void update(List<Rectangle> platforms, int panelHeight) {

        // --- movimiento horizontal ---
        if (movingLeft)      accelX = -ax;
        else if (movingRight) accelX =  ax;
        else                 accelX =  0;

        velX += accelX;
        velX *= friction;

        // corte para evitar patinaje cuando la velocidad es muy pequena
        if (Math.abs(velX) < 0.2) velX = 0;

        // limitar velocidad maxima
        if (velX >  maxSpeedX) velX =  maxSpeedX;
        if (velX < -maxSpeedX) velX = -maxSpeedX;

        x += velX;

        // --- salto con coyote time y jump buffer ---
        if (onGround) lastGroundedTime = System.currentTimeMillis();

        boolean canJump = (System.currentTimeMillis() - lastGroundedTime <= coyoteTime);
        boolean buffered = (System.currentTimeMillis() - jumpPressedTime <= jumpBuffer);

        if (canJump && buffered) {
            velY = jumpStrength;
            onGround = false;
            jumpPressedTime = 0;
        }

        // gravedad
        velY += gravity;
        y += velY;

        // suelo “fijo” del panel
        if (y + height >= panelHeight - 100) {
            y = panelHeight - 100 - height;
            velY = 0;
            onGround = true;
            canDoubleJump = false;
            doubleJumpUsed = false;
        } else {
            onGround = false;
        }

        // colision con plataformas
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

        // animacion Idle
        if (System.currentTimeMillis() - lastFrameTime > frameDelay) {
            currentFrame = (currentFrame + 1) % idleFrames.length;
            lastFrameTime = System.currentTimeMillis();
        }

        // terminar ataque si ya paso el tiempo
        if (attacking && System.currentTimeMillis() - attackStart > attackDuration) {
            attacking = false;
        }
    }

    // === Dibujar ===
    public void draw(Graphics g, int cameraX) {
        if (idleFrames != null && idleFrames.length > 0) {
            g.drawImage(
                    idleFrames[currentFrame],
                    x - cameraX,
                    y,
                    width,
                    height,
                    null
            );
        } else {
            g.setColor(Color.CYAN);
            g.fillRect(x - cameraX, y, width, height);
        }

        // Efecto de ataque
        if (attacking) {
            g.setColor(new Color(255, 255, 0, 128));
            g.fillRect((x + width) - cameraX, y + height / 4, 20, height / 2);
        }
    }

    // Vida
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }
    public void addHealth(int amount) {
        health += amount;
        if (health > maxHealth) health = maxHealth;
    }

    // Ataque
    public void startAttack() {
        attacking = true;
        attackStart = System.currentTimeMillis();
    }
    public void stopAttack() { attacking = false; }
    public boolean isAttacking() { return attacking; }

    public Rectangle getAttackBox() {
        if (!attacking) return null;
        return new Rectangle(x + width, y + height/4, 20, height/2);
    }
}