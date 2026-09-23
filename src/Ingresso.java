public class Ingresso {

    // ATRIBUTOS
    private int id;
    private Sessao sessao;
    private Assento assento;
    private Cliente cliente;
    private statusIngresso status;
    private double valor;

    // ENUM
    public enum statusIngresso {
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
