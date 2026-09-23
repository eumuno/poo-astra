import java.time.LocalDateTime;

public class Pagamento {

    // ATRIBUTOS
    private double valor;
    private LocalDateTime data;
    private statusPagamento status;

    //ENUM
    public enum statusPagamento {
        PENDENTE,
        APROVADO,
        RECUSADO,
        CANCELADO
    }

    // MÉTODOS
    public void processarPagamento();

}
