package astra.interfacegrafica;

import astra.excecoes.AssentoIndisponivelException;
import astra.excecoes.PagamentoException;
import astra.modelo.*;
import astra.servico.CinemaService;

import javax.swing.*;
import java.awt.*;
import java.time.YearMonth;

public class TelaCliente extends JFrame {

    private CinemaService servico;
    private Cliente clienteAtual;
    private Pedido pedidoAtual;

    public TelaCliente(CinemaService servico) {
        this.servico = servico;
        setTitle("Área do Cliente - Astra");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // INSTANCIA CLIENTE E PEDIDO ASSIM Q A TELA ABRE
        clienteAtual = new Cliente(1, "Kiko da Silva", "cliente@email.com", "123", "000.000");
        pedidoAtual = new Pedido(1, clienteAtual);

        // CRIA AS ABAS
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("1. Ingressos", criarPainelIngresso());
        abas.addTab("2. Bomboniere", criarPainelProduto());
        abas.addTab("3. Pagamento", criarPainelPagamento());
        add(abas);
    }

    // ABA INGRESSO
    private JPanel criarPainelIngresso() {
        JPanel painel = new JPanel(new GridLayout(6, 1, 5, 5));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // PUXA O AUTO DA COMPADECIDA
        painel.add(new JLabel("Filme em Cartaz: " + servico.getFilmeEmCartaz().getTitulo()));
        painel.add(new JLabel("Digite o número do assento desejado:"));

        JTextField campoAssento = new JTextField();
        painel.add(campoAssento);
        painel.add(new JLabel("(Assento 5 forçará erro de indisponibilidade)"));

        JButton botaoComprar = new JButton("Adicionar Ingresso ao Pedido");
        painel.add(botaoComprar);

        botaoComprar.addActionListener(e -> {
            try {
                int numAssento = Integer.parseInt(campoAssento.getText());

                // CRIA O ASSENTO NA TELA
                Assento assentoEscolhido = new Assento(numAssento, Assento.tipoAssento.COMUM);
                if (numAssento == 5) {
                    assentoEscolhido.ocupar(); // simula assento ocupado
                }

                // CHAMA O SERVIÇO C OS 4 ARGUMENTOS Q ELE PEDE
                Ingresso ingressoComprado = servico.venderIngresso(clienteAtual, servico.getSessaoTeste(), assentoEscolhido, Ingresso.TipoIngresso.INTEIRA);

                // ADICIONA NO PEDIDO
                pedidoAtual.adicionarIngresso(ingressoComprado);

                JOptionPane.showMessageDialog(this, "Ingresso adicionado ao carrinho!");

            } catch (AssentoIndisponivelException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Assento Ocupado", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        });

        return painel;
    }

    // ABA PROUTO
    private JPanel criarPainelProduto() {
        JPanel painel = new JPanel(new GridLayout(4, 1, 5, 5));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        painel.add(new JLabel("Produtos da Bomboniere:"));

        JComboBox<Produto> comboProdutos = new JComboBox<>();
        for (Produto p : servico.getProdutosDisponiveis()) {
            comboProdutos.addItem(p); // COLOCA OBJETO PRODUTO INTEIRO NA CAIXA
        }
        painel.add(comboProdutos);

        JButton botaoComprar = new JButton("Adicionar Produto ao Pedido");
        painel.add(botaoComprar);

        botaoComprar.addActionListener(e -> {
            Produto produtoSelecionado = (Produto) comboProdutos.getSelectedItem();
            if (produtoSelecionado != null) {
                // USA O METODO adicionarItem(Produto, quantidade)
                pedidoAtual.adicionarItem(produtoSelecionado, 1);
                JOptionPane.showMessageDialog(this, produtoSelecionado.getNome() + " adicionado ao carrinho!");
            }
        });

        return painel;
    }

    // ABA PAGAMENTO
    private JPanel criarPainelPagamento() {
        JPanel painel = new JPanel(new GridLayout(5, 1, 5, 5));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel labelTotal = new JLabel("Total a pagar: R$ 0.0");
        labelTotal.setFont(new Font("Arial", Font.BOLD, 16));
        painel.add(labelTotal);

        JButton botaoAtualizar = new JButton("Atualizar Total");
        JButton botaoPix = new JButton("Pagar com Pix");
        JButton botaoCartao = new JButton("Pagar com Cartão");

        painel.add(botaoAtualizar);
        painel.add(new JLabel("Escolha a forma de pagamento:"));
        painel.add(botaoPix);
        painel.add(botaoCartao);

        // ATUALIZA O VALOR USANDO O METODO calcularTotal()
        botaoAtualizar.addActionListener(e -> {
            labelTotal.setText("Total a pagar: R$ " + pedidoAtual.calcularTotal());
        });

        // PAGAMENTO PIX (polimorfismo)
        botaoPix.addActionListener(e -> {
            try {
                double total = pedidoAtual.calcularTotal();
                if (total == 0) throw new IllegalStateException("O carrinho está vazio!");

                // usa a classe do pagamento c pix c dados fictícios
                PagamentoPix pix = new PagamentoPix(total, "pagamentos@astra.com.br"); // chave pix do cinema
                pix.processarPagamento(); // processa do jeito do pix

                pedidoAtual.confirmarPagamento(); // muda status p pago
                JOptionPane.showMessageDialog(this, "Pagamento via Pix aprovado! Pedido finalizado.");
                dispose(); // fecha a tela

            } catch (PagamentoException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro no Pagamento", JOptionPane.ERROR_MESSAGE);
            }
        });

        // PAGAMENTO CARTÃO (polimorfismo)
        botaoCartao.addActionListener(e -> {
            try {
                double total = pedidoAtual.calcularTotal();
                if (total == 0) throw new IllegalStateException("O carrinho está vazio!");

                // usa dados do pagamento c cartão com dados fictícios
                PagamentoCartao cartao = new PagamentoCartao(total, clienteAtual.getNome(), "1234567890123456", YearMonth.now().plusYears(1));
                cartao.processarPagamento(); // Processa do jeito do Cartão

                pedidoAtual.confirmarPagamento(); // Muda status do pedido para PAGO
                JOptionPane.showMessageDialog(this, "Pagamento via Cartão aprovado! Pedido finalizado.");
                dispose(); // Fecha a tela

            } catch (PagamentoException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro no Pagamento", JOptionPane.ERROR_MESSAGE);
            }
        });

        return painel;
    }
}