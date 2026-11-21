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
     * @param sessao a sessão a gravar
     * @return a sessão gravada, já com o id definitivo
     * @throws PersistenciaException se a sala não existir ou se ela já tiver uma sessão nesse horário
     */
    Sessao salvar(Sessao sessao) throws PersistenciaException;

    /**
     * Atualiza uma sessão que já existe (filme, sala, data e hora, status e preço base).
     * A regra de quando uma sessão pode ser alterada fica no serviço; aqui só se grava.
     *
     * @param sessao sessão com id maior que 0 e os dados novos
     * @return a mesma sessão, já gravada
     * @throws PersistenciaException se a sessão ou a sala não existirem, se a sala já tiver outra sessão no horário
     *                               ou se o banco falhar
     */
    Sessao atualizar(Sessao sessao) throws PersistenciaException;

    /**
     * Lista todas as sessões, em ordem de data e hora.
     *
     * @return as sessões, inclusive canceladas e já realizadas
     * @throws PersistenciaException se o banco falhar
     */
    List<Sessao> listarTodos() throws PersistenciaException;

    /**
     * Busca uma sessão pelo id.
     *
     * @param id identificador da sessão
     * @return a sessão, ou vazia se não existir
     * @throws PersistenciaException se o banco falhar
     */
    Optional<Sessao> buscarPorId(int id) throws PersistenciaException;
}