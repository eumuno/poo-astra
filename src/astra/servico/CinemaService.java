package astra.servico;

import astra.modelo.*;
import astra.excecoes.AssentoIndisponivelException;

public class CinemaService {

    // METODO Q A TELA CHAMARA QD CLIENTE CLICAR EM COMPRAR
    public Ingresso venderIngresso(Cliente cliente, Sessao sessao, Assento assento, Ingresso.TipoIngresso tipoIngresso) throws AssentoIndisponivelException {

        // VERIFICA SE A SESSAO TA DISPONIVEL
        if(!sessao.estaDisponivel()){
            throw new IllegalStateException("A sessão não está disponível para venda!");
        }

        // VALIDA SE O ASSENTO TA OCUPADO
        if (!assento.estaDisponivel()) {
            throw new AssentoIndisponivelException("O assento já está ocupado!");
        }

        assento.ocupar(); // EXECUTA A ALTERAÇÃO

        // CRIA E RETORNA INGRESSO FINAL
        Ingresso novoIngresso = new Ingresso(1,sessao.getFilme(), sessao, assento, cliente, tipoIngresso);

        return novoIngresso; // SE TIVESSE BD, SALVARÍAMOS AQ, MAS AÍ SÓ RETORNA O OBJETO MSM
    }

    // METODOS Q A TELA DO ADMIN CHAMA
    public void cadastrarFilme(Administrador admin, String titulo, int duracaoMinutos, Filme.classificacaoIndicativa classificacaoIndicativa, String genero, String diretor, boolean ativo, boolean dublado, String sinopse) {
        // VALIDA SE TITULO TA EM BRANCO
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título do filme é obrigatório!");
        }

        Filme novoFilme = new Filme(1,titulo, duracaoMinutos, classificacaoIndicativa, genero, diretor, ativo, dublado, sinopse);
    }

    public void cadastrarProduto(Administrador admin, String nome, String descricao, double preco, int estoque, Produto.CategoriaProduto categoria) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório!");
        }
        Produto novoProduto = new Produto(1, nome, descricao, preco, estoque, categoria);
    }
}