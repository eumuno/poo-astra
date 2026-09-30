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

        // CRIA LISTA D ASSENTOS REAIS P SALA D TESTE
        List<Assento> assentosDaSala = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            assentosDaSala.add(new Assento(i, Assento.tipoAssento.COMUM));
        }

        // CRIA A SALA C NOVO CONSTRUTOR
        Sala sala1 = new Sala(1, assentosDaSala);

        sessaoUnicaParaTeste = new Sessao(1, filmeCompadecida, sala1, LocalDateTime.now().plusDays(1), Sessao.statusSessao.AGENDADA);
    }

    // METODOS Q A TELA DO ADMIN CHAMA
    public Filme cadastrarFilme(Administrador admin, String titulo, int duracaoMinutos, Filme.classificacaoIndicativa classificacaoIndicativa, String genero, String diretor, boolean ativo, boolean dublado, String sinopse) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título do filme é obrigatório!");
        }

        Filme novoFilme = new Filme(filmes.size() + 1, titulo, duracaoMinutos, classificacaoIndicativa, genero, diretor, ativo, dublado, sinopse);
        filmes.add(novoFilme); // add na lista

        return novoFilme;
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

    public List<Filme> getFilmesDisponiveis() {
        return filmes;
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

        int numeroAssento = assento.getNumero();

        // VALIDA SE O ASSENTO TA OCUPADO
        if(!sessao.assentoEstaDisponivel(numeroAssento)){
            throw new AssentoIndisponivelException("O assento " + numeroAssento + " não existe na sala ou já está ocupado!");
        }

        // OCUPA ASSENTO NA MEMÓRIA DA SESSÃO
        sessao.ocuparAssento(numeroAssento);
        Assento assentoDaSala = sessao.getSala().buscarAssento(numeroAssento);

        return new Ingresso(1, sessao.getFilme(), sessao, assentoDaSala, cliente, tipoIngresso);
    }
}