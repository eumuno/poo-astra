package astra.modelo;

public class Ingresso {

    // ATRIBUTOS
    private int id;
    private Filme filme;
    private Sessao sessao;
    private Assento assento;
    private Cliente cliente;
    private double valorReferencia = 15; //É uma simplificação professor :)
    private  TipoIngresso tipoIngresso;

    //CONSTRUTOR
    public Ingresso(int id, Filme filme, Sessao sessao, Assento assento, Cliente cliente, TipoIngresso tipoIngresso) {

        this.id = id;
        this.filme = filme;
        this.sessao = sessao;
        this.assento = assento;
        this.cliente = cliente;
        this.tipoIngresso = tipoIngresso;
    }

    // ENUM
    public enum TipoIngresso {
        INTEIRA,
        MEIA,
        CORTESIA
    }

    // MÉTODOS
    public int getId() {
        return id;
    }

    public Filme getFilme() {
        return filme;
    }

    public Sessao getSessao() {
        return sessao;
    }

    public Assento getAssento() {
        return assento;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public TipoIngresso getTipoIngresso() {
        return tipoIngresso;
    }

    public double getValorReferencia() {
        return valorReferencia;
    }

    public double getPrecoIngresso(){
        switch(tipoIngresso){
            case INTEIRA:
                return valorReferencia;

            case MEIA:
                return valorReferencia / 2;

            case CORTESIA:
            return 0;

            default:
                throw new IllegalStateException("Tipo de ingresso inválido!");
        }
    }

    public String gerarResumoIngresso(){
        return "ID: " + id +
                "\nFilme: " + filme.getTitulo() +
                "\nTipo de ingresso: " + tipoIngresso +
                "\nValor pago: R$ " + getPrecoIngresso();
    }

}
