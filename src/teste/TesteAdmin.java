package teste;

import astra.excecoes.PersistenciaException;
import astra.modelo.*;
import astra.persistencia.*;
import astra.servico.FilmeService;
import astra.servico.ProdutoService;
import astra.servico.SessaoService;
import astra.servico.VendaService;
import astra.servico.pagamento.FormaPagamento;
import astra.servico.pagamento.FormaPagamentoPix;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Teste da administração: unicidade de CPF e e-mail, edição de filme, edição de produto,
 * reposição de estoque e alteração/cancelamento de sessão, com os repositórios JDBC de verdade
 * num banco temporário (não mexe no astra.db). Rode a partir da raiz do projeto.
 */
public class TesteAdmin {

    private TesteAdmin() { }
    static int ok = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError("FALHOU: " + msg);
        ok++;
        System.out.println("OK  " + msg);
    }

    static String texto(ConexaoBanco banco, String sql) throws Exception {
        try (Connection c = banco.abrirConexao(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    static boolean temSessao(List<Sessao> lista, int id) {
        return lista.stream().anyMatch(s -> s.getId() == id);
    }

    /**
     * Executa todas as verificações.
     *
     * @param a não usado
     * @throws Exception se alguma verificação ou o banco falhar
     */
    public static void main(String[] a) throws Exception {
        java.io.File arquivo = java.io.File.createTempFile("teste_admin", ".db");
        arquivo.deleteOnExit();
        ConexaoBanco banco = new ConexaoBanco("jdbc:sqlite:" + arquivo.getAbsolutePath());
        banco.inicializar();

        FilmeRepositorio filmes = new FilmeRepositorioJdbc(banco);
        ProdutoRepositorio produtos = new ProdutoRepositorioJdbc(banco);
        SessaoRepositorio sessoes = new SessaoRepositorioJdbc(banco, filmes);
        UsuarioRepositorio usuarios = new UsuarioRepositorioJdbc(banco);
        IngressoRepositorio ingressos = new IngressoRepositorioJdbc(banco, sessoes, usuarios);
        PedidoRepositorio pedidos = new PedidoRepositorioJdbc(banco, usuarios, produtos, ingressos);
        List<FormaPagamento> formas = List.of(new FormaPagamentoPix());
        VendaService venda = new VendaService(formas, pedidos, ingressos, produtos);
        FilmeService fs = new FilmeService(filmes);
        ProdutoService ps = new ProdutoService(produtos);
        SessaoService ss = new SessaoService(sessoes);
        Administrador adm = (Administrador) usuarios.autenticar("admin@astra", "123").get();
        Cliente cliente = (Cliente) usuarios.autenticar("cliente@email.com", "123").get();

        // ---------------- CPF E E-MAIL ÚNICOS ----------------
        usuarios.salvar(new Cliente(0, "Ana", "ana@x.com", "", "111.111.111-11"), "senha123");
        check("11111111111".equals(texto(banco, "SELECT cpf FROM usuario WHERE email = 'ana@x.com'")), "CPF é gravado só com dígitos");
        try { usuarios.salvar(new Cliente(0, "Clone", "outro@x.com", "", "11111111111"), "senha123"); check(false, "CPF repetido"); }
        catch (PersistenciaException e) { check(e.getMessage().contains("CPF"), "mesmo CPF (formatado ou não) é recusado: " + e.getMessage()); }
        try { usuarios.salvar(new Cliente(0, "Maiuscula", "ANA@x.com", "", "22222222222"), "senha123"); check(false, "e-mail repetido"); }
        catch (PersistenciaException e) { check(e.getMessage().contains("e-mail"), "mesmo e-mail trocando maiúsculas é recusado: " + e.getMessage()); }
        check("1".equals(texto(banco, "SELECT COUNT(*) FROM usuario WHERE lower(email) = 'ana@x.com'")), "nenhuma conta duplicada ficou gravada");
        check(usuarios.autenticar("ana@x.com", "senha123").isPresent(), "a conta original continua conseguindo entrar");
        usuarios.salvar(new Cliente(0, "Bia", "  bia@x.com ", "", "33333333333"), "senha123");
        check(usuarios.autenticar("bia@x.com", "senha123").isPresent(), "e-mail com espaços nas pontas é gravado sem eles");

        // ---------------- FILMES ----------------
        Filme cidade = fs.cadastrarFilme(adm, "Tropa de Elite", 130, Filme.ClassificacaoIndicativa.DEZESSEIS, "Drama", "José Padilha", true, false, "Anos 70.");
        check(cidade.getId() > 0, "filme cadastrado recebe id");
        try { fs.cadastrarFilme(adm, " tropa de ELITE ", 100, Filme.ClassificacaoIndicativa.LIVRE, "x", "y", true, false, ""); check(false, "título repetido"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("Já existe um filme"), "título repetido (sem diferenciar maiúsculas) é recusado"); }

        Filme editado = fs.atualizarFilme(adm, cidade.getId(), "Tropa de Elite (Restaurado)", 135, Filme.ClassificacaoIndicativa.DEZOITO, "Crime", "J. Padilha", true, true, "Nova sinopse");
        Filme lido = filmes.buscarPorId(cidade.getId()).get();
        check(lido.getTitulo().equals("Tropa de Elite (Restaurado)") && lido.getDuracaoMinutos() == 135 && lido.isDublado()
                        && lido.getClassificacaoIndicativa() == Filme.ClassificacaoIndicativa.DEZOITO && lido.getGenero().equals("Crime") && lido.getSinopse().equals("Nova sinopse"),
                "editar filme grava todos os campos");
        check(filmes.listarTodos().stream().filter(f -> f.getTitulo().startsWith("Tropa de Elite")).count() == 1, "editar não cria filme novo");
        fs.atualizarFilme(adm, cidade.getId(), "Tropa de Elite (Restaurado)", 136, Filme.ClassificacaoIndicativa.DEZOITO, "Crime", "J. Padilha", true, true, "");
        check(filmes.buscarPorId(cidade.getId()).get().getDuracaoMinutos() == 136, "regravar mantendo o próprio título é permitido");
        try { fs.atualizarFilme(adm, cidade.getId(), "O Auto da Compadecida", 100, Filme.ClassificacaoIndicativa.LIVRE, "x", "y", true, false, ""); check(false, "título de outro filme"); }
        catch (IllegalArgumentException e) { check(true, "não deixa trocar o título para o de outro filme"); }
        try { fs.atualizarFilme(adm, 9999, "Fantasma", 100, Filme.ClassificacaoIndicativa.LIVRE, "x", "y", true, false, ""); check(false, "id inexistente"); }
        catch (PersistenciaException e) { check(true, "editar filme que não existe lança PersistenciaException"); }
        try { fs.atualizarFilme(null, cidade.getId(), "X", 100, Filme.ClassificacaoIndicativa.LIVRE, "x", "y", true, false, ""); check(false, "sem admin"); }
        catch (IllegalArgumentException e) { check(true, "sem administrador não edita filme"); }
        try { fs.atualizarFilme(adm, cidade.getId(), "X", 0, Filme.ClassificacaoIndicativa.LIVRE, "x", "y", true, false, ""); check(false, "duração 0"); }
        catch (IllegalArgumentException e) { check(true, "duração zero é recusada na edição"); }
        try { fs.atualizarFilme(adm, cidade.getId(), "X", 90, Filme.ClassificacaoIndicativa.LIVRE, "  ", "y", true, false, ""); check(false, "gênero vazio"); }
        catch (IllegalArgumentException e) { check(true, "gênero vazio é recusado"); }
        try { fs.atualizarFilme(adm, 0, "X", 90, Filme.ClassificacaoIndicativa.LIVRE, "x", "y", true, false, ""); check(false, "id 0"); }
        catch (IllegalArgumentException e) { check(true, "editar sem escolher um filme é recusado"); }

        // ---------------- PRODUTOS E ESTOQUE ----------------
        Produto choc = ps.cadastrarProduto(adm, "Chocolate", "barra", 6.0, 10, Produto.CategoriaProduto.DOCE);
        try { ps.cadastrarProduto(adm, "chocolate", "", 5, 1, Produto.CategoriaProduto.DOCE); check(false, "nome repetido"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("Já existe um produto"), "nome de produto repetido é recusado"); }

        check(ps.reporEstoque(adm, choc.getId(), 5).getEstoque() == 15, "repor 5 unidades: 10 -> 15");
        check(ps.reporEstoque(adm, choc.getId(), 3).getEstoque() == 18, "reposições se acumulam: 15 -> 18");
        try { ps.reporEstoque(adm, choc.getId(), 0); check(false, "qtd 0"); }
        catch (IllegalArgumentException e) { check(true, "repor zero unidade é recusado"); }
        try { ps.reporEstoque(adm, choc.getId(), -4); check(false, "qtd negativa"); }
        catch (IllegalArgumentException e) { check(true, "repor quantidade negativa é recusado"); }
        try { ps.reporEstoque(adm, 9999, 5); check(false, "produto inexistente"); }
        catch (PersistenciaException e) { check(true, "repor estoque de produto que não existe lança PersistenciaException"); }
        try { ps.reporEstoque(null, choc.getId(), 5); check(false, "sem admin"); }
        catch (IllegalArgumentException e) { check(true, "sem administrador não repõe estoque"); }

        Produto depois = ps.atualizarProduto(adm, choc.getId(), "Chocolate ao Leite", "barra 90g", 7.5, Produto.CategoriaProduto.DOCE);
        Produto lidoP = produtos.buscarPorId(choc.getId()).get();
        check(lidoP.getNome().equals("Chocolate ao Leite") && lidoP.getPreco() == 7.5 && lidoP.getDescricao().equals("barra 90g"), "editar produto grava nome, descrição e preço");
        check(lidoP.getEstoque() == 18 && depois.getEstoque() == 18, "editar produto NÃO mexe no estoque (continua 18)");

        Produto velho = produtos.buscarPorId(choc.getId()).get();            // estoque 18 na memória
        ps.reporEstoque(adm, choc.getId(), 2);                               // banco vai para 20
        produtos.atualizarDados(new Produto(velho.getId(), "Chocolate Premium", "d", 9.0, velho.getEstoque(), Produto.CategoriaProduto.DOCE));
        check(produtos.buscarPorId(choc.getId()).get().getEstoque() == 20, "objeto desatualizado não sobrescreve o estoque (continua 20)");

        try { ps.atualizarProduto(adm, choc.getId(), "Pipoca Grande", "", 5, Produto.CategoriaProduto.PIPOCA); check(false, "nome de outro"); }
        catch (IllegalArgumentException e) { check(true, "não deixa trocar o nome para o de outro produto"); }
        try { ps.atualizarProduto(adm, choc.getId(), "X", "", -1, Produto.CategoriaProduto.DOCE); check(false, "preço negativo"); }
        catch (IllegalArgumentException e) { check(true, "preço negativo é recusado na edição"); }
        try { ps.atualizarProduto(adm, choc.getId(), "X", "", Double.NaN, Produto.CategoriaProduto.DOCE); check(false, "preço NaN"); }
        catch (IllegalArgumentException e) { check(true, "preço inválido (NaN) é recusado"); }
        try { ps.atualizarProduto(adm, 9999, "X", "", 5, Produto.CategoriaProduto.DOCE); check(false, "id inexistente"); }
        catch (PersistenciaException e) { check(true, "editar produto que não existe lança PersistenciaException"); }

        // reposição convivendo com uma venda (a venda abate; a reposição soma)
        Pedido pedidoChoc = venda.iniciarPedido(cliente);
        venda.adicionarProduto(pedidoChoc, produtos.buscarPorId(choc.getId()).get(), 4);
        venda.finalizarPedido(pedidoChoc, formas.get(0));
        check(produtos.buscarPorId(choc.getId()).get().getEstoque() == 16, "venda de 4 abate o estoque: 20 -> 16");
        ps.reporEstoque(adm, choc.getId(), 10);
        check(produtos.buscarPorId(choc.getId()).get().getEstoque() == 26, "reposição depois da venda soma sobre o valor atual: 16 -> 26");

        // ---------------- SESSÕES ----------------
        LocalDateTime base = LocalDateTime.now().plusDays(10).withHour(20).withMinute(0).withSecond(0).withNano(0);
        Sessao s1 = ss.cadastrarSessao(adm, cidade, 1, base, 20.0);
        Sessao s2 = ss.cadastrarSessao(adm, cidade, 1, base.plusDays(3), 18.0);
        check(temSessao(ss.listarSessoes(), s1.getId()) && temSessao(ss.listarSessoesDisponiveis(), s1.getId()), "sessão cadastrada aparece nas listas");

        ss.atualizarSessao(adm, s1.getId(), cidade, 1, base.plusDays(1), 25.0);
        Sessao s1b = sessoes.buscarPorId(s1.getId()).get();
        check(s1b.getDataHora().equals(base.plusDays(1)) && s1b.getPrecoBase() == 25.0, "editar sessão grava data/hora e preço");
        check(sessoes.listarTodos().stream().filter(x -> x.getId() == s1.getId()).count() == 1, "editar não cria sessão nova");

        try { ss.atualizarSessao(adm, s1.getId(), cidade, 1, base.plusDays(3), 25.0); check(false, "conflito de horário"); }
        catch (PersistenciaException e) { check(e.getMessage().contains("já tem uma sessão"), "mover para horário ocupado da sala é recusado: " + e.getMessage()); }
        check(sessoes.buscarPorId(s1.getId()).get().getDataHora().equals(base.plusDays(1)), "a sessão continua no horário anterior depois da recusa");

        Filme inativo = fs.cadastrarFilme(adm, "Filme Inativo", 90, Filme.ClassificacaoIndicativa.LIVRE, "x", "y", false, false, "");
        try { ss.atualizarSessao(adm, s1.getId(), inativo, 1, base.plusDays(1), 25.0); check(false, "filme inativo"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("não está ativo"), "sessão não aceita filme inativo"); }
        try { ss.atualizarSessao(adm, s1.getId(), cidade, 1, LocalDateTime.now().minusHours(1), 25.0); check(false, "data passada"); }
        catch (IllegalArgumentException e) { check(true, "data no passado é recusada na edição"); }
        try { ss.atualizarSessao(adm, s1.getId(), cidade, 77, base.plusDays(1), 25.0); check(false, "sala inexistente"); }
        catch (PersistenciaException e) { check(true, "sala que não existe é recusada na edição"); }
        try { ss.atualizarSessao(adm, 9999, cidade, 1, base.plusDays(1), 25.0); check(false, "sessão inexistente"); }
        catch (IllegalArgumentException e) { check(true, "editar sessão que não existe é recusado"); }

        // uma venda na sessão s2: a partir daí ela fica "congelada"
        Sessao s2carregada = sessoes.buscarPorId(s2.getId()).get();
        Pedido pedidoIngresso = venda.iniciarPedido(cliente);
        venda.venderIngresso(pedidoIngresso, s2carregada, 4, Ingresso.TipoIngresso.INTEIRA);
        venda.finalizarPedido(pedidoIngresso, formas.get(0));
        check(ss.contarIngressosVendidos(sessoes.buscarPorId(s2.getId()).get()) == 1, "contarIngressosVendidos enxerga o ingresso pago");
        try { ss.atualizarSessao(adm, s2.getId(), cidade, 1, base.plusDays(5), 10.0); check(false, "sessão vendida"); }
        catch (IllegalStateException e) { check(e.getMessage().contains("1 ingresso"), "sessão com ingresso vendido não pode ser alterada: " + e.getMessage()); }
        try { ss.cancelarSessao(adm, s2.getId()); check(false, "cancelar vendida"); }
        catch (IllegalStateException e) { check(e.getMessage().contains("cancelada"), "sessão com ingresso vendido não pode ser cancelada: " + e.getMessage()); }
        check(sessoes.buscarPorId(s2.getId()).get().getStatus() == Sessao.StatusSessao.AGENDADA, "a sessão vendida continua agendada");

        // desativar o filme tira as sessões dele da lista do cliente (sem apagar nada)
        fs.atualizarFilme(adm, cidade.getId(), "Tropa de Elite (Restaurado)", 136, Filme.ClassificacaoIndicativa.DEZOITO, "Crime", "J. Padilha", false, true, "");
        check(!temSessao(ss.listarSessoesDisponiveis(), s2.getId()) && temSessao(ss.listarSessoes(), s2.getId()), "filme desativado: sessão some do cliente mas continua para o administrador");
        fs.atualizarFilme(adm, cidade.getId(), "Tropa de Elite (Restaurado)", 136, Filme.ClassificacaoIndicativa.DEZOITO, "Crime", "J. Padilha", true, true, "");
        check(temSessao(ss.listarSessoesDisponiveis(), s2.getId()), "filme reativado: a sessão volta para o cliente");

        // cancelar a sessão s1 (sem vendas)
        Sessao cancelada = ss.cancelarSessao(adm, s1.getId());
        check(cancelada.getStatus() == Sessao.StatusSessao.CANCELADA && sessoes.buscarPorId(s1.getId()).get().getStatus() == Sessao.StatusSessao.CANCELADA, "cancelar grava o status CANCELADA");
        check(!temSessao(ss.listarSessoesDisponiveis(), s1.getId()) && temSessao(ss.listarSessoes(), s1.getId()), "sessão cancelada some do cliente mas continua registrada");
        try { ss.cancelarSessao(adm, s1.getId()); check(false, "cancelar de novo"); }
        catch (IllegalStateException e) { check(true, "cancelar duas vezes é recusado"); }
        try { ss.atualizarSessao(adm, s1.getId(), cidade, 1, base.plusDays(9), 10.0); check(false, "editar cancelada"); }
        catch (IllegalStateException e) { check(true, "sessão cancelada não pode ser editada"); }
        try { ss.cancelarSessao(null, s2.getId()); check(false, "sem admin"); }
        catch (IllegalArgumentException e) { check(true, "sem administrador não cancela sessão"); }

        System.out.println("\nTODOS OS " + ok + " TESTES PASSARAM");
    }
}