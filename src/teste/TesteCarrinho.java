package teste;

import astra.excecoes.EstoqueInsuficienteException;
import astra.modelo.*;
import astra.persistencia.*;
import astra.servico.*;
import astra.servico.TrilhaDeAuditoria;
import astra.servico.ControleDeAcesso;
import astra.servico.pagamento.FormaPagamento;
import astra.servico.pagamento.FormaPagamentoPix;

import java.util.List;

/**
 * Teste do "+" e "−" do carrinho ({@link VendaService#alterarQuantidade}): aumentar, diminuir, preço e
 * ordem preservados, limite de estoque (somando itens do mesmo produto), pedido já pago e o resultado
 * no estoque depois do pagamento. Usa um banco temporário e roda a partir da raiz do projeto.
 */
public class TesteCarrinho {
    static int ok = 0;

    private TesteCarrinho() { }

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError("FALHOU: " + msg);
        ok++;
        System.out.println("OK  " + msg);
    }

    /**
     * Executa todas as verificações.
     *
     * @param args não usado
     * @throws Exception se alguma verificação ou o banco falhar
     */
    public static void main(String[] args) throws Exception {
        java.io.File arquivo = java.io.File.createTempFile("teste_carrinho", ".db");
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
        Cliente cliente = (Cliente) usuarios.autenticar("cliente@email.com", "123").get();
        Produto pipoca = produtos.listarTodos().get(0);                       // estoque 50 no seed
        Produto refri = produtos.salvar(new Produto(0, "Refrigerante", "500ml", 8.0, 20, Produto.CategoriaProduto.BEBIDA));

        // pedido com três itens: pipoca (2), refrigerante (1), pipoca de novo (1)
        Pedido pedido = venda.iniciarPedido(cliente);
        venda.adicionarProduto(pedido, pipoca, 2);
        venda.adicionarProduto(pedido, refri, 1);
        venda.adicionarProduto(pedido, pipoca, 1);
        List<ItemPedido> itens = pedido.getItens();
        ItemPedido a = itens.get(0), b = itens.get(1), c = itens.get(2);
        double totalAntes = pedido.calcularTotal();

        // ---- aumentar ----
        ItemPedido a5 = venda.alterarQuantidade(pedido, a, 5);
        check(a5.getQuantidade() == 5 && pedido.getItens().size() == 3, "aumentar de 2 para 5: o item muda e continua havendo 3 itens");
        check(pedido.getItens().get(0) == a5 && pedido.getItens().get(1) == b && pedido.getItens().get(2) == c, "a ordem dos itens é preservada (o item alterado fica no mesmo lugar)");
        check(a5.getPrecoUnitario() == a.getPrecoUnitario() && a5.getProduto() == a.getProduto(), "o preço unitário e o produto do item são preservados");
        check(pedido.calcularTotal() == totalAntes + 3 * pipoca.getPreco(), "o total do pedido acompanha: +3 pipocas");

        // ---- diminuir ----
        ItemPedido a4 = venda.alterarQuantidade(pedido, a5, 4);
        check(a4.getQuantidade() == 4 && pedido.getItens().get(0) == a4, "diminuir de 5 para 4");
        try { venda.alterarQuantidade(pedido, a4, 0); check(false, "quantidade 0"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("pelo menos 1"), "quantidade zero é recusada: \"" + e.getMessage() + "\""); }
        try { venda.alterarQuantidade(pedido, a4, -3); check(false, "quantidade negativa"); }
        catch (IllegalArgumentException e) { check(true, "quantidade negativa é recusada"); }
        check(pedido.getItens().get(0) == a4, "depois das recusas o carrinho continua igual");

        // ---- limite de estoque: soma os outros itens do mesmo produto ----
        // estoque da pipoca = 50; o item c (outra linha da pipoca) tem 1 -> o item a pode ir até 49
        ItemPedido a49 = venda.alterarQuantidade(pedido, a4, 49);
        check(a49.getQuantidade() == 49, "chegar exatamente ao limite funciona: 49 + 1 de outra linha = 50");
        try { venda.alterarQuantidade(pedido, a49, 50); check(false, "acima do estoque"); }
        catch (EstoqueInsuficienteException e) {
            check(e.getDisponivel() == 50 && e.getNomeProduto().equals("Pipoca Grande"), "passar de 49 (50 + 1 = 51 > 50) é barrado: restam " + e.getDisponivel());
        }
        check(pedido.getItens().get(0) == a49 && pedido.getItens().size() == 3, "depois da recusa por estoque o carrinho não mudou");
        ItemPedido b3 = venda.alterarQuantidade(pedido, b, 3);
        check(b3.getQuantidade() == 3 && pedido.getItens().get(1) == b3, "outro produto (refrigerante) é independente do limite da pipoca");

        // ---- itens que não são do pedido ----
        Pedido outro = venda.iniciarPedido(cliente);
        venda.adicionarProduto(outro, refri, 1);
        try { venda.alterarQuantidade(outro, a49, 2); check(false, "item de outro pedido"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("não está no carrinho"), "item que não está no pedido é recusado"); }
        try { venda.alterarQuantidade(outro, null, 2); check(false, "item nulo"); }
        catch (IllegalArgumentException e) { check(true, "item nulo é recusado"); }

        // ---- pela fachada do cliente ----
        CatalogoCliente catalogo = new AtendimentoCliente(new SessaoService(sessoes, acesso, trilha), new ProdutoService(produtos, acesso, trilha), venda, new PedidoService(pedidos, acesso, trilha));
        ItemPedido pelaFachada = catalogo.alterarQuantidade(outro, outro.getItens().get(0), 4);
        check(pelaFachada.getQuantidade() == 4, "a fachada CatalogoCliente também permite alterar a quantidade");

        // ---- depois do pagamento ----
        Pedido paraPagar = venda.iniciarPedido(cliente);
        venda.adicionarProduto(paraPagar, pipoca, 2);
        venda.adicionarProduto(paraPagar, refri, 1);
        ItemPedido pipocaDoPagamento = paraPagar.getItens().get(0);
        venda.alterarQuantidade(paraPagar, pipocaDoPagamento, 6);
        venda.alterarQuantidade(paraPagar, paraPagar.getItens().get(1), 2);
        venda.finalizarPedido(paraPagar, formas.get(0));
        check(produtos.buscarPorId(pipoca.getId()).get().getEstoque() == 50 - 6, "o estoque abatido é o da quantidade FINAL (6), não a de quando foi adicionado");
        check(produtos.buscarPorId(refri.getId()).get().getEstoque() == 20 - 2, "o estoque do refrigerante também: 20 - 2");
        try { venda.alterarQuantidade(paraPagar, paraPagar.getItens().get(0), 3); check(false, "pedido pago"); }
        catch (IllegalStateException e) { check(true, "pedido já pago não aceita mais mudança de quantidade"); }

        System.out.println("\nTODOS OS " + ok + " TESTES PASSARAM");
    }
}