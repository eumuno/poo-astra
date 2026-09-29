package astra.servico;

import astra.modelo.*;
import astra.excecoes.AssentoIndisponivelException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CinemaService {

    // LISTAS Q GUARDAM TD DO CINEMA
    private List<Filme> filmes = new ArrayList<>();
    private List<Produto> produtos = new ArrayList<>();
    private Sessao sessaoUnicaParaTeste;

    public CinemaService() {
        // DADOS PRÉ-CADASTRADOS P APRESENTAÇÃO
        Filme filmeCompadecida = new Filme(1, "O Auto da Compadecida", 104, Filme.classificacaoIndicativa.DOZE, "Comédia/Aventura", "Guel Arraes", true, true, "As aventuras de João Grilo, um sertanejo pobre e mentiroso, e Chicó, o mais covarde dos homens.");
        filmes.add(filmeCompadecida);
        produtos.add(new Produto(1, "Pipoca Grande", "Salgada", 25.0, 50, Produto.CategoriaProduto.PIPOCA));

        // CRIA UMA SALA C 5 ASSENTOS P TESTE E UMA SESSÃO
        Sala sala1 = new Sala(); // Nota: construtor ainda vazio, adaptar dps
        sessaoUnicaParaTeste = new Sessao(1, filmeCompadecida, sala1, LocalDateTime.now().plusDays(1), 15.0, Sessao.statusSessao.AGENDADA);
    }

    // METODOS Q A TELA DO ADMIN CHAMA
    public void cadastrarFilme(Administrador admin, String titulo, int duracao, Filme.classificacaoIndicativa classificacao, String genero, String diretor, boolean ativo, boolean dublado, String sinopse) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título do filme é obrigatório!");
        }

        Filme novoFilme = new Filme(filmes.size() + 1, titulo, duracao, classificacao, genero, diretor, ativo, dublado, sinopse);
        filmes.add(novoFilme); // add na lista
    }

    public void cadastrarProduto(Administrador admin, String nome, String descricao, double preco, int estoque, Produto.CategoriaProduto categoria) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório!");
        }

        Produto novoProduto = new Produto(produtos.size() + 1, nome, descricao, preco, estoque, categoria);
        produtos.add(novoProduto); // add na lista
    }

    public List<Produto> getProdutosDisponiveis() {
        return produtos;
    }

    public Filme getFilmeEmCartaz() {
        return filmes.get(0);
    }

    public Sessao getSessaoTeste() {
        return sessaoUnicaParaTeste;
    }

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

}