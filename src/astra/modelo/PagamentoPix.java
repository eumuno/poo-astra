package astra.modelo;

import astra.excecoes.PagamentoException;
import java.time.LocalDateTime;

// Herança: PagamentoPix É UM Pagamento
public class PagamentoPix extends Pagamento {

    private String chavePix;
    private LocalDateTime expiraEm;

    public PagamentoPix(double valor, String chavePix) {
        super(valor); // chama o construtor do Pagamento (valor, data, status)
        this.chavePix = chavePix;
        this.expiraEm = LocalDateTime.now().plusMinutes(10); // Pix vale 10 minutos
    }

    // Polimorfismo: o Pix processa do jeito dele
    @Override
    public void processarPagamento() throws PagamentoException {
        // Regra: se passou do prazo, o Pix é recusado
        if (LocalDateTime.now().isAfter(expiraEm)) {
            mudarStatus(statusPagamento.RECUSADO);
            throw new PagamentoException("O Pix expirou. Gere um novo código.");
        }
        mudarStatus(statusPagamento.APROVADO);
    }

    public String getChavePix() {
        return chavePix;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }
}