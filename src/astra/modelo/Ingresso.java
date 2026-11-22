package astra.modelo;

/**
 * Ingresso de um cliente para um assento de uma sessão.
 *
 * <p>O preço final não é decidido pela própria classe: ela guarda o valor de referência
 * (o preço base da {@link Sessao}) e delega o cálculo à {@link PoliticaPreco} do seu
 * {@link TipoIngresso}. Assim, inteira, meia e cortesia são tratadas do mesmo jeito
 * (polimorfismo).</p>
 */
public class Ingresso {

    // ATRIBUTOS
    /** Identificador do ingresso (0 se ainda não foi gravado no banco). */
    private int id;
    /** Filme a que o ingresso dá acesso. */
    private Filme filme;
    /** Sessão para a qual o ingresso vale. */
    private Sessao sessao;
    /** Assento reservado pelo ingresso. */
    private Assento assento;
    /** Cliente dono do ingresso. */
    private Cliente cliente;
    /** Preço do ingresso inteiro, copiado da sessão ({@link Sessao#getPrecoBase()}). */
    private final double valorReferencia;
    /** Tipo do ingresso (inteira, meia ou cortesia). */
    private TipoIngresso tipoIngresso;
    /** Política usada para calcular o preço, definida pelo tipo do ingresso. */
    private final PoliticaPreco politicaPreco;

    //CONSTRUTOR

    /**
     * Cria um ingresso. O valor de referência vem do preço base da sessão e a
     * política de preço vem do tipo informado.
     *
     * @param id           identificador (0 se ainda não foi gravado no banco)
     * @param filme        filme do ingresso
     * @param sessao       sessão do ingresso; não pode ser {@code null}
     * @param assento      assento reservado
     * @param cliente      cliente dono do ingresso
     * @param tipoIngresso tipo do ingresso, que define o cálculo do preço; não pode ser {@code null}
     * @throws IllegalArgumentException se a sessão for {@code null}
     * @throws NullPointerException     se o tipo do ingresso for {@code null}
     */
    public Ingresso(int id, Filme filme, Sessao sessao, Assento assento, Cliente cliente, TipoIngresso tipoIngresso) {

        if (sessao == null) {
            throw new IllegalArgumentException("O ingresso precisa de uma sessão!");
        }
        this.id = id;
        this.filme = filme;
        this.sessao = sessao;
        this.assento = assento;
        this.cliente = cliente;
        this.tipoIngresso = tipoIngresso;
        this.politicaPreco = tipoIngresso.getPoliticaPreco();
        this.valorReferencia = sessao.getPrecoBase();
    }

    // ENUM

    /**
     * Tipos de ingresso vendidos. Cada tipo aponta para a sua {@link PoliticaPreco}.
     * Para criar um tipo novo, basta uma classe que implemente {@link PoliticaPreco}
     * e uma constante aqui.
     */
    public enum TipoIngresso {
        /** Paga o valor integral ({@link PrecoInteira}). */
        INTEIRA(new PrecoInteira()),
        /** Paga metade do valor ({@link PrecoMeia}). */
        MEIA(new PrecoMeia()),
        /** Não paga nada ({@link PrecoCortesia}). */
        CORTESIA(new PrecoCortesia());

        /** Política de preço deste tipo. */
        private final PoliticaPreco politicaPreco;

        /**
         * Associa o tipo à sua política de preço.
         *
         * @param politicaPreco regra que calcula o preço deste tipo
         */
        TipoIngresso(PoliticaPreco politicaPreco) {
            this.politicaPreco = politicaPreco;
        }

        /**
         * Devolve a política de preço deste tipo.
         *
         * @return a {@link PoliticaPreco} usada para calcular o valor do ingresso
         */
        public PoliticaPreco getPoliticaPreco() {
            return politicaPreco;
        }
    }

    // MÉTODOS

    /**
     * Devolve o identificador do ingresso.
     *
     * @return o id (0 se ainda não foi gravado no banco)
     */
    public int getId() {
        return id;
    }

    /**
     * Devolve o filme do ingresso.
     *
     * @return o filme
     */
    public Filme getFilme() {
        return filme;
    }

    /**
     * Devolve a sessão do ingresso.
     *
     * @return a sessão
     */
    public Sessao getSessao() {
        return sessao;
    }

    /**
     * Devolve o assento reservado.
     *
     * @return o assento
     */
    public Assento getAssento() {
        return assento;
    }

    /**
     * Devolve o cliente dono do ingresso.
     *
     * @return o cliente
     */
    public Cliente getCliente() {
        return cliente;
    }

    /**
     * Devolve o tipo do ingresso.
     *
     * @return inteira, meia ou cortesia
     */
    public TipoIngresso getTipoIngresso() {
        return tipoIngresso;
    }

    /**
     * Devolve o preço do ingresso inteiro usado como base do cálculo.
     *
     * @return o valor de referência, em reais
     */
    public double getValorReferencia() {
        return valorReferencia;
    }

    /**
     * Calcula o preço deste ingresso. O cálculo é delegado à {@link PoliticaPreco}
     * do tipo, aplicada ao valor de referência.
     *
     * @return o valor a pagar, em reais
     */
    public double getPrecoIngresso(){
        return politicaPreco.calcular(valorReferencia);
    }

    /**
     * Monta um texto com o resumo do ingresso: id, título do filme, tipo e valor pago.
     *
     * @return o resumo em várias linhas
     */
    public String gerarResumoIngresso(){
        return "ID: " + id +
                "\nFilme: " + filme.getTitulo() +
                "\nTipo de ingresso: " + tipoIngresso +
                "\nValor pago: R$ " + getPrecoIngresso();
    }

}