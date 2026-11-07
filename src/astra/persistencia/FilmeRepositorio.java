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
     * @return o filme gravado, já com o id definitivo
     */
    Filme salvar(Filme filme) throws PersistenciaException;

    /** Todos os filmes, em ordem de cadastro. */
    List<Filme> listarTodos() throws PersistenciaException;

    /** O filme com esse id, ou vazio se não existir. */
    Optional<Filme> buscarPorId(int id) throws PersistenciaException;
}
