import java.time.LocalDateTime;

public class reservaIngresso {

    // ATRIBUTOS
    private int id;
    private Cliente cliente;
    private Ingresso ingressos;
    private LocalDateTime dataReserva;
    private statusReserva status;

    // ENUM
    public enum statusReserva {
        PENDENTE,
        CONFIRMADA,
        CANCELADA
    }

    // MÉTODOS
    public void adicionarIngresso(Ingresso ingresso)
    public void cancelar()
    public double calcularTotal()
    public void confirmar()

}
