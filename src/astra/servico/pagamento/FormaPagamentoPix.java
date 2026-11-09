package astra.servico.pagamento;
import astra.modelo.Cliente;
import astra.modelo.Pagamento;
import astra.modelo.PagamentoPix;

public class FormaPagamentoPix  implements FormaPagamento{

    private static final String CHAVE_PIX_ASTRA = "pagamentos@astra.com.br";

    @Override
    public String getNome(){
        return "Pix";
    }

    @Override
    public Pagamento criarPagamento(double valor, Cliente cliente){
        return new PagamentoPix(valor, CHAVE_PIX_ASTRA);
    }
}
