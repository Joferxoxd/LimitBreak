package game;

import javax.swing.*;
import java.awt.*;

public class StartMenuLauncher extends JFrame {

    public StartMenuLauncher() {
        super("LimitBreak");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 720));
        pack(); // calcula el tamaño real basado en contenido
        setLocationRelativeTo(null);
        setVisible(true);

        showMenu(); //  inicia con el menu principal
    }

    // Muestra el menu principal
    public void showMenu() {
        getContentPane().removeAll();
        getContentPane().add(new StartMenuPanel(this));
        revalidate();
        repaint();
    }

    // Inicia el juego
    public void startGame(Dimension size, boolean fullscreen) {
        getContentPane().removeAll();
        getContentPane().add(new Game(this, size, fullscreen));
        revalidate();
        repaint();
    }

    public void resumeGame(Game previousGame) {
        getContentPane().removeAll();
        getContentPane().add(previousGame);
        previousGame.requestFocus(); // ← importante para que las teclas funcionen
        revalidate();
        repaint();
    }


    // Muestra el menu de pausa
    public void showPause(Game currentGame) {
        getContentPane().removeAll();
        getContentPane().add(new PauseMenuPanel(this, currentGame));
        revalidate();
        repaint();
    }


    // Cierra el juego
    public void exitGame() {
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StartMenuLauncher launcher = new StartMenuLauncher();
            launcher.pack();
        });
    }
}
