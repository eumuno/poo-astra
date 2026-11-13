package astra.interfacegrafica;

import astra.excecoes.AssentoIndisponivelException;
import astra.excecoes.EstoqueInsuficienteException;
import astra.excecoes.PagamentoException;
import astra.excecoes.PersistenciaException;
import astra.modelo.*;
import astra.servico.ProdutoService;
import astra.servico.SessaoService;
import astra.servico.VendaService;
import astra.servico.pagamento.FormaPagamento;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TelaCliente extends JFrame {

    private final ProdutoService produtoService;
    private final SessaoService sessaoService;
    private final VendaService vendaService;
    private final Cliente clienteAtual;
    private final Pedido pedidoAtual;

    // COMBOS RECARREGADOS DO BANCO SEMPRE Q A ABA É ABERTA
    private final JComboBox<Sessao> comboSessoes = new JComboBox<>();
    private final JComboBox<Produto> comboProdutos = new JComboBox<>();

    public TelaCliente(Cliente cliente, ProdutoService produtoService, SessaoService sessaoService, VendaService vendaService) {
        this.produtoService = produtoService;
        this.sessaoService = sessaoService;
        this.vendaService = vendaService;
        setTitle("Área do Cliente - Astra");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // INSTANCIA CLIENTE E PEDIDO ASSIM Q A TELA ABRE
        clienteAtual = cliente;
        pedidoAtual = vendaService.iniciarPedido(clienteAtual);

        // CRIA AS ABAS
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("1. Ingressos", criarPainelIngresso());
        abas.addTab("2. Bomboniere", criarPainelProduto());
        abas.addTab("3. Pagamento", criarPainelPagamento());

        // O Q O ADMIN CADASTRAR DEPOIS Q A TELA ABRIU APARECE AO TROCAR D ABA
        carregarSessoes();
        carregarProdutos();
        abas.addChangeListener(e -> {
            if (abas.getSelectedIndex() == 0) carregarSessoes();
            if (abas.getSelectedIndex() == 1) carregarProdutos();
        });
        add(abas);
    }

    // ABA INGRESSO
    private JPanel criarPainelIngresso() {
        JPanel painel = new JPanel(new GridLayout(9, 1, 5, 5)); // Aumentado para caber o novo campo
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        painel.add(new JLabel("Selecione a Sessão:"));

        // CAIXA Q PUXA AS SESSÕES DISPONÍVEIS DO BANCO
        painel.add(comboSessoes);

        // P SELECIONAR TIPO DE INGRESSO
        painel.add(new JLabel("Tipo de Ingresso:"));
        JComboBox<Ingresso.TipoIngresso> comboTipo = new JComboBox<>(new Ingresso.TipoIngresso[]{Ingresso.TipoIngresso.INTEIRA, Ingresso.TipoIngresso.MEIA});
        painel.add(comboTipo);

        painel.add(new JLabel("Digite o número do assento desejado (1 a 10):"));
        JTextField campoAssento = new JTextField();
        painel.add(campoAssento);

        JButton botaoComprar = new JButton("Adicionar Ingresso ao Pedido");
        painel.add(botaoComprar);

        botaoComprar.addActionListener(e -> {
            try {
                int numAssento = Integer.parseInt(campoAssento.getText().trim());

                // PEGA A SESSÃO E O TIPO Q O CLIENTE ESCOLHEU
                Sessao sessaoSelecionada = (Sessao) comboSessoes.getSelectedItem();
                Ingresso.TipoIngresso tipoSelecionado = (Ingresso.TipoIngresso) comboTipo.getSelectedItem();

                // O SERVIÇO VALIDA O ASSENTO E JÁ COLOCA O INGRESSO NO PEDIDO
                vendaService.venderIngresso(pedidoAtual, sessaoSelecionada, numAssento, tipoSelecionado);
                JOptionPane.showMessageDialog(this, "Ingresso (" + tipoSelecionado + ") para o assento " + numAssento + " adicionado!");

            } catch (AssentoIndisponivelException ex) {
                // SE TENTAR COMPRAR MSM ASSENTO DNV, APARECE NA TELA
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Assento Ocupado", JOptionPane.WARNING_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Digite um número válido para o assento.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        });

        return painel;
    }

    // ABA PRODUTO
    private JPanel criarPainelProduto() {
        JPanel painel = new JPanel(new GridLayout(4, 1, 5, 5));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        painel.add(new JLabel("Produtos da Bomboniere:"));

        painel.add(comboProdutos);

        JButton botaoComprar = new JButton("Adicionar Produto ao Pedido");
        painel.add(botaoComprar);

        botaoComprar.addActionListener(e -> {
            Produto produtoSelecionado = (Produto) comboProdutos.getSelectedItem();
            try {
                vendaService.adicionarProduto(pedidoAtual, produtoSelecionado, 1);
                JOptionPane.showMessageDialog(this, produtoSelecionado.getNome() + " adicionado ao carrinho!");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        return painel;
    }

    // ABA PAGAMENTO
    private JPanel criarPainelPagamento() {
        List<FormaPagamento> formas = vendaService.getFormasPagamento();
        JPanel painel = new JPanel(new GridLayout(3 + formas.size(), 1, 5, 5));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel labelTotal = new JLabel("Total a pagar: R$ 0.0");
        labelTotal.setFont(new Font("Arial", Font.BOLD, 16));
        painel.add(labelTotal);

        JButton botaoAtualizar = new JButton("Atualizar Total");
        painel.add(botaoAtualizar);
        painel.add(new JLabel("Escolha a forma de pagamento:"));

        // ATUALIZA O VALOR USANDO O METODO calcularTotal()
        botaoAtualizar.addActionListener(e -> {
            labelTotal.setText("Total a pagar: R$ " + pedidoAtual.calcularTotal());
        });

        // UM BOTÃO P CADA FORMA DE PAGAMENTO (a tela não conhece Pix nem Cartão)
        for (FormaPagamento forma : formas) {
            JButton botao = new JButton("Pagar com " + forma.getNome());
            botao.addActionListener(e -> finalizarPedido(forma));
            painel.add(botao);
        }

        return painel;
    }

    // O SERVIÇO PROCESSA O PAGAMENTO E GRAVA A VENDA NO BANCO
    private void finalizarPedido(FormaPagamento forma) {
        try {
            vendaService.finalizarPedido(pedidoAtual, forma);
            JOptionPane.showMessageDialog(this, "Pagamento aprovado! Pedido finalizado.");
            dispose();
        } catch (AssentoIndisponivelException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Assento Ocupado", JOptionPane.WARNING_MESSAGE);
        } catch (EstoqueInsuficienteException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Estoque Insuficiente", JOptionPane.WARNING_MESSAGE);
        } catch (PersistenciaException ex) {
            mostrarErroBanco(ex);
        } catch (PagamentoException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro no Pagamento", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarSessoes() {
        comboSessoes.removeAllItems();
        try {
            for (Sessao s : sessaoService.listarSessoesDisponiveis()) {
                comboSessoes.addItem(s);
            }
        } catch (PersistenciaException ex) {
            mostrarErroBanco(ex);
        }
    }

    private void carregarProdutos() {
        comboProdutos.removeAllItems();
        try {
            for (Produto p : produtoService.listarProdutos()) {
                comboProdutos.addItem(p); // COLOCA OBJETO PRODUTO INTEIRO NA CAIXA
            }
        } catch (PersistenciaException ex) {
            mostrarErroBanco(ex);
        }
    }

    private void mostrarErroBanco(PersistenciaException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro no banco de dados", JOptionPane.ERROR_MESSAGE);
    }

}