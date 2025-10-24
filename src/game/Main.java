package game;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StartMenuLauncher launcher = new StartMenuLauncher();
            launcher.pack(); // Ajusta el tamaño según el panel
        });
    }
}
