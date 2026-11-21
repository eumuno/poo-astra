package teste;

import astra.excecoes.PersistenciaException;
import astra.modelo.*;
import astra.persistencia.*;
import astra.servico.SessaoService;
import astra.servico.TrilhaDeAuditoria;
import astra.servico.ControleDeAcesso;
import astra.servico.VendaService;
import astra.servico.pagamento.FormaPagamento;
import astra.servico.pagamento.FormaPagamentoPix;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Teste da regra "sessão que já começou não vende mais": no modelo (com horários fixos, sem depender
 * do relógio), na lista do cliente, na venda do ingresso e no pagamento de um carrinho cuja sessão
 * começou enquanto o cliente decidia. Usa um banco temporário e roda a partir da raiz do projeto.
 */
public class TesteDisponibilidade {

    private TesteDisponibilidade() { }
    static int ok = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError("FALHOU: " + msg);
        ok++;
        System.out.println("OK  " + msg);
    }

    static int contar(ConexaoBanco banco, String tabela) throws Exception {
        try (Connection c = banco.abrirConexao(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tabela)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * Executa todas as verificações.
     *
     * @param args não usado
     * @throws Exception se alguma verificação ou o banco falhar
     */
    public static void main(String[] args) throws Exception {
        // ---------- modelo: horários fixos ----------
        Filme filme = new Filme(1, "Teste", 100, Filme.ClassificacaoIndicativa.LIVRE, "x", "y", true, false, "");
        Sala sala = new Sala(1, new java.util.ArrayList<>());
        LocalDateTime inicio = LocalDateTime.of(2030, 1, 10, 21, 0);
        Sessao agendada = new Sessao(1, filme, sala, inicio, Sessao.StatusSessao.AGENDADA);
        check(agendada.estaDisponivel(inicio.minusMinutes(1)), "um minuto antes do início a sessão está à venda");
        check(!agendada.estaDisponivel(inicio), "na hora exata do início já não está à venda");
        check(!agendada.estaDisponivel(inicio.plusHours(1)), "depois do início não está à venda");
        check(agendada.jaComecou(inicio) && !agendada.jaComecou(inicio.minusSeconds(1)), "jaComecou: verdadeiro a partir do horário");
        Sessao cancelada = new Sessao(2, filme, sala, inicio, Sessao.StatusSessao.CANCELADA);
        check(!cancelada.estaDisponivel(inicio.minusDays(5)), "sessão cancelada nunca está à venda, mesmo antes do horário");
        Sessao encerrada = new Sessao(3, filme, sala, inicio, Sessao.StatusSessao.ENCERRADA);
        check(!encerrada.estaDisponivel(inicio.minusDays(5)), "sessão encerrada não está à venda");

        // ---------- com o banco ----------
        java.io.File arquivo = java.io.File.createTempFile("teste_disp", ".db");
        arquivo.deleteOnExit();
        ConexaoBanco banco = new ConexaoBanco("jdbc:sqlite:" + arquivo.getAbsolutePath());
        banco.inicializar();
        FilmeRepositorio filmes = new FilmeRepositorioJdbc(banco);
        ProdutoRepositorio produtos = new ProdutoRepositorioJdbc(banco);
        SessaoRepositorio sessoes = new SessaoRepositorioJdbc(banco, filmes);
        UsuarioRepositorio usuarios = new UsuarioRepositorioJdbc(banco);
        ControleDeAcesso acesso = new ControleDeAcesso(usuarios);
        TrilhaDeAuditoria trilha = new TrilhaDeAuditoria(new AuditoriaRepositorioJdbc(banco), acesso);
        IngressoRepositorio ingressos = new IngressoRepositorioJdbc(banco, sessoes, usuarios);
        PedidoRepositorio pedidos = new PedidoRepositorioJdbc(banco, usuarios, produtos, ingressos);
        List<FormaPagamento> formas = List.of(new FormaPagamentoPix());
        VendaService venda = new VendaService(formas, pedidos, ingressos, produtos, trilha);
        SessaoService ss = new SessaoService(sessoes, acesso, trilha);
        Cliente cliente = (Cliente) usuarios.autenticar("cliente@email.com", "123").get();
        Filme real = filmes.listarTodos().get(0);
        Sala salaReal = ss.listarSessoes().get(0).getSala();

        // uma sessão no passado entra no banco sem passar pelo serviço de cadastro (que a recusaria)
        Sessao passada = sessoes.salvar(new Sessao(0, real, salaReal, LocalDateTime.now().minusDays(2), Sessao.StatusSessao.AGENDADA, 15));
        check(ss.listarSessoes().stream().anyMatch(s -> s.getId() == passada.getId()), "o administrador continua vendo a sessão passada");
        check(ss.listarSessoesDisponiveis().stream().noneMatch(s -> s.getId() == passada.getId()), "o cliente NÃO vê a sessão que já passou");
        check(ss.listarSessoesDisponiveis().stream().anyMatch(s -> s.getDataHora().isAfter(LocalDateTime.now())), "as sessões futuras continuam aparecendo");

        Pedido pedido = venda.iniciarPedido(cliente);
        try { venda.venderIngresso(pedido, sessoes.buscarPorId(passada.getId()).get(), 1, Ingresso.TipoIngresso.INTEIRA); check(false, "vender para sessão passada"); }
        catch (IllegalStateException e) { check(e.getMessage().contains("já começou"), "vender para sessão que já começou: \"" + e.getMessage() + "\""); }

        Sessao futura = sessoes.salvar(new Sessao(0, real, salaReal, LocalDateTime.now().plusDays(3), Sessao.StatusSessao.CANCELADA, 15));
        try { venda.venderIngresso(pedido, sessoes.buscarPorId(futura.getId()).get(), 1, Ingresso.TipoIngresso.INTEIRA); check(false, "vender para cancelada"); }
        catch (IllegalStateException e) { check(e.getMessage().contains("não está disponível"), "vender para sessão cancelada: \"" + e.getMessage() + "\""); }

        // a sessão começa enquanto o ingresso está no carrinho
        Sessao quaseNoFim = sessoes.salvar(new Sessao(0, real, salaReal, LocalDateTime.now().plusSeconds(2), Sessao.StatusSessao.AGENDADA, 15));
        Pedido carrinho = venda.iniciarPedido(cliente);
        venda.venderIngresso(carrinho, sessoes.buscarPorId(quaseNoFim.getId()).get(), 5, Ingresso.TipoIngresso.INTEIRA);
        check(carrinho.getIngressos().size() == 1, "o ingresso entra no carrinho enquanto a sessão ainda não começou");
        Thread.sleep(2500);
        int pagamentosAntes = contar(banco, "pagamento"), ingressosAntes = contar(banco, "ingresso");
        try { venda.finalizarPedido(carrinho, formas.get(0)); check(false, "pagar sessão que começou"); }
        catch (IllegalStateException e) { check(e.getMessage().contains("não está mais à venda"), "pagar depois do horário é barrado: \"" + e.getMessage() + "\""); }
        check(carrinho.getStatus() == Pedido.StatusPedido.ABERTO, "o carrinho continua ABERTO (nada foi cobrado)");
        check(contar(banco, "pagamento") == pagamentosAntes && contar(banco, "ingresso") == ingressosAntes, "nada foi gravado no banco");
        carrinho.removerIngresso(carrinho.getIngressos().get(0));
        check(carrinho.getIngressos().isEmpty(), "o cliente consegue tirar o ingresso e seguir com o carrinho");

        System.out.println("\nTODOS OS " + ok + " TESTES PASSARAM");
    }
}