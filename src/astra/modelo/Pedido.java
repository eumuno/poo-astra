package astra.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pedido de um {@link Cliente}: reúne os ingressos do cinema e os produtos da
 * bomboniere e controla o ciclo de vida da compra
 * (ABERTO, PAGO, PRONTO, RETIRADO ou CANCELADO).
 *
 * <p>Regras de negócio guardadas aqui: só um pedido aberto aceita ou remove itens e
 * ingressos, e um pedido vazio não pode ser pago.</p>
 */
public class Pedido {

    /** Ciclo de vida do pedido. */
    public enum StatusPedido {
        ABERTO,
        PAGO,
        PRONTO,
        RETIRADO,
        CANCELADO
    }

    // ATRIBUTOS
    private final int id;
    private final Cliente cliente;
    private final List<ItemPedido> itens;     // Produtos da bomboniere
    private final List<Ingresso> ingressos;   // Ingressos do cinema
    private final LocalDateTime data;
    private StatusPedido status;

    // CONSTRUTOR (pedido novo: aberto, com a data de agora)
    /**
     * Cria um pedido novo, aberto e com a data de agora.
     *
     * @param id      identificador do pedido (0 enquanto ainda não foi gravado no banco)
     * @param cliente cliente dono do pedido; não pode ser nulo
     * @throws IllegalArgumentException se o cliente for nulo
     */
    public Pedido(int id, Cliente cliente) {
        this(id, cliente, LocalDateTime.now(), StatusPedido.ABERTO);
    }

    // CONSTRUTOR (privado: só quem está dentro da classe monta um pedido com data e status quaisquer)
    private Pedido(int id, Cliente cliente, LocalDateTime data, StatusPedido status) {
        if (cliente == null) {
            throw new IllegalArgumentException("O pedido deve ter um cliente associado.");
        }
        if (data == null || status == null) {
            throw new IllegalArgumentException("Data e status do pedido são obrigatórios.");
        }
        this.id = id;
        this.cliente = cliente;
        this.data = data;
        this.itens = new ArrayList<>();
        this.ingressos = new ArrayList<>();
        this.status = status;
    }

    /**
     * Reconstrói um pedido que já existe (ex: lido do banco), com o id, a data e o status
     * que ele tinha. Não passa por validarPedidoAberto(), porque um pedido PAGO não aceitaria
     * adicionar itens pelos métodos normais. Usado só para restaurar dados já gravados.
     *
     * @param id        identificador gravado no banco
     * @param cliente   cliente dono do pedido; não pode ser nulo
     * @param data      data em que o pedido foi criado; não pode ser nula
     * @param status    status que o pedido tinha; não pode ser nulo
     * @param itens     produtos do pedido (pode ser nulo, e então fica vazio)
     * @param ingressos ingressos do pedido (pode ser nulo, e então fica vazio)
     * @return o pedido restaurado
     * @throws IllegalArgumentException se o cliente, a data ou o status forem nulos
     */
    public static Pedido restaurar(int id, Cliente cliente, LocalDateTime data, StatusPedido status,
                                   List<ItemPedido> itens, List<Ingresso> ingressos) {
        Pedido pedido = new Pedido(id, cliente, data, status);
        if (itens != null) {
            pedido.itens.addAll(itens);
        }
        if (ingressos != null) {
            pedido.ingressos.addAll(ingressos);
        }
        return pedido;
    }

    // METODOS BOMBONIERE
    /**
     * Adiciona um produto da bomboniere ao pedido, usando o preço atual do produto.
     *
     * @param produto    produto escolhido; não pode ser nulo
     * @param quantidade número de unidades; deve ser no mínimo 1
     * @throws IllegalStateException    se o pedido não estiver mais aberto
     * @throws IllegalArgumentException se o produto for nulo ou a quantidade for menor que 1
     */
    public void adicionarItem(Produto produto, int quantidade) {
        validarPedidoAberto();
        ItemPedido novoItem = new ItemPedido(produto, quantidade);
        this.itens.add(novoItem);
    }

    /**
     * Adiciona um item já montado ao pedido.
     *
     * @param item item a adicionar; não pode ser nulo
     * @throws IllegalStateException    se o pedido não estiver mais aberto
     * @throws IllegalArgumentException se o item for nulo
     */
    public void adicionarItem(ItemPedido item) {
        validarPedidoAberto();
        if (item == null) {
            throw new IllegalArgumentException("Item inválido para adição.");
        }
        this.itens.add(item);
    }

    /**
     * Remove um item do pedido.
     *
     * @param item item a remover
     * @throws IllegalStateException se o pedido não estiver mais aberto
     */
    public void removerItem(ItemPedido item) {
        validarPedidoAberto();
        this.itens.remove(item);
    }

    // METODOS INGRESSOS
    /**
     * Adiciona um ingresso ao pedido.
     *
     * @param ingresso ingresso a adicionar; não pode ser nulo
     * @throws IllegalStateException    se o pedido não estiver mais aberto
     * @throws IllegalArgumentException se o ingresso for nulo
     */
    public void adicionarIngresso(Ingresso ingresso) {
        validarPedidoAberto();
        if (ingresso == null) {
            throw new IllegalArgumentException("Ingresso inválido para adição.");
        }
        this.ingressos.add(ingresso);
    }

    /**
     * Remove um ingresso do pedido.
     *
     * @param ingresso ingresso a remover
     * @throws IllegalStateException se o pedido não estiver mais aberto
     */
    public void removerIngresso(Ingresso ingresso) {
        validarPedidoAberto();
        this.ingressos.remove(ingresso);
    }

    // REGRAS DE NEGOCIO E CALCULOS
    /**
     * Calcula o total do pedido: soma dos subtotais dos produtos e dos preços dos ingressos.
     *
     * @return o valor total a pagar
     */
    public double calcularTotal() {
        double totalItens = 0.0;
        for (ItemPedido item : this.itens) {
            totalItens += item.calcularSubtotal();
        }

        double totalIngressos = 0.0;
        for (Ingresso ing : this.ingressos) {
            totalIngressos += ing.getPrecoIngresso();
        }

        return totalItens + totalIngressos;
    }

    /**
     * Informa se o pedido não tem nenhum produto nem ingresso.
     *
     * @return {@code true} se não houver itens nem ingressos
     */
    public boolean estaVazio(){
        return this.itens.isEmpty() && this.ingressos.isEmpty();
    }

    /**
     * Confere se o pedido pode ser pago. É a fonte da regra "não se paga pedido vazio",
     * para que ela não fique duplicada em outros lugares.
     *
     * @throws IllegalStateException se o pedido não estiver aberto ou se estiver vazio
     */
    public void validarPodeSerPago(){
        validarPedidoAberto();
        if(estaVazio()){
            throw new IllegalStateException("O carrinho está vazio, adicione um produto ou ingresso antes de pagar!");
        }
    }

    /**
     * Confirma o pagamento: o pedido passa de ABERTO para PAGO.
     *
     * @throws IllegalStateException se o pedido não estiver aberto ou se estiver vazio
     */
    public void confirmarPagamento(){
        validarPodeSerPago();
        this.status = StatusPedido.PAGO;
    }

    /**
     * Desfaz o {@link #confirmarPagamento()}: o pedido volta de PAGO para ABERTO.
     *
     * <p>Serve para um único caso: o pagamento foi aprovado, mas a venda NÃO conseguiu ser
     * gravada no banco. Sem isso o pedido ficaria PAGO só na memória e o cliente não
     * conseguiria tentar de novo (todo método que mexe no pedido exige que ele esteja aberto).</p>
     *
     * @throws IllegalStateException se o pedido não estiver PAGO ou se já foi gravado (id maior que 0)
     */
    public void desfazerConfirmacaoPagamento() {
        if (this.status != StatusPedido.PAGO || this.id > 0) {
            throw new IllegalStateException("Só um pedido pago que ainda não foi gravado pode voltar a ficar aberto.");
        }
        this.status = StatusPedido.ABERTO;
    }

    /**
     * Marca o pedido como pronto para retirada.
     *
     * @throws IllegalStateException se o pedido não estiver PAGO
     */
    public void marcarComoPronto() {
        if (this.status != StatusPedido.PAGO) {
            throw new IllegalStateException("O pedido precisa estar pago antes de ser marcado como pronto.");
        }
        this.status = StatusPedido.PRONTO;
    }

    /**
     * Registra a retirada do pedido pelo cliente.
     *
     * @throws IllegalStateException se o pedido não estiver PRONTO
     */
    public void retirar() {
        if (this.status != StatusPedido.PRONTO) {
            throw new IllegalStateException("O pedido só pode ser retirado quando estiver pronto.");
        }
        this.status = StatusPedido.RETIRADO;
    }

    /**
     * Cancela o pedido. Pedidos já pagos ou finalizados não podem ser cancelados por aqui.
     *
     * @throws IllegalStateException se o pedido estiver PAGO, PRONTO ou RETIRADO
     */
    public void cancelar() {
        if (this.status == StatusPedido.PAGO || this.status == StatusPedido.PRONTO || this.status == StatusPedido.RETIRADO) {
            throw new IllegalStateException("Pedidos pagos ou finalizados não podem ser cancelados diretamente.");
        }
        this.status = StatusPedido.CANCELADO;
    }

    private void validarPedidoAberto() {
        if (this.status != StatusPedido.ABERTO) {
            throw new IllegalStateException("Este pedido já foi fechado e não pode mais ser alterado.");
        }
    }

    // GETTERS
    /** @return o identificador do pedido (0 se ainda não foi gravado no banco) */
    public int getId() {
        return id;
    }

    /** @return o cliente dono do pedido */
    public Cliente getCliente() {
        return cliente;
    }

    /** @return a data e hora em que o pedido foi criado */
    public LocalDateTime getData() {
        return data;
    }

    /** @return o status atual do pedido */
    public StatusPedido getStatus() {
        return status;
    }

    /** @return a lista dos produtos do pedido, somente para leitura */
    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    /** @return a lista dos ingressos do pedido, somente para leitura */
    public List<Ingresso> getIngressos() {
        return Collections.unmodifiableList(ingressos);
    }
}