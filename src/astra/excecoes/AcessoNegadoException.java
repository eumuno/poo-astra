package astra.excecoes;

/**
 * A operação foi recusada por motivo de segurança: quem pediu não tem permissão (por exemplo, não é um
 * administrador de verdade) ou fez tentativas demais de login.
 *
 * <p>É uma exceção não checada, como as outras violações de regra do sistema. A mensagem pode ser mostrada ao
 * usuário: ela diz o que aconteceu sem revelar detalhes internos.</p>
 */
public class AcessoNegadoException extends RuntimeException {

    /**
     * Cria a exceção com a mensagem que será mostrada ao usuário.
     *
     * @param mensagem o que aconteceu, em linguagem simples
     */
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}