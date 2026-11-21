package astra.persistencia;

import astra.excecoes.PersistenciaException;
import astra.modelo.Filme;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de acesso aos filmes. Os serviços dependem desta interface e não do JDBC
 * (princípio da inversão de dependência), então dá para trocar a implementação
 * (JDBC, em memória para testes...) sem mexer nos serviços.
 */
public interface FilmeRepositorio {

    /**
     * Grava o filme. Se o id for 0, é um filme novo (INSERT) e o banco gera o id;
     * se o id for maior que 0, atualiza o filme existente.
     *
     * @param filme o filme a gravar
     * @return o filme gravado, já com o id definitivo
     * @throws PersistenciaException se o filme a atualizar não existir ou o banco falhar
     */
    Filme salvar(Filme filme) throws PersistenciaException;

    /**
     * Lista todos os filmes, em ordem de cadastro.
     *
     * @return os filmes, ativos e inativos
     * @throws PersistenciaException se o banco falhar
     */
    List<Filme> listarTodos() throws PersistenciaException;

    /**
     * Busca um filme pelo id.
     *
     * @param id identificador do filme
     * @return o filme, ou vazio se não existir
     * @throws PersistenciaException se o banco falhar
     */
    Optional<Filme> buscarPorId(int id) throws PersistenciaException;
}