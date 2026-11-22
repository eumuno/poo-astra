package astra.excecoes;

/**
 * Erro no processamento de um pagamento, por exemplo código Pix expirado
 * ou cartão vencido. A mensagem é exibida ao cliente na tela de pagamento.
 */
public class PagamentoException extends Exception {

    /**
     * Cria a exceção com a mensagem que explica por que o pagamento falhou.
     *
     * @param mensagem motivo da falha
     */
    public PagamentoException(String mensagem) {
        super(mensagem);
    }
}
