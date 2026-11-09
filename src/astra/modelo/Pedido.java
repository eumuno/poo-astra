package astra.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pedido {

    // ENUM para o ciclo de vida do pedido
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
    public Pedido(int id, Cliente cliente) {
        this(id, cliente, LocalDateTime.now(), StatusPedido.ABERTO);
    }

    // CONSTRUTOR (privado: só quem está dentro da classe monta um pedido com data e status quaisquer)
    private Pedido(int id, Cliente cliente, LocalDateTime data, StatusPedido status) {
        if (cliente == null) {
            throw new IllegalArgumentException("O pedido deve ter um cliente associado.");
        }
        if (data == null || status == null) {
            throw new IllegalArgumentException("Data e status do pedido sao obrigatorios.");
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
    public void adicionarItem(Produto produto, int quantidade) {
        validarPedidoAberto();
        ItemPedido novoItem = new ItemPedido(produto, quantidade);
        this.itens.add(novoItem);
    }

    public void adicionarItem(ItemPedido item) {
        validarPedidoAberto();
        if (item == null) {
            throw new IllegalArgumentException("Item invalido para adicao.");
        }
        this.itens.add(item);
    }

    public void removerItem(ItemPedido item) {
        validarPedidoAberto();
        this.itens.remove(item);
    }

    // METODOS INGRESSOS
    public void adicionarIngresso(Ingresso ingresso) {
        validarPedidoAberto();
        if (ingresso == null) {
            throw new IllegalArgumentException("Ingresso invalido para adicao.");
        }
        this.ingressos.add(ingresso);
    }

    public void removerIngresso(Ingresso ingresso) {
        validarPedidoAberto();
        this.ingressos.remove(ingresso);
    }

    // REGRAS DE NEGOCIO E CALCULOS
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

    public boolean estaVazio(){
        return this.itens.isEmpty() && this.ingressos.isEmpty();
    }

    //Fonte da regra "não se paga pedido vazio, assim não fica mais duplicada
    public void validarPodeSerPago(){
        validarPedidoAberto();
        if(estaVazio()){
            throw new IllegalStateException("O carrinho está vazio, adicione um produto ou ingresso antes de pagar!");
        }
    }

    public void confirmarPagamento(){
        validarPodeSerPago();
        this.status = StatusPedido.PAGO;
    }

    public void marcarComoPronto() {
        if (this.status != StatusPedido.PAGO) {
            throw new IllegalStateException("O pedido precisa estar pago antes de ser marcado como pronto.");
        }
        this.status = StatusPedido.PRONTO;
    }

    public void retirar() {
        if (this.status != StatusPedido.PRONTO) {
            throw new IllegalStateException("O pedido so pode ser retirado quando estiver pronto.");
        }
        this.status = StatusPedido.RETIRADO;
    }

    public void cancelar() {
        if (this.status == StatusPedido.PAGO || this.status == StatusPedido.PRONTO || this.status == StatusPedido.RETIRADO) {
            throw new IllegalStateException("Pedidos pagos ou finalizados nao podem ser cancelados diretamente.");
        }
        this.status = StatusPedido.CANCELADO;
    }

    private void validarPedidoAberto() {
        if (this.status != StatusPedido.ABERTO) {
            throw new IllegalStateException("Operacao invalida: o pedido nao esta mais aberto.");
        }
    }

    // GETTERS
    public int getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public LocalDateTime getData() {
        return data;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public List<Ingresso> getIngressos() {
        return Collections.unmodifiableList(ingressos);
    }
}