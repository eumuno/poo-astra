package astra.servico;

import astra.excecoes.AssentoIndisponivelException;
import astra.excecoes.EstoqueInsuficienteException;
import astra.excecoes.PagamentoException;
import astra.excecoes.PersistenciaException;
import astra.modelo.*;
import astra.servico.pagamento.FormaPagamento;

import java.util.List;

/**
 * Tudo o que a área do CLIENTE pode fazer, e nada além disso (I de SOLID).
 * Antes a TelaCliente recebia o ProdutoService inteiro e "enxergava"
 * o cadastrarProduto, que é coisa de administrador.
 */
public interface CatalogoCliente {

    List<Sessao> listarSessoesDisponiveis() throws PersistenciaException;

    List<Produto> listarProdutos() throws PersistenciaException;

    Pedido iniciarPedido(Cliente cliente);

    Ingresso venderIngresso(Pedido pedido, Sessao sessao, int numeroAssento, Ingresso.TipoIngresso tipo)
            throws AssentoIndisponivelException;

    void adicionarProduto(Pedido pedido, Produto produto, int quantidade);

    List<FormaPagamento> getFormasPagamento();

    Pagamento finalizarPedido(Pedido pedido, FormaPagamento forma)
            throws PagamentoException, AssentoIndisponivelException, EstoqueInsuficienteException, PersistenciaException;
}