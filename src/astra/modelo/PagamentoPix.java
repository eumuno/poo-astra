package astra.modelo;

import astra.excecoes.PagamentoException;
import java.time.LocalDateTime;

public class PagamentoPix extends Pagamento {

    private final String chavePix;
    private final LocalDateTime expiraEm = getData().plusMinutes(10); // Pix vale 10 minutos

    public PagamentoPix(double valor, String chavePix) {
        super(valor);
        this.chavePix = chavePix;
    }

    @Override
    public void processarPagamento() throws PagamentoException {
        if (LocalDateTime.now().isAfter(expiraEm)) {
            mudarStatus(Status.RECUSADO);
            throw new PagamentoException("O Pix expirou. Gere um novo código.");
        }
        mudarStatus(Status.APROVADO);
    }

    @Override
    public Forma getForma() { return Forma.PIX; }

    public String getChavePix() { return chavePix; }
    public LocalDateTime getExpiraEm() { return expiraEm; }
}