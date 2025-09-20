package game;

import java.awt.*;

/**
 * Enemigo que patrulla entre dos puntos (patrolLeft y patrolRight).
 * Si el jugador entra en rango, lo persigue.
 * Al chocar con el jugador le quita vida y lo empuja.
 */
public class EnemyMelee {

    private int x, y;
    private int width, height;

    private int patrolLeft, patrolRight; // límites de patrulla
    private int speed;
    private int dir = 1; // 1 = derecha, -1 = izquierda

    private int damage = 1;         // vida que quita al golpear
    private int detectionRange = 150; // rango para empezar a perseguir
    private int pushBack = 20;      // fuerza del empujón

    private boolean touchingPlayer = false; // evita restar vida varias veces por frame
    private int health = 3;    // nueva vida del enemigo
    private boolean alive = true; // ya está, lo mantenemos


    public EnemyMelee(int x, int y, int width, int height,
                      int patrolLeft, int patrolRight, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.patrolLeft = patrolLeft;
        this.patrolRight = patrolRight;
        this.speed = speed;
    }

    /** Actualiza movimiento y colisión */
    public void update(Player player) {
        if (!alive) return;
        // Calcular distancia al jugador
        int dx = player.getX() - x;
        int dy = player.getY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        // Si el jugador está cerca, perseguirlo
        if (dist < detectionRange) {
            if (player.getX() > x) x += speed;
            else if (player.getX() < x) x -= speed;
        } else {
            // Si no, patrullar entre los límites
            x += speed * dir;
            if (x <= patrolLeft) dir = 1;
            if (x + width >= patrolRight) dir = -1;
        }

        // Revisar choque con jugador
        Rectangle rEnemy = new Rectangle(x, y, width, height);
        Rectangle rPlayer = new Rectangle(player.getX(), player.getY(),
                player.getWidth(), player.getHeight());

        boolean colliding = rEnemy.intersects(rPlayer);

        if (colliding && !touchingPlayer) {
            player.takeDamage(damage);
            if (player.getX() < x) {
                player.knockBack(-pushBack);
            } else {
                player.knockBack(pushBack);
            }
            touchingPlayer = true; // marca que ya se registró el golpe
        } else if (!colliding) {
            touchingPlayer = false; // cuando se separan, se puede volver a golpear
        }
        // --- NUEVO: si el jugador está atacando y colisiona ---
        if (colliding && player.isAttacking()) {
            takeDamage(1);  // o el daño que haga el jugador
        }

    }

    /** Dibuja el enemigo como un rectángulo rojo */
    public void draw(Graphics g, int cameraX) {
        if (!alive) return;
        g.setColor(Color.RED);
        g.fillRect(x - cameraX, y, width, height);
    }

    // --- Getters básicos por si los necesitas ---
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void takeDamage(int dmg) {
        if (!alive) return;
        health -= dmg;
        if (health <= 0) {
            alive = false;
        }
    }

}
