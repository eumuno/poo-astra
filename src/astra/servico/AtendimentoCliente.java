package astra.servico;

import astra.excecoes.AssentoIndisponivelException;
import astra.excecoes.EstoqueInsuficienteException;
import astra.excecoes.IngressosEsgotadosException;
import astra.excecoes.PagamentoException;
import astra.excecoes.PersistenciaException;
import astra.modelo.Cliente;
import astra.modelo.Ingresso;
import astra.modelo.Pagamento;
import astra.modelo.Pedido;
import astra.modelo.Produto;
import astra.modelo.Sessao;
import astra.servico.pagamento.FormaPagamento;

import java.util.List;
import java.util.Set;

/**
 * Fachada da área do cliente: reúne, atrás de {@link CatalogoCliente}, apenas os casos de uso
 * que um cliente pode usar, escolhidos entre os serviços da aplicação.
 *
 * <p>Não tem regra própria: cada método repassa a chamada ao serviço responsável. Quem a usa
 * (a TelaCliente) não enxerga operações de administrador como cadastrar produto ou cancelar sessão.</p>
 */
public class AtendimentoCliente implements CatalogoCliente {

    private final SessaoService sessoes;
    private final ProdutoService produtos;
    private final VendaService vendas;
    private final PedidoService pedidos;

    /**
     * Cria a fachada sobre os serviços que ela repassa.
     *
     * @param sessoes  consulta de sessões
     * @param produtos consulta de produtos
     * @param vendas   montagem e pagamento do pedido
     * @param pedidos  histórico de compras
     */
    public AtendimentoCliente(SessaoService sessoes, ProdutoService produtos, VendaService vendas, PedidoService pedidos) {
        if (sessoes == null || produtos == null || vendas == null || pedidos == null) {
            throw new IllegalArgumentException("Todos os serviços da área do cliente são obrigatórios.");
        }
        this.sessoes = sessoes;
        this.produtos = produtos;
        this.vendas = vendas;
        this.pedidos = pedidos;
    }

    @Override
    public List<Sessao> listarSessoesDisponiveis() throws PersistenciaException {
        return sessoes.listarSessoesDisponiveis();
    }

    @Override
    public List<Produto> listarProdutos() throws PersistenciaException {
        return produtos.listarProdutos();
    }

    @Override
    public Pedido iniciarPedido(Cliente cliente) {
        return vendas.iniciarPedido(cliente);
    }

    @Override
    public Ingresso venderIngresso(Pedido pedido, Sessao sessao, int numeroAssento, Ingresso.TipoIngresso tipo)
            throws AssentoIndisponivelException, IngressosEsgotadosException {
        return vendas.venderIngresso(pedido, sessao, numeroAssento, tipo);
    }

    @Override
    public Set<Integer> consultarAssentosOcupados(Sessao sessao) throws PersistenciaException {
        return vendas.consultarAssentosOcupados(sessao);
    }

    @Override
    public void adicionarProduto(Pedido pedido, Produto produto, int quantidade) {
        vendas.adicionarProduto(pedido, produto, quantidade);
    }

    @Override
    public List<FormaPagamento> getFormasPagamento() {
        return vendas.getFormasPagamento();
    }

    @Override
    public Pagamento finalizarPedido(Pedido pedido, FormaPagamento forma)
            throws PagamentoException, AssentoIndisponivelException, EstoqueInsuficienteException, PersistenciaException {
        return vendas.finalizarPedido(pedido, forma);
    }

    @Override
    public List<Pedido> listarPedidosDoCliente(Cliente cliente) throws PersistenciaException {
        return pedidos.listarPorCliente(cliente);
    }
}