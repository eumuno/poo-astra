package astra.servico.pagamento;
import astra.modelo.Cliente;
import astra.modelo.Pagamento;

//Uma forma de pagamento que o cliente pode escolher
//A tela cria um botão para cada forma de pagamento, sem conhecer tipos concretos

public interface FormaPagamento {

    //nome para o botão
    String getNome();

    Pagamento criarPagamento(double calor, Cliente cliente);
}
