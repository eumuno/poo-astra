package astra.interfacegrafica;

import astra.excecoes.*;
import astra.modelo.*;
import astra.persistencia.*;
import astra.servico.*;
import astra.servico.pagamento.FormaPagamento;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** Compra de ingressos e bomboniere, com seleção visual de poltronas. */
public class TelaCliente extends JFrame {
    private final Cliente cliente;
    private final ProdutoService produtos;
    private final SessaoService sessoes;
    private final VendaService vendas;
    private final IngressoRepositorio ingressos;
    private final PedidoRepositorio pedidos;
    private Pedido pedido;
    private final JComboBox<Sessao> comboSessoes = new JComboBox<>();
    private final JComboBox<Produto> comboProdutos = new JComboBox<>();
    private final JLabel estoqueProduto = TemaAstra.destaque("Consulte os produtos disponíveis.");
    private final JSpinner quantidadeProduto = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
    private final JButton diminuirQuantidade = TemaAstra.botaoSecundario("−");
    private final JButton aumentarQuantidade = TemaAstra.botaoSecundario("+");
    private final JButton adicionarProduto = TemaAstra.botao("Adicionar ao carrinho");
    private boolean carregandoProdutos;
    private boolean conferindoEstoque;
    private long versaoProdutos;
    private final JPanel grade = new JPanel(new GridLayout(0, 5, 10, 10));
    private final JLabel precoSessao = TemaAstra.rotulo("Selecione uma sessão para consultar as poltronas.");
    private final JLabel assentoEscolhido = TemaAstra.destaque("Nenhuma poltrona selecionada");
    private final JLabel total = TemaAstra.titulo("Total: R$ 0,00");
    private final JLabel totalPagamento = TemaAstra.titulo("Total: R$ 0,00");
    private final JTabbedPane abas = new JTabbedPane();
    private final DefaultTableModel itensModelo = new DefaultTableModel(new String[]{"Item", "Quantidade", "Valor"}, 0) {
        @Override public boolean isCellEditable(int linha, int coluna) { return false; }
    };
    private final List<Object> itensTabela = new ArrayList<>();
    private final JPanel historico = TemaAstra.painel();
    private final Set<Integer> ocupadosNoBanco = new HashSet<>();
    private int numeroSelecionado = -1;
    private long versaoConsulta = 0;
    private boolean carregandoSessoes;

    public TelaCliente(Cliente cliente, ProdutoService produtos, SessaoService sessoes,
                       VendaService vendas, IngressoRepositorio ingressos, PedidoRepositorio pedidos) {
        this.cliente = cliente; this.produtos = produtos; this.sessoes = sessoes;
        this.vendas = vendas; this.ingressos = ingressos; this.pedidos = pedidos;
        this.pedido = vendas.iniciarPedido(cliente);
        TemaAstra.configurarJanela(this, "Cliente · " + cliente.getNome(), 1150, 830);
        abas.setFont(new Font("SansSerif", Font.BOLD, 16));
        abas.addTab("Ingressos", criarIngressos());
        abas.addTab("Bomboniere", criarBomboniere());
        abas.addTab("Carrinho", criarCarrinho());
        abas.addTab("Pagamento", criarPagamento());
        abas.addTab("Minhas compras", criarHistorico());
        abas.addChangeListener(e -> {
            if (abas.getSelectedIndex() == 0) carregarSessoes();
            if (abas.getSelectedIndex() == 1) carregarProdutos();
            if (abas.getSelectedIndex() == 2 || abas.getSelectedIndex() == 3) atualizarResumo();
            if (abas.getSelectedIndex() == 4) carregarHistorico();
        });
        setContentPane(TemaAstra.areaPrincipal("Área do cliente", cliente.getNome(), abas));
        carregarSessoes(); carregarProdutos(); atualizarResumo();
    }

    private JPanel criarIngressos() {
        JPanel conteudo = TemaAstra.painel();
        conteudo.setLayout(new BorderLayout(0, 22));
        JPanel topo = new JPanel(new GridBagLayout()); topo.setOpaque(false);
        TemaAstra.prepararEntrada(comboSessoes);
        JComboBox<Ingresso.TipoIngresso> tipo = new JComboBox<>(
                new Ingresso.TipoIngresso[]{Ingresso.TipoIngresso.INTEIRA, Ingresso.TipoIngresso.MEIA});
        TemaAstra.prepararEntrada(tipo);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        c.insets = new Insets(0, 0, 8, 0);
        topo.add(TemaAstra.destaque("Filme e horário"), c);
        c.gridy = 1; c.insets = new Insets(0, 0, 12, 0);
        topo.add(comboSessoes, c);
        c.gridy = 2; c.insets = new Insets(0, 0, 14, 0);
        topo.add(precoSessao, c);
        c.gridy = 3; c.insets = new Insets(0, 0, 8, 0);
        topo.add(TemaAstra.destaque("Tipo de ingresso"), c);
        c.gridy = 4; c.insets = new Insets(0, 0, 0, 0);
        topo.add(tipo, c);
        JPanel painelEscolha = new JPanel(new BorderLayout(0, 16));
        painelEscolha.setOpaque(false);
        painelEscolha.setPreferredSize(new Dimension(405, 400));
        JLabel etapaSessao = TemaAstra.destaque("1   ESCOLHA SUA SESSÃO");
        etapaSessao.setForeground(TemaAstra.SUAVE);
        JPanel tituloEFormulario = new JPanel();
        tituloEFormulario.setOpaque(false);
        tituloEFormulario.setLayout(new BoxLayout(tituloEFormulario, BoxLayout.Y_AXIS));
        etapaSessao.setAlignmentX(Component.LEFT_ALIGNMENT);
        topo.setAlignmentX(Component.LEFT_ALIGNMENT);
        tituloEFormulario.add(etapaSessao);
        tituloEFormulario.add(Box.createVerticalStrut(25));
        tituloEFormulario.add(topo);
        painelEscolha.add(tituloEFormulario, BorderLayout.NORTH);

        JPanel assentos = new JPanel(new BorderLayout(0, 16)); assentos.setOpaque(false);
        JPanel legenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 3)); legenda.setOpaque(false);
        legenda.add(TemaAstra.destaque("2   POLTRONAS"));
        legenda.add(itemLegenda("Disponível", TemaAstra.LIVRE));
        legenda.add(itemLegenda("Ocupado", TemaAstra.OCUPADO));
        legenda.add(itemLegenda("Selecionado", TemaAstra.ROXO));
        assentos.add(legenda, BorderLayout.NORTH);

        JPanel mapa = new JPanel(); mapa.setOpaque(false);
        mapa.setLayout(new BoxLayout(mapa, BoxLayout.Y_AXIS));
        JLabel tela = TemaAstra.destaque("T E L A");
        tela.setHorizontalAlignment(SwingConstants.CENTER);
        tela.setOpaque(true); tela.setBackground(TemaAstra.CARTAO_ALT);
        tela.setMaximumSize(new Dimension(440, 40));
        tela.setPreferredSize(new Dimension(440, 40));
        tela.setAlignmentX(Component.CENTER_ALIGNMENT);
        grade.setOpaque(false);
        grade.setAlignmentX(Component.CENTER_ALIGNMENT);
        // As poltronas mantêm o próprio tamanho; o GridLayout não ocupa toda a altura da janela.
        mapa.add(tela); mapa.add(Box.createVerticalStrut(24)); mapa.add(grade);
        JPanel centralizador = new JPanel(new GridBagLayout()); centralizador.setOpaque(false);
        GridBagConstraints centro = new GridBagConstraints();
        centro.gridx = 0; centro.gridy = 0; centro.anchor = GridBagConstraints.NORTH;
        centro.weightx = 1; centro.weighty = 1;
        centro.insets = new Insets(6, 10, 16, 10);
        centralizador.add(mapa, centro);
        JScrollPane scroll = new JScrollPane(centralizador);
        scroll.setBorder(null); scroll.getViewport().setBackground(TemaAstra.CARTAO);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        assentos.add(scroll, BorderLayout.CENTER);
        JPanel colunas = new JPanel(new GridBagLayout()); colunas.setOpaque(false);
        GridBagConstraints esquerda = new GridBagConstraints();
        esquerda.gridx = 0; esquerda.gridy = 0; esquerda.weightx = 0.41; esquerda.weighty = 1;
        esquerda.fill = GridBagConstraints.BOTH; esquerda.anchor = GridBagConstraints.NORTH;
        esquerda.insets = new Insets(0, 0, 0, 18);
        colunas.add(painelEscolha, esquerda);
        GridBagConstraints direita = new GridBagConstraints();
        direita.gridx = 1; direita.gridy = 0; direita.weightx = 0.59; direita.weighty = 1;
        direita.fill = GridBagConstraints.BOTH; direita.anchor = GridBagConstraints.NORTH;
        direita.insets = new Insets(0, 18, 0, 0);
        colunas.add(assentos, direita);
        conteudo.add(colunas, BorderLayout.CENTER);

        JPanel rodape = new JPanel(new BorderLayout(16, 0)); rodape.setOpaque(false);
        JButton adicionar = TemaAstra.botao("Adicionar ingresso");
        rodape.add(assentoEscolhido, BorderLayout.CENTER);
        rodape.add(adicionar, BorderLayout.EAST);
        conteudo.add(rodape, BorderLayout.SOUTH);
        comboSessoes.addActionListener(e -> {
            Sessao selecionada = (Sessao) comboSessoes.getSelectedItem();
            comboSessoes.setToolTipText(selecionada == null ? "Nenhuma sessão disponível" : selecionada.toString());
            if (!carregandoSessoes) { numeroSelecionado = -1; carregarOcupacao(); }
        });
        adicionar.addActionListener(e -> {
            if (numeroSelecionado < 0) {
                TemaAstra.erro(this, "Escolha uma poltrona disponível (verde)."); return;
            }
            try {
                Sessao sessao = (Sessao) comboSessoes.getSelectedItem();
                vendas.venderIngresso(pedido, sessao, numeroSelecionado,
                        (Ingresso.TipoIngresso) tipo.getSelectedItem());
                int adicionada = numeroSelecionado;
                numeroSelecionado = -1;
                desenharGrade(); atualizarResumo();
                TemaAstra.sucesso(this, "Poltrona " + adicionada + " adicionada ao carrinho.");
            } catch (AssentoIndisponivelException ex) {
                TemaAstra.erro(this, "Essa poltrona não está disponível. Escolha outra."); carregarOcupacao();
            } catch (Exception ex) { TemaAstra.erro(this, ex.getMessage()); }
        });
        return TemaAstra.pagina("Escolha sua sessão", conteudo);
    }

    /** Legenda visual de alta legibilidade, com texto além da cor. */
    private JComponent itemLegenda(String nome, Color cor) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0)); p.setOpaque(false);
        JLabel quadrado = new JLabel(" ");
        quadrado.setOpaque(true); quadrado.setBackground(cor);
        quadrado.setPreferredSize(new Dimension(19, 19));
        p.add(quadrado); p.add(TemaAstra.destaque(nome));
        return p;
    }

    /** Consulta o SQLite em segundo plano, evitando travar o clique na interface. */
    private void carregarOcupacao() {
        long versao = ++versaoConsulta;
        Sessao sessao = (Sessao) comboSessoes.getSelectedItem();
        ocupadosNoBanco.clear();
        numeroSelecionado = -1;
        assentoEscolhido.setText("Nenhuma poltrona selecionada");
        if (sessao == null) { desenharGrade(); return; }
        precoSessao.setText("Sala " + sessao.getSala().getNumero() + "  •  Inteira " + TemaAstra.dinheiro(sessao.getPrecoBase()));
        grade.removeAll();
        JLabel carregando = TemaAstra.destaque("Consultando poltronas...");
        grade.add(carregando); grade.revalidate(); grade.repaint();
        new SwingWorker<Set<Integer>, Void>() {
            @Override protected Set<Integer> doInBackground() throws Exception {
                Set<Integer> lista = new HashSet<>();
                for (Assento a : sessao.getSala().getAssentos()) {
                    if (ingressos.assentoEstaOcupado(sessao.getId(), a.getNumero())) lista.add(a.getNumero());
                }
                return lista;
            }
            @Override protected void done() {
                if (versao != versaoConsulta) return;
                try { ocupadosNoBanco.addAll(get()); desenharGrade(); }
                catch (Exception ex) {
                    grade.removeAll(); grade.add(TemaAstra.destaque("Erro ao consultar poltronas"));
                    grade.revalidate(); grade.repaint();
                    erroDoWorker("Não foi possível carregar os assentos", ex);
                }
            }
        }.execute();
    }

    private void desenharGrade() {
        grade.removeAll();
        Sessao s = (Sessao) comboSessoes.getSelectedItem();
        if (s == null) {
            precoSessao.setText("Nenhuma sessão disponível no momento.");
            grade.add(TemaAstra.destaque("Nenhuma sessão selecionada"));
            grade.revalidate(); grade.repaint(); return;
        }
        for (Assento a : s.getSala().getAssentos()) {
            int numero = a.getNumero();
            boolean livre = !ocupadosNoBanco.contains(numero) && s.assentoEstaDisponivel(numero);
            Color fundo = !livre ? TemaAstra.OCUPADO :
                    numero == numeroSelecionado ? TemaAstra.ROXO : TemaAstra.LIVRE;
            JButton botao = TemaAstra.botaoAssento(String.format("%02d", numero), fundo);
            if (numero == numeroSelecionado) {
                botao.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3, true));
            }
            botao.setToolTipText("Poltrona " + numero + (livre ? " disponível" : " ocupada"));
            botao.addActionListener(e -> {
                if (!livre) {
                    TemaAstra.erro(this, "A poltrona " + numero + " já está ocupada.");
                    return;
                }
                numeroSelecionado = numero;
                assentoEscolhido.setText("Poltrona selecionada: " + numero);
                desenharGrade();
            });
            grade.add(botao);
        }
        grade.revalidate(); grade.repaint();
    }

    /**
     * A interface consulta o estoque, mas não o abate: a baixa é responsabilidade da
     * transação em PedidoRepositorioJdbc.salvar, evitando descontar duas vezes.
     */
    private JPanel criarBomboniere() {
        JPanel form = TemaAstra.formulario();
        TemaAstra.prepararEntrada(comboProdutos);
        comboProdutos.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean selecionado, boolean foco) {
                JLabel rotulo = (JLabel) super.getListCellRendererComponent(
                        lista, valor, indice, selecionado, foco);
                if (valor instanceof Produto produto) {
                    rotulo.setText(produto.getNome() + "  •  " + TemaAstra.dinheiro(produto.getPreco())
                            + "  •  Estoque: " + produto.getEstoque());
                } else {
                    rotulo.setText("Nenhum produto disponível");
                }
                rotulo.setFont(comboProdutos.getFont());
                rotulo.setOpaque(true);
                rotulo.setBackground(selecionado ? TemaAstra.ROXO_ESCURO : TemaAstra.CAMPO);
                rotulo.setForeground(selecionado ? Color.WHITE : TemaAstra.TEXTO_ESCURO);
                rotulo.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                return rotulo;
            }
        });
        TemaAstra.prepararEntrada(quantidadeProduto);
        quantidadeProduto.setPreferredSize(new Dimension(104, 44));
        quantidadeProduto.setToolTipText("Digite a quantidade desejada, limitada pelo estoque disponível.");
        diminuirQuantidade.setPreferredSize(new Dimension(58, 46));
        aumentarQuantidade.setPreferredSize(new Dimension(58, 46));
        JPanel contador = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        contador.setOpaque(false);
        contador.add(diminuirQuantidade);
        contador.add(quantidadeProduto);
        contador.add(aumentarQuantidade);
        TemaAstra.linha(form, 0, "Produto", comboProdutos);
        TemaAstra.linha(form, 1, "Disponibilidade", estoqueProduto);
        TemaAstra.linha(form, 2, "Quantidade", contador);
        TemaAstra.acoes(form, 3, adicionarProduto);

        comboProdutos.addActionListener(e -> {
            if (!carregandoProdutos) atualizarDisponibilidadeProduto();
        });
        quantidadeProduto.addChangeListener(e -> {
            SpinnerNumberModel modelo = (SpinnerNumberModel) quantidadeProduto.getModel();
            int atual = ((Number) quantidadeProduto.getValue()).intValue();
            int maximo = ((Number) modelo.getMaximum()).intValue();
            boolean habilitar = comboProdutos.getSelectedItem() != null
                    && !carregandoProdutos && !conferindoEstoque && maximo > 0;
            diminuirQuantidade.setEnabled(habilitar && atual > 1);
            aumentarQuantidade.setEnabled(habilitar && atual < maximo);
        });
        diminuirQuantidade.addActionListener(e -> ajustarQuantidade(-1));
        aumentarQuantidade.addActionListener(e -> ajustarQuantidade(+1));
        adicionarProduto.addActionListener(e -> adicionarProdutoAoCarrinho());
        atualizarDisponibilidadeProduto();
        return TemaAstra.paginaFormulario("Bomboniere", form, 590);
    }

    /** Soma todas as linhas já adicionadas do mesmo produto, inclusive se repetidas. */
    private int quantidadeDoProdutoNoCarrinho(int produtoId) {
        int quantidade = 0;
        for (ItemPedido item : pedido.getItens()) {
            if (item.getProduto().getId() == produtoId) {
                quantidade += item.getQuantidade();
            }
        }
        return quantidade;
    }

    /** Limite visual: estoque carregado do banco menos o que já está no carrinho. */
    private void atualizarDisponibilidadeProduto() {
        Produto produto = (Produto) comboProdutos.getSelectedItem();
        int disponivel = produto == null ? 0 : Math.max(0,
                produto.getEstoque() - quantidadeDoProdutoNoCarrinho(produto.getId()));

        int valorAnterior = ((Number) quantidadeProduto.getValue()).intValue();
        int limite = Math.max(1, disponivel);
        int valor = Math.min(Math.max(1, valorAnterior), limite);
        SpinnerNumberModel modelo = (SpinnerNumberModel) quantidadeProduto.getModel();
        modelo.setMaximum(limite);
        modelo.setValue(valor);
        boolean habilitar = produto != null && disponivel > 0 && !carregandoProdutos && !conferindoEstoque;
        quantidadeProduto.setEnabled(habilitar);
        aumentarQuantidade.setEnabled(habilitar && valor < limite);
        diminuirQuantidade.setEnabled(habilitar && valor > 1);
        adicionarProduto.setEnabled(habilitar);
        comboProdutos.setEnabled(!carregandoProdutos && !conferindoEstoque);

        if (produto == null) {
            estoqueProduto.setText("Nenhum produto cadastrado.");
        } else if (disponivel == 0) {
            estoqueProduto.setText("Indisponível para adicionar • Estoque: " + produto.getEstoque()
                    + " • No carrinho: " + quantidadeDoProdutoNoCarrinho(produto.getId()));
        } else {
            estoqueProduto.setText("Preço: " + TemaAstra.dinheiro(produto.getPreco())
                    + " • Em estoque: " + produto.getEstoque()
                    + " • No carrinho: " + quantidadeDoProdutoNoCarrinho(produto.getId())
                    + " • Pode adicionar: " + disponivel);
        }
    }

    private void ajustarQuantidade(int passo) {
        try {
            quantidadeProduto.commitEdit();
            int valor = ((Number) quantidadeProduto.getValue()).intValue();
            SpinnerNumberModel modelo = (SpinnerNumberModel) quantidadeProduto.getModel();
            int limite = ((Number) modelo.getMaximum()).intValue();
            quantidadeProduto.setValue(Math.max(1, Math.min(limite, valor + passo)));
            atualizarDisponibilidadeProduto();
        } catch (java.text.ParseException | IllegalArgumentException ex) {
            TemaAstra.erro(this, "Digite uma quantidade inteira válida.");
        }
    }

    /**
     * Consulta novamente o banco em segundo plano antes de incluir o produto. A
     * transação da venda continua sendo a garantia final contra estoque esgotado.
     */
    private void adicionarProdutoAoCarrinho() {
        if (conferindoEstoque || carregandoProdutos) return;
        Produto selecionado = (Produto) comboProdutos.getSelectedItem();
        if (selecionado == null) {
            TemaAstra.erro(this, "Escolha um produto da bomboniere.");
            return;
        }
        final int quantidade;
        try {
            quantidadeProduto.commitEdit();
            quantidade = ((Number) quantidadeProduto.getValue()).intValue();
        } catch (java.text.ParseException | IllegalArgumentException ex) {
            TemaAstra.erro(this, "Informe uma quantidade inteira válida.");
            return;
        }
        int jaNoCarrinho = quantidadeDoProdutoNoCarrinho(selecionado.getId());
        if (quantidade < 1 || quantidade > selecionado.getEstoque() - jaNoCarrinho) {
            TemaAstra.erro(this, "A quantidade solicitada ultrapassa o estoque disponível."
                    + " Confira os itens que já estão no carrinho.");
            atualizarDisponibilidadeProduto();
            return;
        }
        final int idProduto = selecionado.getId();
        final Pedido pedidoAtual = pedido;
        conferindoEstoque = true;
        atualizarDisponibilidadeProduto();
        new SwingWorker<Produto, Void>() {
            @Override protected Produto doInBackground() throws Exception {
                for (Produto produto : produtos.listarProdutos()) {
                    if (produto.getId() == idProduto) return produto;
                }
                return null;
            }
            @Override protected void done() {
                conferindoEstoque = false;
                try {
                    Produto atual = get();
                    if (pedidoAtual != pedido) return;
                    if (atual == null) {
                        TemaAstra.erro(TelaCliente.this,
                                "Esse produto não está mais cadastrado. Atualize a bomboniere.");
                    } else {
                        int restante = atual.getEstoque() - quantidadeDoProdutoNoCarrinho(idProduto);
                        if (restante < quantidade) {
                            TemaAstra.erro(TelaCliente.this, "Estoque insuficiente de " + atual.getNome()
                                    + ": há " + Math.max(0, restante) + " unidade(s) disponíveis para adicionar.");
                        } else {
                            vendas.adicionarProduto(pedido, atual, quantidade);
                            atualizarResumo();
                            TemaAstra.sucesso(TelaCliente.this,
                                    quantidade + " × " + atual.getNome() + " adicionado(s) ao carrinho.");
                        }
                    }
                } catch (Exception ex) {
                    erroDoWorker("Não foi possível consultar o estoque", ex);
                } finally {
                    atualizarDisponibilidadeProduto();
                    carregarProdutos();
                }
            }
        }.execute();
    }

    private JPanel criarCarrinho() {
        JPanel p = TemaAstra.painel(); p.setLayout(new BorderLayout(0, 20));
        JTable tabela = new JTable(itensModelo);
        TemaAstra.configurarTabela(tabela);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(new JScrollPane(tabela), BorderLayout.CENTER);
        JPanel rodape = new JPanel(new BorderLayout(14, 0)); rodape.setOpaque(false);
        JButton remover = TemaAstra.botaoSecundario("Remover selecionado");
        remover.setToolTipText("Selecione uma linha para retirar um produto ou ingresso.");
        remover.setEnabled(false);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) remover.setEnabled(tabela.getSelectedRow() >= 0);
        });
        remover.addActionListener(e -> {
            int i = tabela.getSelectedRow();
            if (i < 0) { TemaAstra.erro(this, "Selecione um item na tabela."); return; }
            Object item = itensTabela.get(tabela.convertRowIndexToModel(i));
            if (item instanceof ItemPedido ip) pedido.removerItem(ip);
            if (item instanceof Ingresso ingresso) {
                pedido.removerIngresso(ingresso);
                carregarSessoes();
            }
            atualizarResumo(); desenharGrade();
        });
        tabela.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("DELETE"), "removerItem");
        tabela.getActionMap().put("removerItem", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (remover.isEnabled()) remover.doClick();
            }
        });
        rodape.add(remover, BorderLayout.WEST); rodape.add(total, BorderLayout.EAST);
        p.add(rodape, BorderLayout.SOUTH);
        return TemaAstra.pagina("Revise seu carrinho", p);
    }

    private JPanel criarPagamento() {
        JPanel p = TemaAstra.formulario();
        JLabel aviso = TemaAstra.rotulo("Demonstração: nenhuma cobrança real é realizada.");
        TemaAstra.linha(p, 0, "Valor da compra", totalPagamento);
        TemaAstra.linha(p, 1, "Informação", aviso);
        int linha = 2;
        for (FormaPagamento forma : vendas.getFormasPagamento()) {
            JButton pagar = TemaAstra.botao("Confirmar com " + forma.getNome());
            TemaAstra.linha(p, linha++, forma.getNome(), pagar);
            pagar.addActionListener(e -> finalizar(forma, pagar));
        }
        return TemaAstra.paginaFormulario("Finalizar compra", p, 570);
    }

    private JPanel criarHistorico() {
        historico.setLayout(new BorderLayout(0, 16));
        return TemaAstra.pagina("Minhas compras", historico);
    }

    private void carregarHistorico() {
        historico.removeAll();
        DefaultTableModel modelo = new DefaultTableModel(new String[]{"Pedido", "Data", "Total", "Status"}, 0) {
            @Override public boolean isCellEditable(int linha, int coluna) { return false; }
        };
        JTable tabela = new JTable(modelo);
        TemaAstra.configurarTabela(tabela);
        historico.add(new JScrollPane(tabela), BorderLayout.CENTER);
        new SwingWorker<List<Pedido>, Void>() {
            @Override protected List<Pedido> doInBackground() throws Exception {
                return pedidos.listarPorCliente(cliente.getId());
            }
            @Override protected void done() {
                try {
                    for (Pedido p : get()) modelo.addRow(new Object[]{p.getId(), p.getData(),
                            TemaAstra.dinheiro(p.calcularTotal()), p.getStatus()});
                } catch (Exception ex) { erroDoWorker("Não foi possível carregar suas compras", ex); }
            }
        }.execute();
        historico.revalidate(); historico.repaint();
    }

    private void atualizarResumo() {
        itensModelo.setRowCount(0); itensTabela.clear();
        for (Ingresso ing : pedido.getIngressos()) {
            itensModelo.addRow(new Object[]{"Ingresso • " + ing.getFilme().getTitulo()
                    + " • Poltrona " + ing.getAssento().getNumero(), 1,
                    TemaAstra.dinheiro(ing.getPrecoIngresso())});
            itensTabela.add(ing);
        }
        for (ItemPedido ip : pedido.getItens()) {
            itensModelo.addRow(new Object[]{ip.getProduto().getNome(), ip.getQuantidade(),
                    TemaAstra.dinheiro(ip.calcularSubtotal())});
            itensTabela.add(ip);
        }
        total.setText("Total: " + TemaAstra.dinheiro(pedido.calcularTotal()));
        totalPagamento.setText(total.getText());
        atualizarDisponibilidadeProduto();
    }

    private void finalizar(FormaPagamento forma, JButton botao) {
        if (pedido.estaVazio()) { TemaAstra.erro(this, "O carrinho está vazio."); return; }
        Pedido pedidoEmPagamento = pedido;
        if (JOptionPane.showConfirmDialog(this,
                "Confirmar pagamento demonstrativo de " + TemaAstra.dinheiro(pedidoEmPagamento.calcularTotal()) + "?",
                "Confirmar compra", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        botao.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                vendas.finalizarPedido(pedidoEmPagamento, forma); return null;
            }
            @Override protected void done() {
                botao.setEnabled(true);
                try {
                    get();
                    pedido = vendas.iniciarPedido(cliente);
                    atualizarResumo();
                    abas.setSelectedIndex(4); // Mostra o comprovante no histórico sem fechar a janela.
                    TemaAstra.sucesso(TelaCliente.this, "Compra de demonstração concluída e salva no banco!");
                } catch (Exception ex) {
                    erroDoWorker("Não foi possível concluir o pagamento", ex);
                    carregarSessoes();
                }
            }
        }.execute();
    }

    private void carregarSessoes() {
        Sessao anterior = (Sessao) comboSessoes.getSelectedItem();
        new SwingWorker<List<Sessao>, Void>() {
            @Override protected List<Sessao> doInBackground() throws Exception {
                return sessoes.listarSessoesDisponiveis();
            }
            @Override protected void done() {
                try {
                    carregandoSessoes = true;
                    comboSessoes.removeAllItems();
                    Sessao novaSelecao = null;
                    for (Sessao sessao : get()) {
                        comboSessoes.addItem(sessao);
                        if (anterior != null && sessao.getId() == anterior.getId()) novaSelecao = sessao;
                    }
                    if (novaSelecao != null) comboSessoes.setSelectedItem(novaSelecao);
                    carregandoSessoes = false;
                    carregarOcupacao();
                } catch (Exception ex) {
                    carregandoSessoes = false;
                    erroDoWorker("Não foi possível carregar as sessões", ex);
                }
            }
        }.execute();
    }

    /** Recarrega o estoque real sempre que o cliente abre a aba da bomboniere. */
    private void carregarProdutos() {
        final long versao = ++versaoProdutos;
        final Produto anterior = (Produto) comboProdutos.getSelectedItem();
        carregandoProdutos = true;
        atualizarDisponibilidadeProduto();
        new SwingWorker<List<Produto>, Void>() {
            @Override protected List<Produto> doInBackground() throws Exception {
                return produtos.listarProdutos();
            }
            @Override protected void done() {
                if (versao != versaoProdutos) return;
                try {
                    List<Produto> lista = get();
                    comboProdutos.removeAllItems();
                    Produto manter = null;
                    for (Produto produto : lista) {
                        comboProdutos.addItem(produto);
                        if (anterior != null && produto.getId() == anterior.getId()) manter = produto;
                    }
                    if (manter != null) comboProdutos.setSelectedItem(manter);
                } catch (Exception ex) {
                    erroDoWorker("Não foi possível atualizar o estoque da bomboniere", ex);
                } finally {
                    carregandoProdutos = false;
                    atualizarDisponibilidadeProduto();
                }
            }
        }.execute();
    }

    private void erroDoWorker(String contexto, Exception ex) {
        Throwable causa = ex instanceof ExecutionException && ex.getCause() != null ? ex.getCause() : ex;
        TemaAstra.erro(this, contexto + ": " + causa.getMessage());
    }
}
