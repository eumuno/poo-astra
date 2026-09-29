package astra.excecoes;

public class TransicaoStatusInvalidaException extends PagamentoException {
    public TransicaoStatusInvalidaException(String mensagem) {
        super(mensagem);
    }
}