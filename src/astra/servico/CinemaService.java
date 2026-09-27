package astra.servico;

import astra.modelo.*;
import astra.excecoes.AssentoIndisponivelException;

public class CinemaService {

    // METODO Q A TELA CHAMARA QD CLIENTE CLICAR EM COMPRAR
    public Ingresso venderIngresso(Cliente cliente, Sessao sessao, Assento assento) throws AssentoIndisponivelException {
        // valida se o assento ta ocupado
        if (!assento.estaDisponivel()) {
            throw new AssentoIndisponivelException("O assento já está ocupado!");
        }

        assento.ocupar(); // EXECUTA A ALTERAÇÃO

        // CRIA E RETORNA INGRESSO FINAL
        Ingresso novoIngresso = new Ingresso(/* POR FAVOR, LEMBREM D COLOCAR OS PARANGOLE DO CONSTRUTOR D INGRESSO AKI GLR */);

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
}