package astra.servico;
import astra.excecoes.AssentoIndisponivelException;
import astra.excecoes.PagamentoException;
import astra.modelo.*;
import astra.servico.pagamento.FormaPagamento;
import java.util.List;

//Venda: pedido, ingresso, produto e pagamento.
//Regras estavam antes entre CinemaService e TelaCliente.

public class VendaService {

    private final List<FormaPagamento> formasPagamento;

    //Chegam prontas pelo construtor
    public VendaService(List<FormaPagamento> formasPagamento) {
        if (formasPagamento == null || formasPagamento.isEmpty()) {
            throw new IllegalArgumentException("Informe pelo menos uma forma de pagamento.");
        }
        this.formasPagamento = List.copyOf(formasPagamento);
    }

    public Pedido iniciarPedido(Cliente cliente) {
        //id 0: pedido ainda não gravado - id vem do banco quando tiver PedidoRepositorio
        return new Pedido(0, cliente);
    }

    public Ingresso venderIngresso (Pedido pedido, Sessao sessao, int numeroAssento, Ingresso.TipoIngresso tipo) throws AssentoIndisponivelException {
        if(sessao == null){
            throw new IllegalArgumentException("Você precisa escolhre uma sessão!");
        }
        if(!sessao.estaDisponivel()){
            throw new IllegalStateException("A sessão não está disponível para venda!");
        }
        if(!sessao.assentoEstaDisponivel(numeroAssento)){
            throw new AssentoIndisponivelException("O assento " + numeroAssento + " está ocupado ou não existe.");
        }

        Assento assento = sessao.getSala().buscarAssento(numeroAssento);
        Ingresso ingresso = new Ingresso(0, sessao.getFilme(), sessao, assento, pedido.getCliente(), tipo);
        pedido.adicionarIngresso(ingresso);
        sessao.ocuparAssento(numeroAssento);
        return ingresso;
    }

    public void adicionarProduto(Pedido pedido, Produto produto, int quantidade){
        if (produto == null){
            throw new IllegalArgumentException("Escolha um produto.");
        }
        pedido.adicionarItem(produto, quantidade);
    }

    public List<FormaPagamento> getFormasPagamento() {
        return formasPagamento;
    }
    //Cria o pagemento pela forma escolhida, processa e confirma o pedido, usa o tipo geral Pagamento
    public Pagamento finalizarPedido(Pedido pedido, FormaPagamento forma) throws PagamentoException {
        pedido.validarPodeSerPago();
        Pagamento pagamento = forma.criarPagamento(pedido.calcularTotal(), pedido.getCliente());
        pagamento.processarPagamento();
        pedido.confirmarPagamento();
        return pagamento;
    }
}
