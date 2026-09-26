package astra.modelo;

import astra.excecoes.PagamentoException;
import java.time.LocalDateTime;

// Abstrata: só existem Pix ou Cartão, nunca "pagamento genérico"
public abstract class Pagamento {

    private double valor;
    private LocalDateTime data;
    private statusPagamento status;

    public Pagamento(double valor) {
        this.valor = valor;
        this.data = LocalDateTime.now();
        this.status = statusPagamento.PENDENTE; // todo pagamento começa pendente
    }

    // Cada subclasse faz do seu jeito
    public abstract void processarPagamento() throws PagamentoException;

    // Regra: só dá pra mudar o status se ainda estiver pendente
    protected void mudarStatus(statusPagamento novoStatus) throws PagamentoException {
        if (status != statusPagamento.PENDENTE) {
            throw new PagamentoException("Este pagamento já foi finalizado.");
        }
        this.status = novoStatus;
    }

    public double getValor() {
        return valor;
    }

    public LocalDateTime getData() {
        return data;
    }

    public statusPagamento getStatus() {
        return status;
    }


        // Enum: conjunto fechado de valores possíveis para o status do pagamento.
        public enum statusPagamento {
            PENDENTE,   // criado, ainda não processado
            APROVADO,   // pago com sucesso
            RECUSADO,   // ex.: cartão recusado
            CANCELADO,  // usuário desistiu
            EXPIRADO    // ex.: Pix não pago no prazo
        }
}