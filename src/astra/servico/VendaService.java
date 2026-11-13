package astra.servico;
import astra.excecoes.AssentoIndisponivelException;
import astra.excecoes.EstoqueInsuficienteException;
import astra.excecoes.PagamentoException;
import astra.excecoes.PersistenciaException;
import astra.modelo.*;
import astra.persistencia.IngressoRepositorio;
import astra.persistencia.PedidoRepositorio;
import astra.persistencia.ProdutoRepositorio;
import astra.servico.pagamento.FormaPagamento;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//Venda: pedido, ingresso, produto e pagamento.
//Regras estavam antes entre CinemaService e TelaCliente.

public class VendaService {

    private final List<FormaPagamento> formasPagamento;
    private final PedidoRepositorio pedidoRepositorio;     //interface: grava a venda
    private final IngressoRepositorio ingressoRepositorio; //interface: consulta assentos vendidos
    private final ProdutoRepositorio produtoRepositorio;   //interface: consulta o estoque atual

    //Chegam prontas pelo construtor
    public VendaService(List<FormaPagamento> formasPagamento, PedidoRepositorio pedidoRepositorio,
                        IngressoRepositorio ingressoRepositorio, ProdutoRepositorio produtoRepositorio) {
        if (formasPagamento == null || formasPagamento.isEmpty()) {
            throw new IllegalArgumentException("Informe pelo menos uma forma de pagamento.");
        }
        if (pedidoRepositorio == null || ingressoRepositorio == null || produtoRepositorio == null) {
            throw new IllegalArgumentException("Os repositórios de pedido, de ingresso e de produto são obrigatórios.");
        }
        this.formasPagamento = List.copyOf(formasPagamento);
        this.pedidoRepositorio = pedidoRepositorio;
        this.ingressoRepositorio = ingressoRepositorio;
        this.produtoRepositorio = produtoRepositorio;
    }

    public Pedido iniciarPedido(Cliente cliente) {
        //id 0: pedido ainda não gravado - id vem do banco quando tiver PedidoRepositorio
        return new Pedido(0, cliente);
    }

    public Ingresso venderIngresso (Pedido pedido, Sessao sessao, int numeroAssento, Ingresso.TipoIngresso tipo) throws AssentoIndisponivelException {
        if(sessao == null){
            throw new IllegalArgumentException("Você precisa escolher uma sessão!");
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
    //Só depois do pagamento aprovado a venda (pedido + itens + ingressos + pagamento) é gravada no banco
    //e o estoque dos produtos é abatido (tudo na mesma transação, dentro do PedidoRepositorio)
    public Pagamento finalizarPedido(Pedido pedido, FormaPagamento forma)
            throws PagamentoException, AssentoIndisponivelException, EstoqueInsuficienteException, PersistenciaException {
        pedido.validarPodeSerPago();
        conferirAssentosNoBanco(pedido); //antes de cobrar: outra janela pode ter vendido o assento
        conferirEstoqueNoBanco(pedido);  //antes de cobrar: outra janela pode ter vendido as últimas unidades
        Pagamento pagamento = forma.criarPagamento(pedido.calcularTotal(), pedido.getCliente());
        pagamento.processarPagamento();
        pedido.confirmarPagamento();
        pedidoRepositorio.salvar(pedido, pagamento); //transação: grava tudo ou nada
        return pagamento;
    }

    //Pergunta ao banco (fonte da verdade) se há estoque para tudo que o pedido pede.
    //Soma as quantidades por produto: o mesmo produto pode estar em mais de um item do pedido.
    //É só para NÃO COBRAR à toa; quem garante o estoque de verdade é o UPDATE da transação.
    private void conferirEstoqueNoBanco(Pedido pedido) throws EstoqueInsuficienteException, PersistenciaException {
        Map<Integer, Integer> pedidoPorProduto = new LinkedHashMap<>();
        for (ItemPedido item : pedido.getItens()) {
            pedidoPorProduto.merge(item.getProduto().getId(), item.getQuantidade(), Integer::sum);
        }
        for (Map.Entry<Integer, Integer> pedidoDoProduto : pedidoPorProduto.entrySet()) {
            Produto atual = produtoRepositorio.buscarPorId(pedidoDoProduto.getKey()).orElseThrow(() ->
                    new PersistenciaException("Um produto do pedido não existe mais no banco."));
            if (!atual.possuiEstoque(pedidoDoProduto.getValue())) {
                throw new EstoqueInsuficienteException("Estoque insuficiente de \"" + atual.getNome()
                        + "\": restam " + atual.getEstoque() + " e o pedido pede " + pedidoDoProduto.getValue() + ".");
            }
        }
    }

    //Pergunta ao banco (fonte da verdade) se algum assento do pedido já foi vendido
    private void conferirAssentosNoBanco(Pedido pedido) throws AssentoIndisponivelException, PersistenciaException {
        for (Ingresso ingresso : pedido.getIngressos()) {
            int numero = ingresso.getAssento().getNumero();
            if (ingressoRepositorio.assentoEstaOcupado(ingresso.getSessao().getId(), numero)) {
                throw new AssentoIndisponivelException("O assento " + numero + " já foi vendido para esta sessão.");
            }
        }
    }
}