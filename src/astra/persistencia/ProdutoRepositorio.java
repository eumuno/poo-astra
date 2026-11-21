package astra.persistencia;

import astra.excecoes.PersistenciaException;
import astra.modelo.Produto;

import java.util.List;
import java.util.Optional;

/** Contrato de acesso aos produtos da bomboniere. */
public interface ProdutoRepositorio {

    /**
     * Grava o produto. Id 0 = produto novo (INSERT, o banco gera o id);
     * id maior que 0 = atualiza o existente (todos os campos, inclusive o estoque, ficam como
     * estão no objeto recebido).
     *
     * Cuidado: NÃO use para baixar estoque de uma venda. A baixa é feita dentro de
     * PedidoRepositorioJdbc.salvar(); e se você gravar um Produto carregado há tempos, o estoque
     * antigo que está no objeto sobrescreve o valor atual do banco.
     *
     * @return o produto gravado, já com o id definitivo
     */
    Produto salvar(Produto produto) throws PersistenciaException;

    /** Todos os produtos, em ordem de cadastro. */
    List<Produto> listarTodos() throws PersistenciaException;

    /** O produto com esse id, ou vazio se não existir. */
    Optional<Produto> buscarPorId(int id) throws PersistenciaException;

    /**
     * Atualiza só os dados cadastrais do produto (nome, descrição, preço e categoria).
     * O estoque NÃO é alterado: ele só muda por {@link #adicionarEstoque(int, int)} ou por uma venda.
     * Assim, editar um produto com um objeto carregado há tempos não desfaz baixas ou reposições recentes.
     *
     * @param produto produto com id maior que 0 e os dados novos
     * @return o produto como ficou no banco, com o estoque atual
     * @throws PersistenciaException se o produto não existir ou o banco falhar
     */
    Produto atualizarDados(Produto produto) throws PersistenciaException;

    /**
     * Soma unidades ao estoque do produto em uma única instrução ({@code estoque = estoque + ?}),
     * sem ler antes: duas reposições ou uma reposição durante uma venda não se sobrescrevem.
     *
     * @param produtoId  id do produto
     * @param quantidade unidades a somar (maior que zero)
     * @return o produto com o estoque já atualizado
     * @throws PersistenciaException se o produto não existir ou o banco falhar
     */
    Produto adicionarEstoque(int produtoId, int quantidade) throws PersistenciaException;
}