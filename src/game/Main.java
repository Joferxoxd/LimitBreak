package game;

public class Main {
    public static void main(String[] args) {
        // Esto es clave para que se muestre la ventana
        javax.swing.SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}
