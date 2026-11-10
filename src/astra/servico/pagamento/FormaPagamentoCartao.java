package astra.servico.pagamento;
import astra.modelo.Cliente;
import astra.modelo.Pagamento;
import astra.modelo.PagamentoCartao;

import java.time.YearMonth;

public class FormaPagamentoCartao implements FormaPagamento {

    @Override
    public String getNome(){
        return "Cartão";
    }

    @Override
    //Numero fixo por ser simulação
    public Pagamento criarPagamento(double valor, Cliente cliente){
        return new PagamentoCartao(valor, cliente.getNome(),"1234567890123456", YearMonth.now().plusYears(1));
    }
}
