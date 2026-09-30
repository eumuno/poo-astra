package astra.modelo;

import astra.excecoes.PagamentoException;
import java.time.YearMonth;

// Herança: PagamentoCartao É UM Pagamento
public class PagamentoCartao extends Pagamento {

    private final String titular;
    private final String finalCartao; // só os 4 últimos dígitos, por segurança
    private final YearMonth validade;

    public PagamentoCartao(double valor, String titular, String numeroCartao, YearMonth validade) {
        super(valor);
        this.titular = titular;
        this.finalCartao = numeroCartao.substring(numeroCartao.length() - 4);
        this.validade = validade;
    }

    // Polimorfismo: o Cartão processa do jeito dele
    @Override
    public void processarPagamento() throws PagamentoException {
        if (validade.isBefore(YearMonth.now())) {
            mudarStatus(Status.RECUSADO);
            throw new PagamentoException("Cartão vencido. Use outro cartão.");
        }
        mudarStatus(Status.APROVADO);
    }

    public String getTitular() { return titular; }
    public String getFinalCartao() { return finalCartao; }
    public YearMonth getValidade() { return validade; }
}