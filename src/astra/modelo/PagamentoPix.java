package astra.modelo;

import astra.excecoes.PagamentoException;
import java.time.LocalDateTime;

// Herança: PagamentoPix É UM Pagamento
public class PagamentoPix extends Pagamento {

    private final String chavePix;
    private final LocalDateTime expiraEm = getData().plusMinutes(10); // Pix vale 10 minutos

    public PagamentoPix(double valor, String chavePix) {
        super(valor);
        this.chavePix = chavePix;
    }

    // Polimorfismo: o Pix processa do jeito dele
    @Override
    public void processarPagamento() throws PagamentoException {
        if (LocalDateTime.now().isAfter(expiraEm)) {
            mudarStatus(Status.RECUSADO);
            throw new PagamentoException("O Pix expirou. Gere um novo código.");
        }
        mudarStatus(Status.APROVADO);
    }

    public String getChavePix() { return chavePix; }
    public LocalDateTime getExpiraEm() { return expiraEm; }
}