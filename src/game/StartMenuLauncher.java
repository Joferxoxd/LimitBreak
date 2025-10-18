package game;

import javax.swing.*;
import java.awt.*;

public class StartMenuLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("LimitBreak");
            StartMenuPanel menu = new StartMenuPanel();

            // Establece el tamaño preferido del panel
            menu.setPreferredSize(new Dimension(1280, 720));

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false); // Bloquea redimensionamiento
            frame.setLayout(new BorderLayout());
            frame.add(menu, BorderLayout.CENTER);
            frame.pack(); // Ajusta el frame al tamaño del panel
            frame.setLocationRelativeTo(null); // Centra la ventana
            frame.setVisible(true);

            menu.requestFocusInWindow(); // Asegura que reciba teclas
        });
    }
}
