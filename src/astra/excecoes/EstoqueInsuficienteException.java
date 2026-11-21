package astra.excecoes;

/**
 * Não há unidades suficientes de um produto para atender o pedido.
 *
 * Guarda o nome do produto, quanto ainda resta e quanto foi pedido como DADOS,
 * para a tela montar a mensagem certa ("Restam 2 de Pipoca...") sem depender do texto.
 */
public class EstoqueInsuficienteException extends Exception {

    private final String nomeProduto;
    private final int disponivel;
    private final int solicitado;

    public EstoqueInsuficienteException(String nomeProduto, int disponivel, int solicitado) {
        super("Estoque insuficiente de \"" + nomeProduto + "\": restam " + disponivel
                + " e o pedido pede " + solicitado + ".");
        this.nomeProduto = nomeProduto;
        this.disponivel = disponivel;
        this.solicitado = solicitado;
    }

    /** @return nome do produto que faltou */
    public String getNomeProduto() { return nomeProduto; }

    /** @return quantas unidades ainda restam no estoque */
    public int getDisponivel() { return disponivel; }

    /** @return quantas unidades o pedido pedia */
    public int getSolicitado() { return solicitado; }
}
