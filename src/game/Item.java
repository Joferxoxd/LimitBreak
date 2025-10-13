package game;

import java.awt.Color;
import java.awt.Image;

public abstract class Item {

    // === Atributos privados (encapsulación) ===
    protected String nombre;
    protected String tipo;
    protected String descripcion;
    protected Image icon;
    protected Rareza rareza; // ✅ ahora está dentro de la clase
    private boolean consumido = false; // ✅ nuevo campo para controlar uso

    // === Constructor ===
    public Item(String nombre, String tipo, String descripcion, Image icon, Rareza rareza) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.icon = icon;
        this.rareza = rareza;
    }

    // === Getters ===
    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Image getIcon() {
        return icon;
    }

    public Rareza getRareza() {
        return rareza;
    }

    // === Lógica de consumo ===
    public boolean isConsumible() {
        return true; // por defecto, todos los ítems se consumen
    }

    public boolean fueConsumido() {
        return consumido;
    }

    public void marcarConsumido() {
        this.consumido = true;
    }

    // === Acción al usar el ítem ===
    public abstract void usar(Player player);
}
