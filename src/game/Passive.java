package game;

public class Passive {
    private String nombre;
    private String tipo;
    private String descripcion;
    private String estado;
    private String efecto;

    public Passive(String nombre, String tipo, String descripcion, String estado, String efecto) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.estado = estado;
        this.efecto = efecto;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public String getEfecto() {
        return efecto;
    }

    // ✅ ESTE MÉTODO FALTABA
    public void activar() {
        System.out.println("Activando pasiva: " + nombre);
        // Aquí puedes poner la lógica real de activación
        // Por ejemplo: cambiar estado, aplicar buff, etc.
        estado = "Activa";
    }
}
