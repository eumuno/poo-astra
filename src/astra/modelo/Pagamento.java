package astra.modelo;

import astra.excecoes.PagamentoException;
import astra.excecoes.TransicaoStatusInvalidaException;
import java.time.LocalDateTime;

/**
 * Pagamento de um pedido (conceito geral, não pode ser instanciado diretamente).
 *
 * <p>Cada subclasse ({@link PagamentoPix}, {@link PagamentoCartao}) implementa a
 * sua própria regra em {@link #processarPagamento()}. Quem usa o pagamento trata
 * todos pelo tipo {@code Pagamento} (polimorfismo), sem saber qual é a forma.</p>
 *
 * <p>Todo pagamento começa {@link Status#PENDENTE} e só sai desse estado uma vez:
 * vira {@link Status#APROVADO} ou {@link Status#RECUSADO}. Um pagamento aprovado
 * ainda pode ser {@link Status#ESTORNADO} com {@link #estornar()}.</p>
 */
public abstract class Pagamento {

    /** Situação do pagamento ao longo do processamento. */
    public enum Status {
        /** Criado, ainda não processado. É o estado inicial de todo pagamento. */
        PENDENTE,
        /** Processado com sucesso: o valor foi cobrado. */
        APROVADO,
        /** Processado e negado (Pix expirado ou cartão vencido). */
        RECUSADO,
        /**
         * Foi aprovado, mas a venda não pôde ser gravada e o valor foi devolvido.
         * Um pagamento estornado nunca chega ao banco (o estorno só acontece quando a gravação falhou).
         */
        ESTORNADO
    }

    /** Meio de pagamento, gravado no banco (os valores são os aceitos pela tabela {@code pagamento}). */
    public enum Forma {
        /** Pagamento instantâneo por chave Pix. */
        PIX,
        /** Pagamento com cartão. */
        CARTAO
    }

    /** Valor cobrado, em reais. */
    private final double valor;
    /** Momento em que o pagamento foi criado. */
    private final LocalDateTime data = LocalDateTime.now();
    /** Situação atual; todo pagamento começa pendente. */
    private Status status = Status.PENDENTE;

    /**
     * Cria um pagamento pendente com a data e hora atuais.
     *
     * @param valor valor a ser cobrado, em reais
     */
    public Pagamento(double valor) {
        this.valor = valor;
    }

    /**
     * Processa o pagamento conforme a regra da forma escolhida e muda o status
     * para {@link Status#APROVADO} ou {@link Status#RECUSADO}.
     *
     * @throws PagamentoException se o pagamento for recusado ou já tiver sido processado
     */
    public abstract void processarPagamento() throws PagamentoException;

    /**
     * Informa a forma deste pagamento, sem que quem usa precise de {@code instanceof}.
     *
     * @return a forma de pagamento da subclasse
     */
    public abstract Forma getForma();

    /**
     * Muda o status do pagamento. Só é permitido enquanto ele ainda estiver pendente.
     *
     * @param novo o novo status
     * @throws PagamentoException (mais especificamente {@link TransicaoStatusInvalidaException})
     *                            se o pagamento já foi finalizado
     */
    protected void mudarStatus(Status novo) throws PagamentoException {
        if (status != Status.PENDENTE) {
            throw new TransicaoStatusInvalidaException("este pagamento já foi finalizado");
        }
        status = novo;
    }

    /**
     * Devolve um pagamento aprovado (simulação). Usado quando a cobrança passou,
     * mas a venda não foi registrada: o cliente não pode ficar cobrado por algo que não comprou.
     *
     * @throws IllegalStateException se o pagamento não estiver aprovado
     */
    public void estornar() {
        if (status != Status.APROVADO) {
            throw new IllegalStateException("Só um pagamento aprovado pode ser estornado.");
        }
        status = Status.ESTORNADO;
    }

    /**
     * Devolve o valor cobrado.
     *
     * @return o valor, em reais
     */
    public double getValor() { return valor; }

    /**
     * Devolve o momento em que o pagamento foi criado.
     *
     * @return a data e hora de criação
     */
    public LocalDateTime getData() { return data; }

    /**
     * Devolve a situação atual do pagamento.
     *
     * @return o status atual
     */
    public Status getStatus() { return status; }
}