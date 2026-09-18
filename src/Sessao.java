import java.time.LocalDateTime;

public class Sessao {

    // ATRIBUTOS
    private int id;
    private Filme filme;
    private Sala sala;
    private LocalDateTime dataHora;
    private double preco;
    private statusSessao status;

    // ENUM
    public enum statusSessao {
        AGENDADA,
        ENCERRADA,
        CANCELADA
    }

    // MÉTODOS
    public boolean estaDisponivel()
    public boolean possuiVagas()
    public void cancelar()
    public double calcularPreco(...)

}