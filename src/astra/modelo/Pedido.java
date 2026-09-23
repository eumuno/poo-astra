package astra.modelo;

import java.time.LocalDateTime;
import java.util.List;

public class Pedido {

    // ATRIBUTOS
    private int id;
    private Cliente cliente;
    private List<ItemPedido> itens;
    private LocalDateTime data;
    private statusPedido status;

    // ENUM
    public enum statusPedido {
        ABERTO,
        PAGO,
        PRONTO,
        RETIRADO,
        CANCELADO
    }

    // MÉTODOS
    public void adicionarItem(Produto produto, int quantidade)
    public void removerItem(...)
    public double calcularTotal()
    public void confirmarPagamento()
    public void marcarComoPronto()
    public void retirar()
    public void cancelar()

}
