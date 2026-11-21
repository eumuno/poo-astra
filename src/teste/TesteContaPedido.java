package teste;

import astra.excecoes.PersistenciaException;
import astra.modelo.*;
import astra.persistencia.*;
import astra.servico.*;
import astra.excecoes.AcessoNegadoException;
import astra.servico.TrilhaDeAuditoria;
import astra.servico.ControleDeAcesso;
import astra.servico.pagamento.FormaPagamento;
import astra.servico.pagamento.FormaPagamentoPix;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

/**
 * Teste dos serviços usados pela interface no lugar dos repositórios: {@link ContaService} (login e
 * cadastro), {@link PedidoService} (histórico e status) e {@link AtendimentoCliente} (a fachada que
 * implementa {@link CatalogoCliente}). Usa um banco temporário e roda a partir da raiz do projeto.
 */
public class TesteContaPedido {

    private TesteContaPedido() { }
    static int ok = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError("FALHOU: " + msg);
        ok++;
        System.out.println("OK  " + msg);
    }

    static int contarUsuarios(ConexaoBanco banco) throws Exception {
        try (Connection c = banco.abrirConexao(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM usuario")) {
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
        java.io.File arquivo = java.io.File.createTempFile("teste_conta", ".db");
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
        ContaService contas = new ContaService(usuarios, trilha);
        PedidoService pedidoService = new PedidoService(pedidos, acesso, trilha);
        CatalogoCliente catalogo = new AtendimentoCliente(new SessaoService(sessoes, acesso, trilha), new ProdutoService(produtos, acesso, trilha), venda, pedidoService);

        // ---------------- ContaService: login ----------------
        check(contas.autenticar("cliente@email.com", "123").get() instanceof Cliente, "login do cliente devolve um Cliente");
        check(contas.autenticar("  admin@astra ", "123").get() instanceof Administrador, "login do administrador (e-mail com espaços) devolve um Administrador");
        check(!contas.autenticar("cliente@email.com", "errada").isPresent(), "senha errada devolve vazio");
        check(!contas.autenticar("ninguem@x.com", "123").isPresent(), "e-mail inexistente devolve vazio (igual à senha errada)");
        try { contas.autenticar("", "123"); check(false, "e-mail em branco"); }
        catch (IllegalArgumentException e) { check(e.getMessage().equals("Preencha e-mail e senha."), "e-mail em branco: \"" + e.getMessage() + "\""); }
        try { contas.autenticar("cliente@email.com", "   "); check(false, "senha em branco"); }
        catch (IllegalArgumentException e) { check(true, "senha em branco é recusada"); }

        // ---------------- ContaService: cadastro ----------------
        int antes = contarUsuarios(banco);
        Cliente nova = contas.cadastrarCliente("  Maria Souza ", " maria@x.com ", "222.222.222-22", "segredo1");
        check(nova.getId() > 0 && nova.getCpf().equals("22222222222") && nova.getNome().equals("Maria Souza"), "cadastro devolve o Cliente com id, CPF só com dígitos e nome sem espaços");
        check(contas.autenticar("maria@x.com", "segredo1").isPresent(), "a conta nova já consegue entrar");
        try { contas.cadastrarCliente("  ", "a@x.com", "33333333333", "segredo1"); check(false, "nome vazio"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("nome"), "nome em branco: \"" + e.getMessage() + "\""); }
        try { contas.cadastrarCliente("Ana", "sem-arroba", "33333333333", "segredo1"); check(false, "e-mail inválido"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("e-mail válido"), "e-mail inválido: \"" + e.getMessage() + "\""); }
        try { contas.cadastrarCliente("Ana", "a@x.com", "123.456.789-0", "segredo1"); check(false, "cpf curto"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("11 dígitos"), "CPF com 10 dígitos: \"" + e.getMessage() + "\""); }
        try { contas.cadastrarCliente("Ana", "a@x.com", "33333333333", "12345"); check(false, "senha curta"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("pelo menos 6"), "senha com 5 caracteres: \"" + e.getMessage() + "\""); }
        check(contarUsuarios(banco) == antes + 1, "nenhum dado inválido chegou a ser gravado");
        try { contas.cadastrarCliente("Outra", "outra@x.com", "22222222222", "segredo1"); check(false, "cpf repetido"); }
        catch (PersistenciaException e) { check(e.getMessage().contains("CPF"), "CPF repetido: \"" + e.getMessage() + "\""); }
        try { contas.cadastrarCliente("Clone do Kiko", "clone@x.com", "000.000.000-00", "segredo1"); check(false, "cpf do seed"); }
        catch (PersistenciaException e) { check(e.getMessage().contains("CPF"), "o CPF do cliente de demonstração também é protegido (seed só com dígitos)"); }
        try { contas.cadastrarCliente("Outra", "MARIA@x.com", "44444444444", "segredo1"); check(false, "e-mail repetido"); }
        catch (PersistenciaException e) { check(e.getMessage().contains("e-mail"), "e-mail repetido: \"" + e.getMessage() + "\""); }

        // ---------------- AtendimentoCliente (fachada) e PedidoService ----------------
        Cliente cliente = (Cliente) contas.autenticar("cliente@email.com", "123").get();
        Administrador admin = (Administrador) contas.autenticar("admin@astra", "123").get();
        Sessao sessao = catalogo.listarSessoesDisponiveis().get(0);
        check(catalogo.listarProdutos().size() >= 1 && !catalogo.getFormasPagamento().isEmpty(), "a fachada lista produtos e formas de pagamento");
        check(catalogo.consultarAssentosOcupados(sessao).isEmpty(), "antes da venda nenhuma poltrona está ocupada");

        Pedido compra = catalogo.iniciarPedido(cliente);
        catalogo.venderIngresso(compra, sessao, 7, Ingresso.TipoIngresso.INTEIRA);
        catalogo.finalizarPedido(compra, catalogo.getFormasPagamento().get(0));
        Set<Integer> ocupados = catalogo.consultarAssentosOcupados(sessao);
        check(ocupados.size() == 1 && ocupados.contains(7), "depois do pagamento a poltrona 7 aparece ocupada pela fachada");

        List<Pedido> historico = catalogo.listarPedidosDoCliente(cliente);
        check(historico.size() == 1 && historico.get(0).getStatus() == Pedido.StatusPedido.PAGO, "histórico do cliente: 1 pedido PAGO");
        check(pedidoService.listarTodos(admin).size() == 1, "o administrador vê o pedido na lista geral");
        check(catalogo.listarPedidosDoCliente(nova).isEmpty(), "outro cliente não vê esse pedido");
        try { pedidoService.listarPorCliente(null); check(false, "cliente nulo"); }
        catch (IllegalArgumentException e) { check(true, "listar pedidos sem informar o cliente é recusado"); }

        int pedidoId = historico.get(0).getId();
        try { pedidoService.marcarComoRetirado(admin, pedidoId); check(false, "retirar antes de pronto"); }
        catch (IllegalStateException e) { check(e.getMessage().contains("pronto"), "retirar antes de ficar pronto: \"" + e.getMessage() + "\""); }
        check(pedidoService.marcarComoPronto(admin, pedidoId).getStatus() == Pedido.StatusPedido.PRONTO, "marcar como pronto devolve o pedido PRONTO");
        check(pedidos.buscarPorId(pedidoId).get().getStatus() == Pedido.StatusPedido.PRONTO, "o status PRONTO foi gravado no banco");
        check(pedidoService.marcarComoRetirado(admin, pedidoId).getStatus() == Pedido.StatusPedido.RETIRADO, "marcar como retirado devolve o pedido RETIRADO");
        check(pedidos.buscarPorId(pedidoId).get().getStatus() == Pedido.StatusPedido.RETIRADO, "o status RETIRADO foi gravado no banco");
        try { pedidoService.marcarComoPronto(admin, pedidoId); check(false, "pronto depois de retirado"); }
        catch (IllegalStateException e) { check(true, "um pedido já retirado não volta a ficar pronto"); }
        try { pedidoService.marcarComoPronto(admin, 9999); check(false, "pedido inexistente"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("não existe"), "pedido inexistente: \"" + e.getMessage() + "\""); }
        try { pedidoService.marcarComoPronto(null, pedidoId); check(false, "sem admin"); }
        catch (AcessoNegadoException e) { check(true, "sem administrador não atualiza pedido"); }

        // ---------------- I de SOLID: a área do cliente não enxerga operações de administrador ----------------
        StringBuilder metodos = new StringBuilder();
        for (Method m : CatalogoCliente.class.getMethods()) metodos.append(m.getName()).append(' ');
        String[] soDoAdministrador = {"cadastrarProduto", "atualizarProduto", "reporEstoque", "cadastrarFilme", "atualizarFilme",
                "cadastrarSessao", "atualizarSessao", "cancelarSessao", "marcarComoPronto", "marcarComoRetirado", "listarTodos"};
        boolean vazou = false;
        for (String nome : soDoAdministrador) if (metodos.toString().contains(nome + " ")) vazou = true;
        check(!vazou, "CatalogoCliente não oferece nenhuma operação de administrador (princípio I)");

        System.out.println("\nTODOS OS " + ok + " TESTES PASSARAM");
    }
}