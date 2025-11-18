package game;

import java.awt.*;
import java.util.List;

public class Player {
    private int x, y;
    private int width, height;
    private int speed;

    // física
    private double velX = 0;
    private double velY = 0;
    private final double ax = 2.0;
    private double accelX = 0;
    private final double friction = 0.8;
    private final double maxSpeedX = 12;
    private final double gravity = 0.8;
    private final double maxFall = 18;
    private final double jumpStrength = -12;

    // estado
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private boolean onGround = false;

    // coyote & buffer
    private long lastGroundedTime = 0;
    private long jumpPressedTime = 0;
    private final long coyoteTime = 120;
    private final long jumpBuffer = 120;

    // doble salto
    private boolean canDoubleJump = false;
    private boolean doubleJumpUsed = false;

    // vida
    private int health = 3;
    private int maxHealth = 5;

    // ataque
    private boolean attacking = false;
    private long attackStart = 0;
    private final long attackDuration = 300;

    // sprites (si tienes)
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

        // carga silenciosa si existen recursos (no falla si no encuentra)
        try {
            idleFrames = new Image[3];
            idleFrames[0] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/player/idle1.png"));
            idleFrames[1] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/player/idle2.png"));
            idleFrames[2] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/player/idle3.png"));
        } catch (Exception ignored) {
            idleFrames = null;
        }
    }

    // getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }

    public void setPosition(int nx, int ny) {
        this.x = nx;
        this.y = ny;
        velX = 0;
        velY = 0;
    }

    // inputs
    public void pressLeft(boolean pressed) { movingLeft = pressed; }
    public void pressRight(boolean pressed) { movingRight = pressed; }
    public void pressJump() { jumpPressedTime = System.currentTimeMillis(); }
    public void startAttack() { attacking = true; attackStart = System.currentTimeMillis(); }
    public void stopAttack() { attacking = false; }

    public boolean isAttacking() { return attacking; }

    // actualización: platforms = rectángulos sólidos con coordenadas en píxeles
    public void update(List<Rectangle> platforms) {
        // --- horizontal ---
        accelX = 0;
        if (movingLeft) accelX = -ax;
        else if (movingRight) accelX = ax;

        velX += accelX;
        velX *= friction;

        if (Math.abs(velX) < 0.2) velX = 0;
        if (velX > maxSpeedX) velX = maxSpeedX;
        if (velX < -maxSpeedX) velX = -maxSpeedX;

        x += (int)Math.round(velX);

        // chequear colisiones horizontales
        Rectangle pr = getBounds();
        for (Rectangle r : platforms) {
            if (pr.intersects(r)) {
                if (velX > 0) {
                    x = r.x - width;
                } else if (velX < 0) {
                    x = r.x + r.width;
                }
                velX = 0;
                pr = getBounds();
            }
        }

        // --- vertical ---
        if (onGround) lastGroundedTime = System.currentTimeMillis();
        boolean canJump = (System.currentTimeMillis() - lastGroundedTime) <= coyoteTime;
        boolean buffered = (System.currentTimeMillis() - jumpPressedTime) <= jumpBuffer;

        if (buffered && canJump) {
            velY = jumpStrength;
            onGround = false;
            jumpPressedTime = 0;
            canDoubleJump = true;
            doubleJumpUsed = false;
        }

        velY += gravity;
        if (velY > maxFall) velY = maxFall;

        y += (int)Math.round(velY);

        // colisiones verticales
        pr = getBounds();
        onGround = false;
        for (Rectangle r : platforms) {
            if (pr.intersects(r)) {
                // si venimos desde arriba (caída)
                if (velY > 0 && (y + height - velY) <= r.y + 1) {
                    y = r.y - height;
                    velY = 0;
                    onGround = true;
                    lastGroundedTime = System.currentTimeMillis();
                    canDoubleJump = false;
                    doubleJumpUsed = false;
                } else if (velY < 0 && (y - velY) >= (r.y + r.height - 1)) {
                    // golpe techo
                    y = r.y + r.height;
                    velY = 0;
                } else {
                    // fallback: empujar abajo
                    if (velY > 0) {
                        y = r.y - height;
                        velY = 0;
                        onGround = true;
                        lastGroundedTime = System.currentTimeMillis();
                    }
                }
                pr = getBounds();
            }
        }

        // animacion
        if (idleFrames != null && System.currentTimeMillis() - lastFrameTime > frameDelay) {
            currentFrame = (currentFrame + 1) % idleFrames.length;
            lastFrameTime = System.currentTimeMillis();
        }

        // terminar ataque
        if (attacking && System.currentTimeMillis() - attackStart > attackDuration) attacking = false;
    }

    public void draw(Graphics g, int cameraX, int cameraY) {
        Graphics2D g2 = (Graphics2D) g;
        int sx = x - cameraX;
        int sy = y - cameraY;

        if (idleFrames != null) {
            g2.drawImage(idleFrames[currentFrame], sx, sy, width, height, null);
        } else {
            g2.setColor(Color.CYAN);
            g2.fillRect(sx, sy, width, height);
        }

        if (attacking) {
            g2.setColor(new Color(255, 255, 0, 128));
            g2.fillRect(sx + width, sy + height/4, 20, height/2);
        }
    }

    // vida
    public int getMaxHealth() { return maxHealth; }
    public int getHealth() { return health; }
    public void addHealth(int a) { health = Math.min(maxHealth, health + a); }
    public void takeDamage(int dmg) { health = Math.max(0, health - dmg); }

    public Rectangle getAttackBox() {
        if (!attacking) return null;
        return new Rectangle(x + width, y + height/4, 20, height/2);
    }
    public void knockBack(int force) {
        velX = force;
    }

}
