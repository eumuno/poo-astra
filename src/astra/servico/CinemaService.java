package astra.servico;

import astra.modelo.*;
import astra.excecoes.AssentoIndisponivelException;

public class CinemaService {

    // METODO QUE  A TELA CHAMARA QD CLIENTE CLICAR EM COMPRAR
    public Ingresso venderIngresso(Cliente cliente, Sessao sessao, Assento assento) throws AssentoIndisponivelException {
        // valida se o assento ta ocupado
        if (!assento.estaDisponivel()) {
            throw new AssentoIndisponivelException("O assento já está ocupado!");
        }

        assento.ocupar(); // EXECUTA A ALTERAÇÃO

        // CRIA E RETORNA INGRESSO FINAL
        Ingresso novoIngresso = new Ingresso (cliente, sessao, assento);
        return novoIngresso; // se tivesse um bd, salvaríamos aq, mas aí só retornamos o objeto msm
    }

    // METODO Q A TELA DO ADMIN CHAMA
    public void cadastrarFilme(Administrador admin, String titulo, String sinopse, int duracao) {
        // validacao simpls neh
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título do filme é obrigatório!");
        }

        Filme novoFilme = new Filme(/* botem aki */);
    }
        // Realiza a venda de um produto para o cliente e Baixa o estoque do produto e retorna o valor total da compra.

     public double venderProduto(Cliente cliente, Produto produto, int quantidade){

         if (quantidade <= 0) {   //Validação: verifica se a quantidade pedida é válida
             throw new IllegalArgumentException("A quantidade deve ser maior que zero!");
         }
         //Validação: usa o metodo do proprio Produto para ver se ha unidades suficientes
         if (!produto.possuiEstoque(quantidade)) {
             throw new IllegalStateException("Estoque insuficiente para o produto: " + produto.getNome());
         }
         produto.retirarEstoque(quantidade);  //3. Ação: chama o metodo do Produto para subtrair a quantidade vendida

         //Cálculo: calcula o valor total gasto nessa venda de produtos
         double valorTotal = produto.getPreco() * quantidade;

         return valorTotal;
     }
}