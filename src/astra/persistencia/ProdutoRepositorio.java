package astra.persistencia;

import astra.excecoes.PersistenciaException;
import astra.modelo.Produto;

import java.util.List;
import java.util.Optional;

/** Contrato de acesso aos produtos da bomboniere. */
public interface ProdutoRepositorio {

    /**
     * Grava o produto. Id 0 = produto novo (INSERT, o banco gera o id);
     * id maior que 0 = atualiza o existente (por exemplo, o estoque depois de uma venda).
     *
     * @return o produto gravado, já com o id definitivo
     */
    Produto salvar(Produto produto) throws PersistenciaException;

    /** Todos os produtos, em ordem de cadastro. */
    List<Produto> listarTodos() throws PersistenciaException;

    /** O produto com esse id, ou vazio se não existir. */
    Optional<Produto> buscarPorId(int id) throws PersistenciaException;
}
