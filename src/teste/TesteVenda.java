package teste;

import astra.excecoes.AssentoIndisponivelException;
import astra.excecoes.EstoqueInsuficienteException;
import astra.excecoes.PersistenciaException;
import astra.modelo.*;
import astra.persistencia.*;
import astra.servico.VendaService;
import astra.servico.pagamento.FormaPagamento;
import astra.servico.pagamento.FormaPagamentoCartao;
import astra.servico.pagamento.FormaPagamentoPix;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Teste de ponta a ponta da venda: usuário, preço da sessão, pedido, ingresso e VendaService,
 * tudo com os repositórios JDBC de verdade num banco temporário (não mexe no astra.db).
 * Rode a partir da raiz do projeto, com lib/sqlite-jdbc no classpath.
 */
public class TesteVenda {
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

    static int estoque(ProdutoRepositorio produtos, int id) throws Exception {
        return produtos.buscarPorId(id).get().getEstoque();
    }

    static String texto(ConexaoBanco banco, String sql) throws Exception {
        try (Connection c = banco.abrirConexao(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    public static void main(String[] a) throws Exception {
        java.io.File arquivo = java.io.File.createTempFile("teste_venda", ".db");
        arquivo.deleteOnExit();
        ConexaoBanco banco = new ConexaoBanco("jdbc:sqlite:" + arquivo.getAbsolutePath());
        banco.inicializar();

        FilmeRepositorio filmes = new FilmeRepositorioJdbc(banco);
        ProdutoRepositorio produtos = new ProdutoRepositorioJdbc(banco);
        SessaoRepositorio sessoes = new SessaoRepositorioJdbc(banco, filmes);
        UsuarioRepositorio usuarios = new UsuarioRepositorioJdbc(banco);
        IngressoRepositorio ingressos = new IngressoRepositorioJdbc(banco, sessoes, usuarios);
        PedidoRepositorio pedidos = new PedidoRepositorioJdbc(banco, usuarios, produtos, ingressos);
        List<FormaPagamento> formas = List.of(new FormaPagamentoPix(), new FormaPagamentoCartao());
        VendaService venda = new VendaService(formas, pedidos, ingressos, produtos);

        // ---------------- USUÁRIO ----------------
        Usuario admin = usuarios.autenticar("admin@astra", "123").orElse(null);
        check(admin instanceof Administrador && admin.getId() == 1, "login do admin do seed devolve Administrador");
        Usuario kiko = usuarios.autenticar("cliente@email.com", "123").orElse(null);
        check(kiko instanceof Cliente && ((Cliente) kiko).getCpf().equals("000.000.000-00"), "login do cliente do seed devolve Cliente com CPF");
        check(!usuarios.autenticar("cliente@email.com", "errada").isPresent(), "senha errada não autentica");
        check(!usuarios.autenticar("naoexiste@email.com", "123").isPresent(), "e-mail inexistente não autentica");
        check(usuarios.buscarPorEmail("CLIENTE@Email.com").isPresent(), "busca por e-mail ignora maiúsculas");
        check(usuarios.buscarPorId(kiko.getId()).get().getNome().equals("Kiko da Silva"), "buscarPorId");
        Usuario maria = usuarios.salvar(new Cliente(0, "Maria", "maria@email.com", "x", "111.111.111-11"), "segredo");
        check(maria.getId() > 0 && usuarios.autenticar("maria@email.com", "segredo").isPresent(), "cadastro novo recebe id e já consegue logar");
        check(!"segredo".equals(texto(banco, "SELECT senha_hash FROM usuario WHERE email = 'maria@email.com'")), "a senha NÃO fica em texto puro no banco");
        try { usuarios.salvar(new Cliente(0, "Outra", "maria@email.com", "x", "222"), "123"); check(false, "e-mail duplicado"); }
        catch (PersistenciaException e) { check(e.getMessage().contains("maria@email.com"), "e-mail duplicado é recusado: " + e.getMessage()); }
        try { usuarios.salvar(new Cliente(0, "Sem CPF", "semcpf@email.com", "x", null), "123"); check(false, "cliente sem CPF"); }
        catch (PersistenciaException e) { check(true, "cliente sem CPF é recusado pelo banco"); }

        Cliente cliente = (Cliente) kiko;

        // ---------------- PREÇO BASE DA SESSÃO ----------------
        Sessao s1 = sessoes.listarTodos().get(0);
        check(s1.getPrecoBase() == 15.0, "sessão do seed tem preço base 15 lido do banco");
        Filme filme = filmes.listarTodos().get(0);
        Sessao cara = sessoes.salvar(new Sessao(0, filme, s1.getSala(), LocalDateTime.of(2030, 5, 1, 20, 0),
                Sessao.StatusSessao.AGENDADA, 40.0));
        check(sessoes.buscarPorId(cara.getId()).get().getPrecoBase() == 40.0, "preço base 40 gravado e lido de volta");
        Ingresso teste = new Ingresso(0, filme, cara, cara.getSala().buscarAssento(1), cliente, Ingresso.TipoIngresso.MEIA);
        check(teste.getPrecoIngresso() == 20.0, "meia na sessão de R$ 40 custa 20 (preço vem da sessão, não do 15 fixo)");
        try { new Sessao(0, filme, s1.getSala(), LocalDateTime.now(), Sessao.StatusSessao.AGENDADA, -1); check(false, "preço negativo"); }
        catch (IllegalArgumentException e) { check(true, "preço base negativo é recusado"); }

        // ---------------- VENDA COMPLETA ----------------
        Produto pipoca = produtos.listarTodos().get(0);
        Pedido p = venda.iniciarPedido(cliente);
        venda.venderIngresso(p, s1, 3, Ingresso.TipoIngresso.INTEIRA);
        venda.venderIngresso(p, s1, 5, Ingresso.TipoIngresso.MEIA);
        venda.adicionarProduto(p, pipoca, 2);
        check(p.calcularTotal() == 15 + 7.5 + 50, "total do pedido em memória = 72,50");
        int pedidosAntes = contar(banco, "pedido");
        Pagamento pg = venda.finalizarPedido(p, formas.get(0));
        check(pg.getStatus() == Pagamento.Status.APROVADO && pg.getForma() == Pagamento.Forma.PIX, "pagamento Pix aprovado e com forma PIX");
        check(contar(banco, "pedido") == pedidosAntes + 1 && contar(banco, "ingresso") == 2 && contar(banco, "item_pedido") == 1 && contar(banco, "pagamento") == 1,
                "venda gravada: 1 pedido, 2 ingressos, 1 item, 1 pagamento");
        check(estoque(produtos, pipoca.getId()) == pipoca.getEstoque() - 2, "estoque da pipoca abatido na venda: " + pipoca.getEstoque() + " -> " + (pipoca.getEstoque() - 2));

        List<Pedido> doCliente = pedidos.listarPorCliente(cliente.getId());
        check(doCliente.size() == 1, "listarPorCliente acha o pedido");
        Pedido lido = doCliente.get(0);
        check(lido.getId() > 0 && lido.getStatus() == Pedido.StatusPedido.PAGO, "pedido lido volta PAGO com id do banco");
        check(lido.getIngressos().size() == 2 && lido.getItens().size() == 1, "pedido lido traz 2 ingressos e 1 item");
        check(lido.calcularTotal() == 72.5, "total recalculado a partir do banco = 72,50");
        check(lido.getCliente().getNome().equals("Kiko da Silva"), "cliente do pedido lido");
        check(lido.getIngressos().get(0).getAssento().getNumero() == 3 && lido.getIngressos().get(1).getTipoIngresso() == Ingresso.TipoIngresso.MEIA,
                "ingressos lidos: assento 3 inteira e assento 5 meia");
        check(lido.getIngressos().get(0).getId() > 0, "ingresso lido tem id do banco");
        check("PIX".equals(texto(banco, "SELECT forma FROM pagamento WHERE pedido_id = " + lido.getId())), "forma PIX gravada na tabela pagamento");
        check("7.5".equals(texto(banco, "SELECT valor_pago FROM ingresso WHERE tipo = 'MEIA'")), "valor_pago da meia = 7,5");

        // assentos agora aparecem ocupados para quem carrega a sessão de novo ("fechar e abrir")
        Sessao recarregada = sessoes.buscarPorId(s1.getId()).get();
        check(!recarregada.assentoEstaDisponivel(3) && !recarregada.assentoEstaDisponivel(5) && recarregada.assentoEstaDisponivel(4),
                "depois de fechar e abrir (sessão recarregada) os assentos 3 e 5 continuam ocupados");
        check(ingressos.assentoEstaOcupado(s1.getId(), 3) && !ingressos.assentoEstaOcupado(s1.getId(), 4), "IngressoRepositorio.assentoEstaOcupado");
        check(ingressos.listarPorPedido(lido.getId()).size() == 2, "IngressoRepositorio.listarPorPedido");

        // preço histórico: mudar o preço do produto depois NÃO muda o pedido antigo
        Produto pipocaAtual = produtos.buscarPorId(pipoca.getId()).get(); // recarrega: o objeto 'pipoca' tem o estoque de ANTES da venda
        produtos.salvar(new Produto(pipocaAtual.getId(), pipocaAtual.getNome(), pipocaAtual.getDescricao(), 30.0, pipocaAtual.getEstoque(), pipocaAtual.getCategoria()));
        check(estoque(produtos, pipoca.getId()) == pipoca.getEstoque() - 2, "mudar o preço (com o produto recarregado) não mexe no estoque");
        Pedido relido = pedidos.buscarPorId(lido.getId()).get();
        check(relido.getItens().get(0).getPrecoUnitario() == 25.0 && relido.calcularTotal() == 72.5, "preço do item no pedido antigo continua 25 mesmo com produto a 30");

        // ---------------- OUTRA JANELA / ATOMICIDADE ----------------
        // "janela B" carregou a sessão ANTES de A pagar? simulamos com uma sessão carregada agora e assento 7
        Sessao janelaA = sessoes.buscarPorId(s1.getId()).get();
        Sessao janelaB = sessoes.buscarPorId(s1.getId()).get();
        Pedido pa = venda.iniciarPedido(cliente);
        Pedido pb = venda.iniciarPedido(cliente);
        venda.venderIngresso(pa, janelaA, 7, Ingresso.TipoIngresso.INTEIRA);
        venda.venderIngresso(pb, janelaB, 7, Ingresso.TipoIngresso.INTEIRA); // na memória da B o 7 ainda está livre
        venda.finalizarPedido(pa, formas.get(1));
        check("CARTAO".equals(texto(banco, "SELECT forma FROM pagamento ORDER BY id DESC LIMIT 1")), "forma CARTAO gravada");
        int antesItem = contar(banco, "item_pedido");
        int antesPed = contar(banco, "pedido"), antesIng = contar(banco, "ingresso"), antesPag = contar(banco, "pagamento");
        try { venda.finalizarPedido(pb, formas.get(0)); check(false, "B deveria falhar"); }
        catch (AssentoIndisponivelException e) { check(true, "janela B é barrada ANTES de cobrar: " + e.getMessage()); }
        check(pb.getStatus() == Pedido.StatusPedido.ABERTO, "pedido da janela B continua ABERTO (não cobrou)");

        // pula a conferência do VendaService e vai direto no repositório: o UNIQUE do banco segura e faz rollback
        Pedido manual = new Pedido(0, cliente);
        Sessao sx = sessoes.buscarPorId(s1.getId()).get();
        manual.adicionarItem(pipoca, 1); // item entra ANTES do ingresso que vai falhar
        sx = new Sessao(sx.getId(), sx.getFilme(), sx.getSala(), sx.getDataHora(), sx.getStatus(), sx.getPrecoBase()); // sem assentos ocupados na memória
        manual.adicionarIngresso(new Ingresso(0, sx.getFilme(), sx, sx.getSala().buscarAssento(7), cliente, Ingresso.TipoIngresso.INTEIRA));
        manual.confirmarPagamento();
        try { pedidos.salvar(manual, new astra.modelo.PagamentoPix(10, "chave")); check(false, "UNIQUE deveria barrar"); }
        catch (AssentoIndisponivelException e) { check(true, "UNIQUE do banco barra o assento repetido: " + e.getMessage()); }
        check(contar(banco, "pedido") == antesPed && contar(banco, "ingresso") == antesIng && contar(banco, "pagamento") == antesPag
                        && contar(banco, "item_pedido") == antesItem,
                "ROLLBACK: nada do pedido que falhou ficou gravado (pedido, item, ingresso, pagamento intactos)");

        // administrador não pode ser dono de pedido (o TelaCliente antigo usa Cliente id 1 = admin do seed!)
        Pedido deAdmin = new Pedido(0, new Cliente(1, "Kiko da Silva", "cliente@email.com", "123", "000"));
        deAdmin.adicionarItem(pipoca, 1);
        deAdmin.confirmarPagamento();
        try { pedidos.salvar(deAdmin, new astra.modelo.PagamentoPix(25, "chave")); check(false, "cliente id 1"); }
        catch (PersistenciaException e) { check(e.getMessage().contains("não é um cliente"), "cliente com id de administrador é recusado: " + e.getMessage()); }

        // produto que nunca foi gravado
        Pedido semProduto = new Pedido(0, cliente);
        semProduto.adicionarItem(new Produto(0, "Fantasma", "", 5, 1, Produto.CategoriaProduto.DOCE), 1);
        semProduto.confirmarPagamento();
        try { pedidos.salvar(semProduto, new astra.modelo.PagamentoPix(5, "chave")); check(false, "produto sem id"); }
        catch (PersistenciaException e) { check(contar(banco, "pedido") == antesPed, "produto não gravado é recusado e faz rollback"); }

        // ---------------- STATUS (tela do admin) ----------------
        Pedido paraPronto = pedidos.buscarPorId(lido.getId()).get();
        paraPronto.marcarComoPronto();
        pedidos.atualizarStatus(paraPronto.getId(), paraPronto.getStatus());
        check(pedidos.buscarPorId(lido.getId()).get().getStatus() == Pedido.StatusPedido.PRONTO, "status PRONTO gravado e lido de volta");
        try { pedidos.atualizarStatus(9999, Pedido.StatusPedido.PRONTO); check(false, "id inexistente"); }
        catch (PersistenciaException e) { check(true, "atualizarStatus de id inexistente lança PersistenciaException"); }
        check(pedidos.listarTodos().size() == 2 && !pedidos.buscarPorId(9999).isPresent(), "listarTodos (2 pedidos) e buscarPorId inexistente = vazio");

        // restaurar() não vale para pedido aberto novo: confere que o pedido PAGO lido não aceita item
        try { relido.adicionarItem(pipoca, 1); check(false, "pedido pago aceitou item"); }
        catch (IllegalStateException e) { check(true, "pedido restaurado como PAGO continua protegido (não aceita item novo)"); }

        // ---------------- ESTOQUE ----------------
        Produto choc = produtos.salvar(new Produto(0, "Chocolate", "barra", 6.0, 3, Produto.CategoriaProduto.DOCE));
        Produto refri = produtos.salvar(new Produto(0, "Refrigerante", "500ml", 8.0, 5, Produto.CategoriaProduto.BEBIDA));

        // três janelas, cada uma com 2 chocolates no carrinho (o estoque é 3): nada impede de adicionar
        Pedido x = venda.iniciarPedido(cliente);
        Pedido y = venda.iniciarPedido(cliente);
        Pedido z = venda.iniciarPedido(cliente);
        venda.adicionarProduto(x, choc, 2);
        venda.adicionarProduto(y, choc, 2);
        venda.adicionarProduto(z, choc, 2);
        venda.finalizarPedido(x, formas.get(0));
        check(estoque(produtos, choc.getId()) == 1, "janela X pagou 2 chocolates: estoque 3 -> 1");

        int pagamentosAntes = contar(banco, "pagamento");
        try { venda.finalizarPedido(y, formas.get(0)); check(false, "Y deveria falhar"); }
        catch (EstoqueInsuficienteException e) { check(e.getMessage().contains("restam 1") && e.getMessage().contains("Chocolate"), "janela Y barrada: " + e.getMessage()); }
        check(y.getStatus() == Pedido.StatusPedido.ABERTO && contar(banco, "pagamento") == pagamentosAntes,
                "janela Y barrada ANTES de cobrar: pedido continua ABERTO e nenhum pagamento novo");

        // pula a conferência do VendaService: quem segura é o UPDATE ... WHERE estoque >= ? da transação
        z.confirmarPagamento();
        try { pedidos.salvar(z, new astra.modelo.PagamentoPix(12, "chave")); check(false, "UPDATE deveria barrar"); }
        catch (EstoqueInsuficienteException e) { check(true, "o banco barra na transação mesmo sem a conferência prévia: " + e.getMessage()); }
        check(estoque(produtos, choc.getId()) == 1, "estoque do chocolate continua 1 depois da tentativa barrada");

        // ROLLBACK da baixa: 1º item (refrigerante) passa, 2º item (chocolate) falta -> a baixa do refrigerante some
        int pedidosAntes2 = contar(banco, "pedido"), itensAntes2 = contar(banco, "item_pedido"), pagAntes2 = contar(banco, "pagamento");
        Pedido w = new Pedido(0, cliente);
        w.adicionarItem(refri, 2);
        w.adicionarItem(choc, 2);
        w.confirmarPagamento();
        try { pedidos.salvar(w, new astra.modelo.PagamentoPix(32, "chave")); check(false, "w deveria falhar"); }
        catch (EstoqueInsuficienteException e) { check(true, "pedido com 2 itens falha no 2º: " + e.getMessage()); }
        check(estoque(produtos, refri.getId()) == 5 && estoque(produtos, choc.getId()) == 1
                        && contar(banco, "pedido") == pedidosAntes2 && contar(banco, "item_pedido") == itensAntes2 && contar(banco, "pagamento") == pagAntes2,
                "ROLLBACK: a baixa do refrigerante (1º item) foi desfeita, e nada do pedido ficou gravado");

        // o mesmo produto em dois itens: cada item (3) cabe sozinho no estoque (5), mas a soma (6) não
        Pedido dobro = venda.iniciarPedido(cliente);
        venda.adicionarProduto(dobro, refri, 3);
        venda.adicionarProduto(dobro, refri, 3);
        try { venda.finalizarPedido(dobro, formas.get(0)); check(false, "soma deveria falhar"); }
        catch (EstoqueInsuficienteException e) { check(e.getMessage().contains("pede 6"), "conferência soma o mesmo produto em vários itens: " + e.getMessage()); }

        // limite exato: levar a última unidade funciona (estoque vai a 0), e a seguinte falha
        Pedido ultima = venda.iniciarPedido(cliente);
        venda.adicionarProduto(ultima, choc, 1);
        venda.finalizarPedido(ultima, formas.get(1));
        check(estoque(produtos, choc.getId()) == 0, "levar exatamente a última unidade funciona: estoque 1 -> 0");
        Pedido alemDoFim = venda.iniciarPedido(cliente);
        venda.adicionarProduto(alemDoFim, choc, 1);
        try { venda.finalizarPedido(alemDoFim, formas.get(1)); check(false, "estoque zerado"); }
        catch (EstoqueInsuficienteException e) { check(e.getMessage().contains("restam 0"), "com estoque zerado a venda é barrada: " + e.getMessage()); }

        System.out.println("\nTODOS OS " + ok + " TESTES PASSARAM");
    }
}