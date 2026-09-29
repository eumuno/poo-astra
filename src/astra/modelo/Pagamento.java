package astra.modelo;

import astra.excecoes.PagamentoException;
import java.time.LocalDateTime;

// Abstrata: só existem Pix ou Cartão, nunca "pagamento genérico"
public abstract class Pagamento {

    public enum Status { PENDENTE, APROVADO, RECUSADO }

    private final double valor;
    private final LocalDateTime data = LocalDateTime.now();
    private Status status = Status.PENDENTE; // todo pagamento começa pendente

    public Pagamento(double valor) {
        this.valor = valor;
    }

    // Cada subclasse (Pix, Cartão) processa do seu jeito
    public abstract void processarPagamento() throws PagamentoException;

    // Só dá pra mudar o status se ainda estiver pendente
    protected void mudarStatus(Status novo) throws PagamentoException {
        if (status != Status.PENDENTE) {
            throw new PagamentoException("Este pagamento já foi finalizado.");
        }
        status = novo;
    }

    public double getValor() { return valor; }
    public LocalDateTime getData() { return data; }
    public Status getStatus() { return status; }
}