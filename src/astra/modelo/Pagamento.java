package astra.modelo;

import astra.excecoes.PagamentoException;
import astra.excecoes.TransicaoStatusInvalidaException;
import java.time.LocalDateTime;

public abstract class Pagamento {

    // ESTORNADO: foi aprovado, mas a venda não pôde ser gravada e o valor foi devolvido.
    // Um pagamento estornado nunca chega ao banco (o estorno só acontece quando a gravação falhou).
    public enum Status { PENDENTE, APROVADO, RECUSADO, ESTORNADO }

    // Qual é o meio de pagamento (gravado no banco; os valores são os da tabela pagamento)
    public enum Forma { PIX, CARTAO }

    private final double valor;
    private final LocalDateTime data = LocalDateTime.now();
    private Status status = Status.PENDENTE; // todo pagamento começa pendente

    public Pagamento(double valor) {
        this.valor = valor;
    }

    // Cada subclasse (Pix, Cartão) processa do seu jeito
    public abstract void processarPagamento() throws PagamentoException;

    // Cada subclasse diz qual é a sua forma (sem precisar de instanceof em quem usa)
    public abstract Forma getForma();

    // Só dá pra mudar o status se ainda estiver pendente
    protected void mudarStatus(Status novo) throws PagamentoException {
        if (status != Status.PENDENTE) {
            throw new TransicaoStatusInvalidaException("este pagamento já foi finalizado");
        }
        status = novo;
    }

    // Devolve um pagamento aprovado (simulação). Usado quando a cobrança passou,
    // mas a venda não foi registrada: o cliente não pode ficar cobrado por algo que não comprou.
    public void estornar() {
        if (status != Status.APROVADO) {
            throw new IllegalStateException("Só um pagamento aprovado pode ser estornado.");
        }
        status = Status.ESTORNADO;
    }

    public double getValor() { return valor; }
    public LocalDateTime getData() { return data; }
    public Status getStatus() { return status; }
}