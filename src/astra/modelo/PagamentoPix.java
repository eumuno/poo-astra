package astra.modelo;

import astra.excecoes.PagamentoException;
import java.time.LocalDateTime;

/**
 * Pagamento por Pix. O código gerado vale 10 minutos a partir da criação;
 * depois disso o pagamento é recusado.
 */
public class PagamentoPix extends Pagamento {

    /** Chave Pix que recebe o pagamento. */
    private final String chavePix;
    /** Limite para pagar: 10 minutos depois da criação. */
    private final LocalDateTime expiraEm = getData().plusMinutes(10);

    /**
     * Cria um pagamento Pix pendente, válido por 10 minutos.
     *
     * @param valor valor a ser cobrado, em reais
     * @param chavePix chave Pix que recebe o pagamento
     */
    public PagamentoPix(double valor, String chavePix) {
        super(valor);
        this.chavePix = chavePix;
    }

    /**
     * Aprova o pagamento se ainda estiver dentro do prazo de 10 minutos.
     *
     * @throws PagamentoException se o código Pix expirou (o status vira {@link Pagamento.Status#RECUSADO})
     *         ou se o pagamento já tinha sido processado
     */
    @Override
    public void processarPagamento() throws PagamentoException {
        if (LocalDateTime.now().isAfter(expiraEm)) {
            mudarStatus(Status.RECUSADO);
            throw new PagamentoException("o código Pix expirou");
        }
        mudarStatus(Status.APROVADO);
    }

    /**
     * {@inheritDoc}
     *
     * @return sempre {@link Pagamento.Forma#PIX}
     */
    @Override
    public Forma getForma() { return Forma.PIX; }

    /**
     * Devolve a chave Pix que recebe o pagamento.
     *
     * @return a chave Pix
     */
    public String getChavePix() { return chavePix; }

    /**
     * Devolve o momento em que o código Pix deixa de valer.
     *
     * @return a data e hora de expiração
     */
    public LocalDateTime getExpiraEm() { return expiraEm; }
}
