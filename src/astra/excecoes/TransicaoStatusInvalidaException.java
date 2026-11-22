package astra.excecoes;

/**
 * Tentativa de mudar o status de algo que não pode mais mudar, por exemplo
 * processar de novo um pagamento que já foi aprovado ou recusado.
 */
public class TransicaoStatusInvalidaException extends PagamentoException {

    /**
     * Cria a exceção com a mensagem que explica qual transição foi negada.
     *
     * @param mensagem descrição da transição inválida
     */
    public TransicaoStatusInvalidaException(String mensagem) {
        super(mensagem);
    }
}
