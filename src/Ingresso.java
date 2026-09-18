public class Ingresso {

    // ATRIBUTOS
    private int id;
    private Sessao sessao;
    private Assento assento;
    private Cliente cliente;
    private StatusIngresso status;
    private double valor;

    // ENUM
    public enum StatusIngresso {
        RESERVADO,
        PAGO,
        CANCELADO,
        UTILIZADO
    }

    // MÉTODOS
    public void cancelar()
    public void confirmarPagamento()
    public void utilizar()
    public boolean estaValido()

}
