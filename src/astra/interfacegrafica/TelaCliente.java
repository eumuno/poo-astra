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
        JPanel painel = new JPanel(new GridLayout(9, 1, 5, 5)); // Aumentado para caber o novo campo
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        painel.add(new JLabel("Selecione o Filme:"));

        // CAIXA Q PUXA FILMES CADASTRADOS PELO ADMIN
        JComboBox<Filme> comboFilmes = new JComboBox<>();
        for (Filme f : servico.getFilmesDisponiveis()) {
            comboFilmes.addItem(f);
        }
        painel.add(comboFilmes);

        // P SELECIONAR TIPO DE INGRESSO
        painel.add(new JLabel("Tipo de Ingresso:"));
        JComboBox<Ingresso.TipoIngresso> comboTipo = new JComboBox<>(Ingresso.TipoIngresso.values());
        painel.add(comboTipo);

        painel.add(new JLabel("Digite o número do assento desejado (1 a 10):"));
        JTextField campoAssento = new JTextField();
        painel.add(campoAssento);

        JButton botaoComprar = new JButton("Adicionar Ingresso ao Pedido");
        painel.add(botaoComprar);

        botaoComprar.addActionListener(e -> {
            try {
                int numAssento = Integer.parseInt(campoAssento.getText());
                // CRIA ASSENTO TEMPORÁRIO SÓ P TRANSFORMAR O NÚMERO Q O USUÁRIO DIGITOU
                Assento assentoEscolhido = new Assento(numAssento, Assento.tipoAssento.COMUM);

                // PEGA O TIPO Q O CLIENTE ESCOLHEU
                Ingresso.TipoIngresso tipoSelecionado = (Ingresso.TipoIngresso) comboTipo.getSelectedItem();

                // PASSA O TIPO P SERVIÇO
                Ingresso ingressoComprado = servico.venderIngresso(clienteAtual, servico.getSessaoTeste(), assentoEscolhido, tipoSelecionado);

                pedidoAtual.adicionarIngresso(ingressoComprado);
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
            double total = pedidoAtual.calcularTotal();
            if (total == 0) {
                JOptionPane.showMessageDialog(this, "O carrinho está vazio!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            processarPagamentoPolimorfico(new PagamentoPix(total, "pagamentos@astra.com.br"));
        });

        // PAGAMENTO CARTÃO (polimorfismo)
        botaoCartao.addActionListener(e -> {
            double total = pedidoAtual.calcularTotal();
            if (total == 0) {
                JOptionPane.showMessageDialog(this, "O carrinho está vazio!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            processarPagamentoPolimorfico(new PagamentoCartao(total, clienteAtual.getNome(), "1234567890123456", YearMonth.now().plusYears(1)));
        });

        return painel;
    }

    // PROCESSA O PAGAMENTO P SABER SE FOI OU NÃO
    private void processarPagamentoPolimorfico(Pagamento pagamento) {
        try {
            // POLIMORFISMO PURO: A tela não sabe se é Pix ou Cartão, ela só manda processar!
            pagamento.processarPagamento();
            pedidoAtual.confirmarPagamento();
            JOptionPane.showMessageDialog(this, "Pagamento aprovado! Pedido finalizado.");
            dispose();
        } catch (PagamentoException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro no Pagamento", JOptionPane.ERROR_MESSAGE);
        }
    }

}