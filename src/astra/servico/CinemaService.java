package astra.servico;

import astra.modelo.*;
import astra.excecoes.AssentoIndisponivelException;

public class CinemaService {

    // METODO Q A TELA CHAMARA QD CLIENTE CLICAR EM COMPRAR
    public Ingresso venderIngresso(Cliente cliente, Sessao sessao, Assento assento, Ingresso.TipoIngresso tipoIngresso) throws AssentoIndisponivelException {

        //verifica se a sessão está disponível
        if(!sessao.estaDisponivel()){
            throw new IllegalStateException("A sessão não está disponível para venda!");
        }

        // valida se o assento ta ocupado
        if (!assento.estaDisponivel()) {
            // VERIFICAR QM TA COM ASSENTO: AssentoDisponivelException precisa de um construtor que receba String!!!!!!!
            throw new AssentoIndisponivelException("O assento já está ocupado!");
        }

        assento.ocupar(); // EXECUTA A ALTERAÇÃO

        // CRIA E RETORNA INGRESSO FINAL
        Ingresso novoIngresso = new Ingresso(1,sessao.getFilme(), sessao, assento, cliente, tipoIngresso);

        return novoIngresso; // se tivesse um bd, salvaríamos aq, mas aí só retornamos o objeto msm
    }

    // METODO Q A TELA DO ADMIN CHAMA
    public Filme cadastrarFilme(Administrador admin, String titulo, int duracaoMinutos, Filme.classificacaoIndicativa classificacaoIndicativa, String genero, String diretor, boolean ativo, boolean dublado, String sinopse) {
        // validacao simpls neh
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título do filme é obrigatório!");
        }

        // OBJETO PARA CRIAR CADASTRO
        //ARMAZENAMENTO VAI SER CRIADO DEPOIS
        Filme novoFilme = new Filme(1,titulo, duracaoMinutos, classificacaoIndicativa, genero, diretor, ativo, dublado, sinopse);

        return novoFilme;
    }
}