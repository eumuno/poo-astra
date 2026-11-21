package teste;

import astra.excecoes.AssentoIndisponivelException;
import astra.excecoes.EstoqueInsuficienteException;
import astra.excecoes.PagamentoException;
import astra.excecoes.PersistenciaException;
import astra.interfacegrafica.FalhaDaCompra;
import astra.modelo.*;
import astra.persistencia.*;
import astra.servico.VendaService;
import astra.servico.pagamento.FormaPagamento;
import astra.servico.pagamento.FormaPagamentoPix;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Comentário do professor: se a gravação falha DEPOIS do pagamento aprovado, o pedido
 * ficava PAGO em memória e o cliente não conseguia tentar de novo.
 * Aqui um repositório "quebrado" reproduz isso, e depois a mesma compra é refeita com o repositório real.
 * Também confere as mensagens da FalhaDaCompra (sem abrir janela nenhuma).
 */
public class TesteFalhaCompra {
    static int ok = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError("FALHOU: " + msg);
        ok++;
        System.out.println("OK  " + msg);
    }

    /** Repositório de pedidos que falha ao gravar (banco fora do ar, disco cheio...). Guarda o pagamento que recebeu. */
    static class PedidoRepositorioQueFalha implements PedidoRepositorio {
        final Exception falha;
        Pagamento recebido;
        PedidoRepositorioQueFalha(Exception falha) { this.falha = falha; }

        @Override public Pedido salvar(Pedido pedido, Pagamento pagamento)
                throws PersistenciaException, AssentoIndisponivelException, EstoqueInsuficienteException {
            recebido = pagamento;
            if (falha instanceof PersistenciaException e) throw e;
            if (falha instanceof AssentoIndisponivelException e) throw e;
            if (falha instanceof EstoqueInsuficienteException e) throw e;
            throw (RuntimeException) falha;
        }
        @Override public Optional<Pedido> buscarPorId(int id) { return Optional.empty(); }
        @Override public List<Pedido> listarPorCliente(int clienteId) { return List.of(); }
        @Override public List<Pedido> listarTodos() { return List.of(); }
        @Override public void atualizarStatus(int pedidoId, Pedido.StatusPedido novoStatus) { }
    }

    public static void main(String[] a) throws Exception {
        java.io.File arquivo = java.io.File.createTempFile("teste_falha", ".db");
        arquivo.deleteOnExit();
        ConexaoBanco banco = new ConexaoBanco("jdbc:sqlite:" + arquivo.getAbsolutePath());
        banco.inicializar();

        FilmeRepositorio filmes = new FilmeRepositorioJdbc(banco);
        ProdutoRepositorio produtos = new ProdutoRepositorioJdbc(banco);
        SessaoRepositorio sessoes = new SessaoRepositorioJdbc(banco, filmes);
        UsuarioRepositorio usuarios = new UsuarioRepositorioJdbc(banco);
        IngressoRepositorio ingressos = new IngressoRepositorioJdbc(banco, sessoes, usuarios);
        PedidoRepositorio pedidosReal = new PedidoRepositorioJdbc(banco, usuarios, produtos, ingressos);
        List<FormaPagamento> formas = List.of(new FormaPagamentoPix());
        Cliente cliente = (Cliente) usuarios.autenticar("cliente@email.com", "123").get();
        Sessao sessao = sessoes.listarTodos().get(0);

        // ---------------- GRAVAÇÃO FALHA DEPOIS DO PAGAMENTO ----------------
        PedidoRepositorioQueFalha quebrado = new PedidoRepositorioQueFalha(
                new PersistenciaException("Erro ao gravar o pedido.", new java.sql.SQLException("database is locked")));
        VendaService vendaQuebrada = new VendaService(formas, quebrado, ingressos, produtos);
        Pedido pedido = vendaQuebrada.iniciarPedido(cliente);
        vendaQuebrada.venderIngresso(pedido, sessao, 9, Ingresso.TipoIngresso.INTEIRA);

        try { vendaQuebrada.finalizarPedido(pedido, formas.get(0)); check(false, "deveria falhar"); }
        catch (PersistenciaException e) { check(true, "a falha de gravação chega até a tela como PersistenciaException"); }
        check(quebrado.recebido != null, "o pagamento chegou a ser aprovado antes da gravação (cenário do professor)");
        check(quebrado.recebido.getStatus() == Pagamento.Status.ESTORNADO, "o pagamento aprovado foi ESTORNADO: nada fica cobrado");
        check(pedido.getStatus() == Pedido.StatusPedido.ABERTO, "o pedido voltou para ABERTO (antes ficava PAGO em memória)");
        check(sessao.assentoEstaDisponivel(9), "a poltrona 9 NÃO foi ocupada em memória");
        check(!ingressos.assentoEstaOcupado(sessao.getId(), 9), "e nada foi gravado no banco");

        // o cliente consegue mexer no carrinho de novo...
        pedido.adicionarItem(produtos.listarTodos().get(0), 1);
        check(pedido.getItens().size() == 1, "pedido reaberto aceita item novo");

        // ...e tentar de novo, agora com o banco funcionando
        VendaService vendaReal = new VendaService(formas, pedidosReal, ingressos, produtos);
        Pagamento segundo = vendaReal.finalizarPedido(pedido, formas.get(0));
        check(segundo.getStatus() == Pagamento.Status.APROVADO && pedido.getStatus() == Pedido.StatusPedido.PAGO,
                "a segunda tentativa do MESMO pedido é aprovada");
        check(ingressos.assentoEstaOcupado(sessao.getId(), 9) && pedidosReal.listarPorCliente(cliente.getId()).size() == 1,
                "e agora a venda está gravada (1 pedido, poltrona 9 ocupada)");

        // ---------------- CORRIDA: O BANCO RECUSA O ASSENTO DEPOIS DO PAGAMENTO ----------------
        PedidoRepositorioQueFalha corrida = new PedidoRepositorioQueFalha(
                new AssentoIndisponivelException("O assento 10 já foi vendido para esta sessão.", sessao.getId(), 10));
        VendaService vendaCorrida = new VendaService(formas, corrida, ingressos, produtos);
        Pedido p2 = vendaCorrida.iniciarPedido(cliente);
        vendaCorrida.venderIngresso(p2, sessao, 10, Ingresso.TipoIngresso.MEIA);
        try { vendaCorrida.finalizarPedido(p2, formas.get(0)); check(false, "deveria falhar"); }
        catch (AssentoIndisponivelException e) { check(e.getNumeroAssento() == 10, "exceção de assento traz o número da poltrona como dado"); }
        check(corrida.recebido.getStatus() == Pagamento.Status.ESTORNADO && p2.getStatus() == Pedido.StatusPedido.ABERTO,
                "assento perdido na transação também estorna e reabre o pedido");

        // ---------------- REGRAS DO PEDIDO ----------------
        try { new Pedido(0, cliente).desfazerConfirmacaoPagamento(); check(false, "aberto"); }
        catch (IllegalStateException e) { check(true, "não dá para 'desfazer' um pedido que nem foi pago"); }
        Pedido gravado = pedidosReal.listarPorCliente(cliente.getId()).get(0);
        try { gravado.desfazerConfirmacaoPagamento(); check(false, "gravado"); }
        catch (IllegalStateException e) { check(true, "pedido já gravado no banco não volta a ficar aberto"); }

        // ---------------- MENSAGENS (FalhaDaCompra) ----------------
        FalhaDaCompra f = FalhaDaCompra.classificar(new ExecutionException(
                new AssentoIndisponivelException("x", 1, 7)));
        check(f.texto().equals("A poltrona 7 acabou de ser vendida. Nada foi cobrado. Escolha outra.")
                        && f.destino() == FalhaDaCompra.Destino.INGRESSOS && f.gravidade() == FalhaDaCompra.Gravidade.AVISO,
                "assento -> " + f.texto() + " (aba Ingressos, aviso)");

        f = FalhaDaCompra.classificar(new EstoqueInsuficienteException("Pipoca", 2, 5));
        check(f.texto().equals("Restam 2 de Pipoca. Nada foi cobrado. Reduza a quantidade.")
                        && f.destino() == FalhaDaCompra.Destino.CARRINHO,
                "estoque -> " + f.texto() + " (aba Carrinho)");

        f = FalhaDaCompra.classificar(new EstoqueInsuficienteException("Chocolate", 0, 1));
        check(f.texto().startsWith("Chocolate esgotou."), "estoque zerado -> " + f.texto());

        f = FalhaDaCompra.classificar(new PagamentoException("cartão vencido"));
        check(f.texto().equals("Pagamento recusado: cartão vencido. Tente outra forma.")
                        && f.destino() == FalhaDaCompra.Destino.FICAR,
                "pagamento -> " + f.texto());

        f = FalhaDaCompra.classificar(new ExecutionException(new PersistenciaException("Erro ao gravar o pedido.")));
        check(f.texto().startsWith("Não conseguimos registrar a compra.") && f.gravidade() == FalhaDaCompra.Gravidade.ERRO,
                "persistência -> " + f.texto() + " (ícone de erro)");

        check(!FalhaDaCompra.classificar(new AssentoIndisponivelException("x", 1, 7)).titulo()
                        .equals(FalhaDaCompra.classificar(new PersistenciaException("x")).titulo()),
                "assento e banco fora do ar NÃO têm mais o mesmo título");

        System.out.println("\nTODOS OS " + ok + " TESTES PASSARAM");
    }
}