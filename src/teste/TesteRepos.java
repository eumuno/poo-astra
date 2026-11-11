package teste;

import astra.excecoes.PersistenciaException;
import astra.modelo.*;
import astra.persistencia.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class TesteRepos {
    static int ok = 0;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError("FALHOU: " + msg); ok++; System.out.println("OK  " + msg); }

    public static void main(String[] a) throws Exception {
        // banco temporário, separado do astra.db de verdade (funciona em Windows, Mac e Linux)
        java.io.File arquivo = java.io.File.createTempFile("teste_astra", ".db");
        arquivo.deleteOnExit();
        ConexaoBanco banco = new ConexaoBanco("jdbc:sqlite:" + arquivo.getAbsolutePath());
        banco.inicializar();
        FilmeRepositorio filmes = new FilmeRepositorioJdbc(banco);
        ProdutoRepositorio produtos = new ProdutoRepositorioJdbc(banco);
        SessaoRepositorio sessoes = new SessaoRepositorioJdbc(banco, filmes);

        // FILME
        List<Filme> fs = filmes.listarTodos();
        check(fs.size() == 3, "seed: 3 filmes");
        check(fs.get(0).getClassificacaoIndicativa() == Filme.ClassificacaoIndicativa.DOZE && fs.get(0).isDublado() && fs.get(0).isAtivo(), "mapeamento de enum e boolean do filme");
        Filme novo = filmes.salvar(new Filme(0, "Tropa de Elite", 115, Filme.ClassificacaoIndicativa.DEZESSEIS, "Ação", "José Padilha", true, false, "Capitão Nascimento."));
        check(novo.getId() > 0, "filme novo recebe id " + novo.getId());
        check(filmes.listarTodos().size() == 4, "agora 4 filmes");
        Filme editado = filmes.salvar(new Filme(novo.getId(), "Tropa de Elite 2", 116, Filme.ClassificacaoIndicativa.DEZESSEIS, "Ação", "José Padilha", false, false, "x"));
        check(filmes.buscarPorId(novo.getId()).get().getTitulo().equals("Tropa de Elite 2") && !filmes.buscarPorId(novo.getId()).get().isAtivo(), "UPDATE de filme");
        check(!filmes.buscarPorId(999).isPresent(), "buscarPorId inexistente = vazio");
        try { filmes.salvar(new Filme(999, "X", 1, Filme.ClassificacaoIndicativa.LIVRE, "", "", true, true, "")); check(false, "update de id inexistente"); }
        catch (PersistenciaException e) { check(true, "update de id inexistente lança PersistenciaException: " + e.getMessage()); }
        try { filmes.salvar(new Filme(0, "Ruim", 0, Filme.ClassificacaoIndicativa.LIVRE, "", "", true, true, "")); check(false, "duração 0"); }
        catch (PersistenciaException e) { check(e.getCause() instanceof SQLException, "CHECK do banco recusa duração 0 e vira PersistenciaException"); }

        // PRODUTO
        check(produtos.listarTodos().size() == 1, "seed: 1 produto");
        Produto p = produtos.salvar(new Produto(0, "Refrigerante", "500ml", 8.5, 30, Produto.CategoriaProduto.BEBIDA));
        check(p.getId() > 0, "produto novo recebe id");
        p.retirarEstoque(5);
        produtos.salvar(p);
        check(produtos.buscarPorId(p.getId()).get().getEstoque() == 25, "UPDATE do estoque (baixa de 5)");
        try { produtos.salvar(new Produto(0, "Negativo", "", 1, -1, Produto.CategoriaProduto.DOCE)); check(false, "estoque negativo"); }
        catch (PersistenciaException e) { check(true, "estoque negativo recusado"); }

        // SESSAO
        List<Sessao> ss = sessoes.listarTodos();
        check(ss.size() == 1, "seed: 1 sessão");
        Sessao s1 = ss.get(0);
        check(s1.getFilme().getTitulo().equals("O Auto da Compadecida") && s1.getSala().getAssentos().size() == 10 && s1.estaDisponivel(), "sessão carregada com filme + sala + 10 assentos + status");
        check(s1.assentoEstaDisponivel(3), "assento 3 livre antes da venda");

        // simula uma venda direto no banco (ingresso do assento 3)
        try (Connection c = banco.abrirConexao(); Statement st = c.createStatement()) {
            st.execute("INSERT INTO pedido (cliente_id, data, status) VALUES (2, '2026-11-01T10:00', 'PAGO')");
            st.execute("INSERT INTO ingresso (pedido_id, sessao_id, assento_id, tipo, valor_pago) "
                    + "SELECT 1, " + s1.getId() + ", id, 'INTEIRA', 15 FROM assento WHERE numero = 3");
        }
        Sessao s1b = sessoes.buscarPorId(s1.getId()).get();
        check(!s1b.assentoEstaDisponivel(3) && s1b.assentoEstaDisponivel(4), "assento vendido aparece ocupado na Sessao carregada");

        Sessao nova = sessoes.salvar(new Sessao(0, novo, s1.getSala(), LocalDateTime.of(2030, 1, 10, 21, 0), Sessao.StatusSessao.AGENDADA));
        check(nova.getId() > 0, "sessão nova recebe id");
        check(sessoes.listarTodos().size() == 2 && sessoes.buscarPorId(nova.getId()).get().getDataHora().equals(LocalDateTime.of(2030, 1, 10, 21, 0)), "data/hora volta igual do banco");
        try { sessoes.salvar(new Sessao(0, novo, s1.getSala(), LocalDateTime.of(2030, 1, 10, 21, 0), Sessao.StatusSessao.AGENDADA)); check(false, "sessão duplicada"); }
        catch (PersistenciaException e) { check(e.getMessage().contains("já tem uma sessão"), "horário duplicado: " + e.getMessage()); }
        try { sessoes.salvar(new Sessao(0, novo, new Sala(77, new java.util.ArrayList<>()), LocalDateTime.of(2030, 2, 1, 10, 0), Sessao.StatusSessao.AGENDADA)); check(false, "sala inexistente"); }
        catch (PersistenciaException e) { check(true, "sala inexistente: " + e.getMessage()); }
        try { sessoes.salvar(new Sessao(0, new Filme(0, "Nao gravado", 90, Filme.ClassificacaoIndicativa.LIVRE, "", "", true, true, ""), s1.getSala(), LocalDateTime.of(2030, 3, 1, 10, 0), Sessao.StatusSessao.AGENDADA)); check(false, "filme não gravado"); }
        catch (PersistenciaException e) { check(true, "filme não gravado: " + e.getMessage()); }
        System.out.println("\nTODOS OS " + ok + " TESTES PASSARAM");
    }
}
