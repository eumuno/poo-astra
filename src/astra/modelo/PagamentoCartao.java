package astra.modelo;

import astra.excecoes.PagamentoException;
import java.time.YearMonth;

/**
 * Pagamento com cartão. É recusado se o cartão estiver vencido.
 * Por segurança, guarda apenas os 4 últimos dígitos do número.
 */
public class PagamentoCartao extends Pagamento {

    /** Nome do titular impresso no cartão. */
    private final String titular;
    /** Só os 4 últimos dígitos do cartão, por segurança. */
    private final String finalCartao;
    /** Mês e ano de validade do cartão. */
    private final YearMonth validade;

    /**
     * Cria um pagamento com cartão pendente. O número completo não é guardado:
     * apenas os 4 últimos dígitos.
     *
     * @param valor valor a ser cobrado, em reais
     * @param titular nome do titular do cartão
     * @param numeroCartao número do cartão (com pelo menos 4 dígitos)
     * @param validade mês e ano de validade do cartão
     */
    public PagamentoCartao(double valor, String titular, String numeroCartao, YearMonth validade) {
        super(valor);
        this.titular = titular;
        this.finalCartao = numeroCartao.substring(numeroCartao.length() - 4);
        this.validade = validade;
    }

    /**
     * Aprova o pagamento se o cartão ainda estiver dentro da validade
     * (o cartão vale até o fim do mês de validade).
     *
     * @throws PagamentoException se o cartão estiver vencido (o status vira {@link Pagamento.Status#RECUSADO})
     *         ou se o pagamento já tinha sido processado
     */
    @Override
    public void processarPagamento() throws PagamentoException {
        if (validade.isBefore(YearMonth.now())) {
            mudarStatus(Status.RECUSADO);
            throw new PagamentoException("cartão vencido");
        }
        mudarStatus(Status.APROVADO);
    }

    /**
     * {@inheritDoc}
     *
     * @return sempre {@link Pagamento.Forma#CARTAO}
     */
    @Override
    public Forma getForma() { return Forma.CARTAO; }

    /**
     * Devolve o nome do titular do cartão.
     *
     * @return o titular
     */
    public String getTitular() { return titular; }

    /**
     * Devolve os 4 últimos dígitos do cartão.
     *
     * @return o final do cartão
     */
    public String getFinalCartao() { return finalCartao; }

    /**
     * Devolve a validade do cartão.
     *
     * @return o mês e ano de validade
     */
    public YearMonth getValidade() { return validade; }
}
