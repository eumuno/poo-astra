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

    // ATRIBUTOS ENCAPSULADOS
    private final int id;
    private final Cliente cliente;
    private final List<ItemPedido> itens;     // Produtos da bomboniere
    private final List<Ingresso> ingressos;   // Ingressos do cinema
    private final LocalDateTime data;
    private StatusPedido status;

    // CONSTRUTOR
    public Pedido(int id, Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("O pedido deve ter um cliente associado.");
        }
        this.id = id;
        this.cliente = cliente;
        this.data = LocalDateTime.now();
        this.itens = new ArrayList<>();
        this.ingressos = new ArrayList<>();
        this.status = StatusPedido.ABERTO;
    }

    // METODOS PARA ITENS DA BOMBONIERE
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

    // METODOS PARA INGRESSOS DO CINEMA
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

    // Delegacao: soma os subtotais dos itens e os valores dos ingressos
    public double calcularTotal() {
        double totalItens = 0.0;
        for (ItemPedido item : this.itens) {
            totalItens += item.calcularSubtotal();
        }

        double totalIngressos = 0.0;
        for (Ingresso ing : this.ingressos) {
            // Nota: Se na classe do seu colega o metodo se chamar getPreco(), mude abaixo para ing.getPreco()
            totalIngressos += ing.getPrecoIngresso();
        }

        return totalItens + totalIngressos;
    }

    public void confirmarPagamento() {
        validarPedidoAberto();
        if (this.itens.isEmpty() && this.ingressos.isEmpty()) {
            throw new IllegalStateException("Nao e possivel pagar um pedido totalmente vazio.");
        }
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

    // GETTERS (Acesso seguro de leitura)
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