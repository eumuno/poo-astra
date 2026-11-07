package astra.persistencia;

import astra.excecoes.PersistenciaException;
import astra.modelo.Sessao;

import java.util.List;
import java.util.Optional;

/** Contrato de acesso às sessões (que carregam o filme, a sala com seus assentos e os assentos já ocupados). */
public interface SessaoRepositorio {

    /**
     * Grava uma sessão nova (id 0). O filme da sessão já precisa estar gravado
     * (id maior que 0) e a sala precisa existir.
     *
     * @return a sessão gravada, já com o id definitivo
     * @throws PersistenciaException se a sala não existir ou se ela já tiver uma sessão nesse horário
     */
    Sessao salvar(Sessao sessao) throws PersistenciaException;

    /** Todas as sessões, em ordem de data e hora. */
    List<Sessao> listarTodos() throws PersistenciaException;

    /** A sessão com esse id, ou vazia se não existir. */
    Optional<Sessao> buscarPorId(int id) throws PersistenciaException;
}
