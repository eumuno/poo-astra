package astra.persistencia;

import astra.excecoes.PersistenciaException;
import astra.modelo.RegistroDeAuditoria;

import java.util.List;

/**
 * Contrato de acesso à trilha de auditoria. A interface só permite <b>acrescentar</b> e <b>consultar</b>:
 * não existe operação para alterar ou apagar um registro.
 */
public interface AuditoriaRepositorio {

    /**
     * Acrescenta um registro à trilha, com a data e hora de agora.
     *
     * @param usuarioId id de quem fez a ação, ou {@code null} se não há usuário identificado
     * @param acao      o tipo da ação (obrigatório)
     * @param detalhe   o que foi feito (obrigatório)
     * @throws PersistenciaException se o banco falhar
     */
    void registrar(Integer usuarioId, String acao, String detalhe) throws PersistenciaException;

    /**
     * Lista os registros mais recentes, do mais novo para o mais antigo.
     *
     * @param limite quantos registros devolver (de 1 a 1000)
     * @return os registros
     * @throws PersistenciaException    se o banco falhar
     * @throws IllegalArgumentException se o limite estiver fora do intervalo
     */
    List<RegistroDeAuditoria> listarRecentes(int limite) throws PersistenciaException;
}