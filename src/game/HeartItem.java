package game;

import java.awt.Image;
import java.io.IOException;
import javax.imageio.ImageIO;

public class HeartItem extends Item {

    public HeartItem() {
        super(
                "Fragmento de Corazón",
                "Consumible",
                "Restaura parte de la salud perdida.",
                cargarIcono(), //  metodo estático que devuelve el ícono
                Rareza.LEGENDARIO
        );
    }

    private static Image cargarIcono() {
        try {
            return ImageIO.read(HeartItem.class.getResourceAsStream("/items/heart.png"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println(" No se pudo cargar el icono del corazón: " + e.getMessage());
            return null;
        }
    }


    @Override
    public void usar(Player player) {
        if (!fueConsumido()) {
            System.out.println("Usaste un Fragmento de Corazon. Tu salud aumenta.");
            // player.curar(20); // si tienes lógica de curación
            marcarConsumido(); // se marca como usado
        } else {
            System.out.println("Este ítem ya fue consumido.");
        }
    }
}
