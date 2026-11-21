package astra.servico;

import astra.excecoes.AssentoIndisponivelException;
import astra.excecoes.EstoqueInsuficienteException;
import astra.excecoes.IngressosEsgotadosException;
import astra.excecoes.PagamentoException;
import astra.excecoes.PersistenciaException;
import astra.modelo.*;
import astra.servico.pagamento.FormaPagamento;

import java.util.List;
import java.util.Set;

/**
 * Tudo o que a área do CLIENTE pode fazer, e nada além disso (I de SOLID).
 *
 * <p>Antes a TelaCliente recebia o ProdutoService inteiro e "enxergava" o cadastrarProduto, que é
 * coisa de administrador. Agora a tela conhece só esta interface; ela não importa nenhuma classe
 * de persistência.</p>
 */
public interface CatalogoCliente {

    /**
     * Sessões que o cliente pode comprar agora (agendadas, que ainda não começaram, de filme ativo).
     *
     * @return as sessões à venda
     * @throws PersistenciaException se o banco falhar
     */
    List<Sessao> listarSessoesDisponiveis() throws PersistenciaException;

    /**
     * Produtos da bomboniere, com o estoque atual.
     *
     * @return os produtos cadastrados
     * @throws PersistenciaException se o banco falhar
     */
    List<Produto> listarProdutos() throws PersistenciaException;

    /**
     * Abre um pedido novo e vazio para o cliente.
     *
     * @param cliente dono do pedido
     * @return o pedido aberto
     */
    Pedido iniciarPedido(Cliente cliente);

    /**
     * Coloca um ingresso no pedido.
     *
     * @param pedido        pedido aberto
     * @param sessao        sessão escolhida
     * @param numeroAssento número da poltrona
     * @param tipo          inteira ou meia
     * @return o ingresso criado
     * @throws AssentoIndisponivelException se a poltrona está ocupada, não existe ou já está no pedido
     * @throws IngressosEsgotadosException  se a sessão não tem mais nenhuma poltrona livre
     */
    Ingresso venderIngresso(Pedido pedido, Sessao sessao, int numeroAssento, Ingresso.TipoIngresso tipo)
            throws AssentoIndisponivelException, IngressosEsgotadosException;

    /**
     * Consulta no banco quais poltronas da sessão já foram vendidas.
     *
     * @param sessao a sessão consultada
     * @return os números das poltronas vendidas
     * @throws PersistenciaException se o banco falhar
     */
    Set<Integer> consultarAssentosOcupados(Sessao sessao) throws PersistenciaException;

    /**
     * Coloca produtos da bomboniere no pedido.
     *
     * @param pedido     pedido aberto
     * @param produto    produto escolhido
     * @param quantidade unidades (pelo menos 1)
     */
    void adicionarProduto(Pedido pedido, Produto produto, int quantidade);

    /**
     * Troca a quantidade de um produto que já está no carrinho.
     *
     * @param pedido         pedido aberto
     * @param item           item do carrinho que será alterado
     * @param novaQuantidade nova quantidade (pelo menos 1)
     * @return o item novo, no lugar do antigo
     * @throws EstoqueInsuficienteException se não houver estoque para a quantidade pedida
     * @throws PersistenciaException        se o banco falhar
     */
    ItemPedido alterarQuantidade(Pedido pedido, ItemPedido item, int novaQuantidade)
            throws EstoqueInsuficienteException, PersistenciaException;

    /**
     * Formas de pagamento oferecidas.
     *
     * @return as formas de pagamento
     */
    List<FormaPagamento> getFormasPagamento();

    /**
     * Paga o pedido e grava a venda.
     *
     * @param pedido pedido aberto e não vazio
     * @param forma  forma de pagamento escolhida
     * @return o pagamento aprovado
     * @throws PagamentoException           se o pagamento for recusado
     * @throws AssentoIndisponivelException se alguma poltrona já foi vendida
     * @throws EstoqueInsuficienteException se algum produto não tem unidades suficientes
     * @throws PersistenciaException        se o banco falhar
     */
    Pagamento finalizarPedido(Pedido pedido, FormaPagamento forma)
            throws PagamentoException, AssentoIndisponivelException, EstoqueInsuficienteException, PersistenciaException;

    /**
     * As compras de um cliente, da mais recente para a mais antiga.
     *
     * @param cliente o cliente
     * @return os pedidos do cliente
     * @throws PersistenciaException se o banco falhar
     */
    List<Pedido> listarPedidosDoCliente(Cliente cliente) throws PersistenciaException;
}