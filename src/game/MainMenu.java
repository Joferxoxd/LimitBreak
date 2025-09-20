package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
public class MainMenu extends JFrame {
    private void createButtons() {
        // ==== PANEL DE BOTONES PRINCIPALES ====
        JPanel buttons = new JPanel(new GridLayout(2, 1, 10, 10));

        JButton playBtn = new JButton("Jugar");
        JButton exitBtn = new JButton("Salir");

        // Acción del botón Jugar: cierra el menú y abre el juego
        playBtn.addActionListener((ActionEvent e) -> {
            dispose();
            SwingUtilities.invokeLater(Game::new);
        });

        // Acción del botón Salir
        exitBtn.addActionListener(e -> System.exit(0));

        buttons.add(playBtn);
        buttons.add(exitBtn);

        // Se agrega al centro del BorderLayout
        add(buttons, BorderLayout.CENTER);
    }
    public MainMenu() {
        setTitle("LimitBreak – Menú");
        setSize(600, 400);        // un poco más grande para que quepa todo
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        // ==== TÍTULO ARRIBA ====
        JLabel title = new JLabel("LimitBreak", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 32));
        add(title, BorderLayout.NORTH);
        // ==== LLAMAMOS AL PANEL DE BOTONES PRINCIPALES ====
        createButtons();
        // ==== PANEL DE CONFIGURACIÓN ABAJO ====
        JPanel settingsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        // Resolución
        JLabel resolutionLabel = new JLabel("Resolución:");
        String[] resolutions = {"640x360", "960x540", "1280x720"};
        JComboBox<String> resolutionBox = new JComboBox<>(resolutions);
        resolutionBox.addActionListener(e -> {
            String selected = (String) resolutionBox.getSelectedItem();
            if (selected != null) {
                String[] parts = selected.split("x");
                int w = Integer.parseInt(parts[0]);
                int h = Integer.parseInt(parts[1]);
                setSize(w, h);
                setLocationRelativeTo(null);
            }
        });
        // Volumen
        JLabel volumeLabel = new JLabel("Volumen:");
        JSlider volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        // Aquí podrías conectar el slider con tu sistema de sonido
        // Idioma
        JLabel languageLabel = new JLabel("Idioma:");
        String[] languages = {"Español", "Inglés"};
        JComboBox<String> languageBox = new JComboBox<>(languages);
        // Aquí podrías cambiar textos según idioma elegido
        // Versión
        JLabel versionLabel = new JLabel("Versión: 1.0.0");
        // Añadimos los componentes al panel
        settingsPanel.add(resolutionLabel);
        settingsPanel.add(resolutionBox);
        settingsPanel.add(volumeLabel);
        settingsPanel.add(volumeSlider);
        settingsPanel.add(languageLabel);
        settingsPanel.add(languageBox);
        settingsPanel.add(new JLabel());  // espacio vacío
        settingsPanel.add(versionLabel);
        // Se agrega al sur del BorderLayout
        add(settingsPanel, BorderLayout.SOUTH);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}