package astra.excecoes;

/**
 * Erro ao ler ou gravar no banco de dados.
 *
 * Embrulha a SQLException (detalhe técnico do JDBC) para que as camadas de
 * serviço e de tela não precisem conhecer o java.sql. A causa original fica
 * guardada em getCause() para depuração.
 */
public class PersistenciaException extends Exception {

    /**
     * Cria a exceção só com a mensagem, para erros que não vêm de uma SQLException.
     *
     * @param mensagem descrição do erro
     */
    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    /**
     * Cria a exceção guardando o erro original (normalmente a SQLException).
     *
     * @param mensagem descrição do erro, em termos do sistema
     * @param causa erro original, disponível em {@link #getCause()}
     */
    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
