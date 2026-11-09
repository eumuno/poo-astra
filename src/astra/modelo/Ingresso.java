package astra.modelo;

public class Ingresso {

    // ATRIBUTOS
    private int id;
    private Filme filme;
    private Sessao sessao;
    private Assento assento;
    private Cliente cliente;
    private double valorReferencia = 15; //É uma simplificação professor :)
    private TipoIngresso tipoIngresso;
    private final PoliticaPreco politicaPreco; // NOVO: quem sabe calcular o preço

    //CONSTRUTOR
    public Ingresso(int id, Filme filme, Sessao sessao, Assento assento, Cliente cliente, TipoIngresso tipoIngresso) {

        this.id = id;
        this.filme = filme;
        this.sessao = sessao;
        this.assento = assento;
        this.cliente = cliente;
        this.tipoIngresso = tipoIngresso;
        this.politicaPreco = tipoIngresso.getPoliticaPreco(); // NOVO
    }

    // ENUM
    // MUDOU: cada tipo de ingresso aponta para a sua política de preço.
    // Tipo novo = uma classe nova que implementa PoliticaPreco + uma linha aqui.
    public enum TipoIngresso {
        INTEIRA(new PrecoInteira()),
        MEIA(new PrecoMeia()),
        CORTESIA(new PrecoCortesia());

        private final PoliticaPreco politicaPreco;

        TipoIngresso(PoliticaPreco politicaPreco) {
            this.politicaPreco = politicaPreco;
        }

        public PoliticaPreco getPoliticaPreco() {
            return politicaPreco;
        }
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

    // MUDOU: antes era um switch por tipo; agora pede para a política calcular (polimorfismo)
    public double getPrecoIngresso(){
        return politicaPreco.calcular(valorReferencia);
    }

    public String gerarResumoIngresso(){
        return "ID: " + id +
                "\nFilme: " + filme.getTitulo() +
                "\nTipo de ingresso: " + tipoIngresso +
                "\nValor pago: R$ " + getPrecoIngresso();
    }

}