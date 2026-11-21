package astra.servico;
import astra.excecoes.AssentoIndisponivelException;
import astra.excecoes.EstoqueInsuficienteException;
import astra.excecoes.IngressosEsgotadosException;
import astra.excecoes.PagamentoException;
import astra.excecoes.PersistenciaException;
import astra.modelo.*;
import astra.persistencia.IngressoRepositorio;
import astra.persistencia.PedidoRepositorio;
import astra.persistencia.ProdutoRepositorio;
import astra.servico.pagamento.FormaPagamento;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Casos de uso da venda: montar o pedido (ingressos e produtos), consultar poltronas ocupadas e pagar.
 *
 * <p>As regras da venda ficam aqui, e não na interface gráfica. Depende apenas de interfaces
 * ({@link PedidoRepositorio}, {@link IngressoRepositorio}, {@link ProdutoRepositorio} e
 * {@link FormaPagamento}), que chegam prontas pelo construtor.</p>
 */
public class VendaService {

    private final List<FormaPagamento> formasPagamento;
    private final PedidoRepositorio pedidoRepositorio;     //interface: grava a venda
    private final IngressoRepositorio ingressoRepositorio; //interface: consulta assentos vendidos
    private final ProdutoRepositorio produtoRepositorio;   //interface: consulta o estoque atual

    /**
     * Cria o serviço de venda.
     *
     * @param formasPagamento     formas que o cliente pode escolher (pelo menos uma)
     * @param pedidoRepositorio   onde a venda é gravada
     * @param ingressoRepositorio de onde se consulta quais poltronas já foram vendidas
     * @param produtoRepositorio  de onde se consulta o estoque atual
     * @throws IllegalArgumentException se faltar alguma forma de pagamento ou algum repositório
     */
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

    /**
     * Abre um pedido novo e vazio para o cliente.
     *
     * @param cliente dono do pedido
     * @return o pedido aberto (ainda não gravado: id 0)
     */
    public Pedido iniciarPedido(Cliente cliente) {
        //id 0: pedido ainda não gravado - o id definitivo vem do banco em finalizarPedido()
        return new Pedido(0, cliente);
    }

    /**
     * Coloca um ingresso no pedido. A poltrona só é marcada como ocupada depois do pagamento.
     *
     * @param pedido        pedido aberto que receberá o ingresso
     * @param sessao        sessão escolhida (precisa estar à venda)
     * @param numeroAssento número da poltrona
     * @param tipo          inteira ou meia
     * @return o ingresso criado
     * @throws AssentoIndisponivelException se a poltrona está ocupada, não existe ou já está no pedido
     * @throws IngressosEsgotadosException  se a sessão não tem mais nenhuma poltrona livre
     * @throws IllegalArgumentException     se nenhuma sessão foi escolhida
     * @throws IllegalStateException        se a sessão foi cancelada ou já começou
     */
    public Ingresso venderIngresso (Pedido pedido, Sessao sessao, int numeroAssento, Ingresso.TipoIngresso tipo) throws AssentoIndisponivelException, IngressosEsgotadosException {
        if(sessao == null){
            throw new IllegalArgumentException("Você precisa escolher uma sessão!");
        }
        if(!sessao.estaDisponivel()){
            throw new IllegalStateException(sessao.getStatus() == Sessao.StatusSessao.AGENDADA
                    ? "Esta sessão já começou e não vende mais ingressos."
                    : "A sessão não está disponível para venda!");
        }
        if(!sessao.possuiVagas()){
            throw new IngressosEsgotadosException("Todos os assentos desta sessão já foram vendidos.");
        }
        if(!sessao.assentoEstaDisponivel(numeroAssento)){
            throw new AssentoIndisponivelException("O assento " + numeroAssento + " está ocupado ou não existe.",
                    sessao.getId(), numeroAssento);
        }
        if(pedidoJaTemAssento(pedido, sessao, numeroAssento)){
            throw new AssentoIndisponivelException("O assento " + numeroAssento + " já está no seu pedido.",
                    sessao.getId(), numeroAssento);
        }

        Assento assento = sessao.getSala().buscarAssento(numeroAssento);
        Ingresso ingresso = new Ingresso(0, sessao.getFilme(), sessao, assento, pedido.getCliente(), tipo);
        pedido.adicionarIngresso(ingresso); //o assento só é ocupado depois do pagamento aprovado
        return ingresso;
    }

    //Antes de pagar o assento ainda não está ocupado; por isso confere se o próprio pedido já o reservou
    private boolean pedidoJaTemAssento(Pedido pedido, Sessao sessao, int numeroAssento) {
        for (Ingresso existente : pedido.getIngressos()) {
            if (existente.getSessao().getId() == sessao.getId()
                    && existente.getAssento().getNumero() == numeroAssento) {
                return true;
            }
        }
        return false;
    }

    /**
     * Consulta no banco (a fonte da verdade) quais poltronas da sessão já foram vendidas.
     * É o que a tela usa para pintar as poltronas de vermelho, sem conhecer o repositório.
     *
     * @param sessao a sessão consultada
     * @return os números das poltronas vendidas (vazio se nenhuma)
     * @throws PersistenciaException se o banco falhar
     */
    public Set<Integer> consultarAssentosOcupados(Sessao sessao) throws PersistenciaException {
        Set<Integer> ocupados = new LinkedHashSet<>();
        for (Assento assento : sessao.getSala().getAssentos()) {
            if (ingressoRepositorio.assentoEstaOcupado(sessao.getId(), assento.getNumero())) {
                ocupados.add(assento.getNumero());
            }
        }
        return ocupados;
    }

    /**
     * Coloca produtos da bomboniere no pedido.
     *
     * @param pedido     pedido aberto que receberá o item
     * @param produto    produto escolhido
     * @param quantidade unidades (pelo menos 1)
     * @throws IllegalArgumentException se nenhum produto foi escolhido ou a quantidade é inválida
     */
    public void adicionarProduto(Pedido pedido, Produto produto, int quantidade){
        if (produto == null){
            throw new IllegalArgumentException("Escolha um produto.");
        }
        pedido.adicionarItem(produto, quantidade);
    }

    /**
     * Formas de pagamento oferecidas ao cliente.
     *
     * @return a lista (somente leitura) de formas de pagamento
     */
    public List<FormaPagamento> getFormasPagamento() {
        return formasPagamento;
    }

    /**
     * Paga o pedido pela forma escolhida e, só depois do pagamento aprovado, grava a venda
     * (pedido, itens, ingressos e pagamento) numa transação e abate o estoque.
     *
     * <p>Antes de cobrar, confere se as sessões ainda estão à venda e se as poltronas e o
     * estoque continuam disponíveis. Se a gravação falhar depois do pagamento aprovado, a venda
     * é desfeita (estorno e pedido volta a ABERTO) para o cliente poder tentar de novo.</p>
     *
     * @param pedido pedido aberto e não vazio
     * @param forma  forma de pagamento escolhida
     * @return o pagamento aprovado
     * @throws PagamentoException           se o pagamento for recusado
     * @throws AssentoIndisponivelException se alguma poltrona já foi vendida
     * @throws EstoqueInsuficienteException se algum produto não tem unidades suficientes
     * @throws PersistenciaException        se o banco falhar
     * @throws IllegalStateException        se o pedido não puder ser pago ou alguma sessão já não estiver à venda
     */
    public Pagamento finalizarPedido(Pedido pedido, FormaPagamento forma)
            throws PagamentoException, AssentoIndisponivelException, EstoqueInsuficienteException, PersistenciaException {
        pedido.validarPodeSerPago();
        conferirSessoesDoPedido(pedido); //antes de cobrar: a sessão pode ter começado enquanto o ingresso estava no carrinho
        conferirAssentosNoBanco(pedido); //antes de cobrar: outra janela pode ter vendido o assento
        conferirEstoqueNoBanco(pedido);  //antes de cobrar: outra janela pode ter vendido as últimas unidades
        Pagamento pagamento = forma.criarPagamento(pedido.calcularTotal(), pedido.getCliente());
        pagamento.processarPagamento();  //se for recusado, lança aqui e o pedido nem chega a mudar
        pedido.confirmarPagamento();
        try {
            pedidoRepositorio.salvar(pedido, pagamento); //transação: grava tudo ou nada
        } catch (PersistenciaException | AssentoIndisponivelException
                 | EstoqueInsuficienteException | RuntimeException e) {
            desfazerVenda(pedido, pagamento); //nada foi gravado: volta tudo como estava antes de pagar
            throw e;                          //a tela continua recebendo a exceção original para explicar o motivo
        }
        ocuparAssentosDoPedido(pedido);              //agora sim: pagou e gravou, os assentos ficam ocupados
        return pagamento;
    }

    //O pagamento passou mas a gravação não: devolve o dinheiro e reabre o pedido
    private void desfazerVenda(Pedido pedido, Pagamento pagamento) {
        pagamento.estornar();
        pedido.desfazerConfirmacaoPagamento();
    }

    //Marca na sessão (em memória) os assentos que acabaram de ser pagos e gravados
    private void ocuparAssentosDoPedido(Pedido pedido) {
        for (Ingresso ingresso : pedido.getIngressos()) {
            ingresso.getSessao().ocuparAssento(ingresso.getAssento().getNumero());
        }
    }

    //Uma sessão agendada deixa de estar à venda quando o horário chega; o ingresso pode ter ficado no carrinho
    private void conferirSessoesDoPedido(Pedido pedido) {
        for (Ingresso ingresso : pedido.getIngressos()) {
            if (!ingresso.getSessao().estaDisponivel()) {
                throw new IllegalStateException("A sessão \"" + ingresso.getSessao()
                        + "\" não está mais à venda (já começou ou foi cancelada). Remova o ingresso do carrinho.");
            }
        }
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
                throw new EstoqueInsuficienteException(atual.getNome(), atual.getEstoque(), pedidoDoProduto.getValue());
            }
        }
    }

    //Pergunta ao banco (fonte da verdade) se algum assento do pedido já foi vendido
    private void conferirAssentosNoBanco(Pedido pedido) throws AssentoIndisponivelException, PersistenciaException {
        for (Ingresso ingresso : pedido.getIngressos()) {
            int numero = ingresso.getAssento().getNumero();
            if (ingressoRepositorio.assentoEstaOcupado(ingresso.getSessao().getId(), numero)) {
                throw new AssentoIndisponivelException("O assento " + numero + " já foi vendido para esta sessão.",
                        ingresso.getSessao().getId(), numero);
            }
        }
    }
}