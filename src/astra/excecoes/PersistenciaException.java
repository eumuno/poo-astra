package astra.excecoes;

/**
 * Erro ao ler ou gravar no banco de dados.
 *
 * Embrulha a SQLException (detalhe técnico do JDBC) para que as camadas de
 * serviço e de tela não precisem conhecer o java.sql. A causa original fica
 * guardada em getCause() para depuração.
 */
public class PersistenciaException extends Exception {

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
