package astra.modelo;

import astra.excecoes.PagamentoException;
import java.time.YearMonth;

// Herança: PagamentoCartao É UM Pagamento
public class PagamentoCartao extends Pagamento {

    private String titular;
    private String finalCartao;   // guarda só os 4 últimos dígitos, por segurança
    private YearMonth validade;   // YearMonth = mês/ano, ideal para validade de cartão

    public PagamentoCartao(double valor, String titular, String numeroCartao, YearMonth validade) {
        super(valor); // chama o construtor do Pagamento (valor, data, status)
        this.titular = titular;
        this.finalCartao = numeroCartao.substring(numeroCartao.length() - 4);
        this.validade = validade;
    }

    // Polimorfismo: o Cartão processa do jeito dele
    @Override
    public void processarPagamento() throws PagamentoException {
        // Regra: cartão vencido é recusado
        if (validade.isBefore(YearMonth.now())) {
            mudarStatus(StatusPagamento.RECUSADO);
            throw new PagamentoException("Cartão vencido. Use outro cartão.");
        }
        mudarStatus(StatusPagamento.APROVADO);
    }

    public String getTitular() {
        return titular;
    }

    public String getFinalCartao() {
        return finalCartao;
    }

    public YearMonth getValidade() {
        return validade;
    }
}