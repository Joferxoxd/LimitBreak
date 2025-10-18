package game;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("LimitBreak");
            StartMenuPanel menu = new StartMenuPanel();

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1280, 720); // Resolución inicial deseada
            frame.setLocationRelativeTo(null); // Centrar en pantalla
            frame.setResizable(false); // Bloquear redimensionamiento
            frame.setLayout(new BorderLayout()); // Permite que el panel se estire
            frame.add(menu, BorderLayout.CENTER); // Panel ocupa todo el espacio
            frame.setVisible(true);

            menu.requestFocusInWindow(); // Asegura que reciba las teclas
        });
    }
}
