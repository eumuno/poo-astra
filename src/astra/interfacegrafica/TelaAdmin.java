package astra.interfacegrafica;

import astra.modelo.Administrador;
import astra.modelo.Filme;
import astra.modelo.Produto;
import astra.modelo.Pedido;
import astra.persistencia.PedidoRepositorio;
import astra.servico.FilmeService;
import astra.servico.ProdutoService;
import astra.servico.SessaoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ExecutionException;

/** Administração de filmes, produtos, sessões e acompanhamento dos pedidos. */
public class TelaAdmin extends JFrame {
    private final Administrador admin;
    private final FilmeService filmeService;
    private final ProdutoService produtoService;
    private final SessaoService sessaoService;
    private final PedidoRepositorio pedidoRepositorio;
    private final JComboBox<Filme> comboFilmesSessao = new JComboBox<>();
    private final DefaultTableModel pedidosModelo = new DefaultTableModel(
            new String[]{"Pedido", "Cliente", "Total", "Status"}, 0) {
        @Override public boolean isCellEditable(int linha, int coluna) { return false; }
    };
    private final java.util.List<Pedido> pedidosListados = new java.util.ArrayList<>();
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public TelaAdmin(Administrador admin, FilmeService filmeService, ProdutoService produtoService,
                     SessaoService sessaoService, PedidoRepositorio pedidoRepositorio) {
        this.admin = admin; this.filmeService = filmeService;
        this.produtoService = produtoService; this.sessaoService = sessaoService;
        this.pedidoRepositorio = pedidoRepositorio;
        TemaAstra.configurarJanela(this, "Administração · " + admin.getNome(), 1120, 810);
        JTabbedPane abas = new JTabbedPane();
        abas.setFont(new Font("SansSerif", Font.BOLD, 16));
        abas.addTab("Filmes", criarPainelFilme());
        abas.addTab("Produtos", criarPainelProduto());
        abas.addTab("Sessões", criarPainelSessao());
        abas.addTab("Pedidos", criarPainelPedidos());
        abas.addChangeListener(e -> {
            if (abas.getSelectedIndex() == 2) carregarFilmes();
            if (abas.getSelectedIndex() == 3) carregarPedidos();
        });
        setContentPane(TemaAstra.areaPrincipal("Painel administrativo", admin.getNome(), abas));
    }

    private JPanel criarPainelFilme() {
        JPanel form = TemaAstra.formulario();
        JTextField titulo = TemaAstra.campo(23);
        JTextField duracao = TemaAstra.campo(23);
        JTextField genero = TemaAstra.campo(23);
        JTextField diretor = TemaAstra.campo(23);
        JTextArea sinopse = new JTextArea(3, 22);
        sinopse.setLineWrap(true); sinopse.setWrapStyleWord(true);
        sinopse.setFont(new Font("SansSerif", Font.PLAIN, 16));
        sinopse.setBackground(TemaAstra.CAMPO);
        sinopse.setForeground(TemaAstra.TEXTO_ESCURO);
        sinopse.setCaretColor(TemaAstra.TEXTO_ESCURO);
        sinopse.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JScrollPane rolagemSinopse = new JScrollPane(sinopse);
        rolagemSinopse.setPreferredSize(new Dimension(480, 96));
        JComboBox<Filme.ClassificacaoIndicativa> classificacao =
                new JComboBox<>(Filme.ClassificacaoIndicativa.values());
        JCheckBox dublado = new JCheckBox("Dublado");
        JCheckBox ativo = new JCheckBox("Filme ativo", true);
        dublado.setForeground(TemaAstra.TEXTO); ativo.setForeground(TemaAstra.TEXTO);
        dublado.setBackground(TemaAstra.CARTAO); ativo.setBackground(TemaAstra.CARTAO);
        dublado.setFont(new Font("SansSerif", Font.PLAIN, 16));
        ativo.setFont(new Font("SansSerif", Font.PLAIN, 16));
        JPanel opcoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 2)); opcoes.setOpaque(false);
        opcoes.add(dublado); opcoes.add(ativo);
        JButton salvar = TemaAstra.botao("Salvar filme");
        TemaAstra.linha(form, 0, "Título do filme", titulo);
        TemaAstra.linha(form, 1, "Duração (minutos)", duracao);
        TemaAstra.linha(form, 2, "Gênero", genero);
        TemaAstra.linha(form, 3, "Diretor", diretor);
        TemaAstra.linha(form, 4, "Classificação indicativa", classificacao);
        TemaAstra.linha(form, 5, "Sinopse", rolagemSinopse);
        TemaAstra.linha(form, 6, "Opções", opcoes);
        TemaAstra.acoes(form, 7, salvar);
        salvar.addActionListener(e -> {
            final int minutos;
            try { minutos = Integer.parseInt(duracao.getText().trim()); }
            catch (NumberFormatException ex) { TemaAstra.erro(this, "Informe uma duração válida em minutos."); duracao.requestFocusInWindow(); return; }
            String nome = titulo.getText().trim(), gen = genero.getText().trim(), dir = diretor.getText().trim();
            if (nome.isEmpty() || gen.isEmpty() || dir.isEmpty() || minutos <= 0) {
                TemaAstra.erro(this, "Preencha título, gênero, diretor e duração maior que zero."); return;
            }
            Filme.ClassificacaoIndicativa faixa = (Filme.ClassificacaoIndicativa) classificacao.getSelectedItem();
            boolean ehAtivo = ativo.isSelected(), ehDublado = dublado.isSelected();
            String resumo = sinopse.getText().trim();
            executar(salvar, "Filme salvo com sucesso.", () ->
                            filmeService.cadastrarFilme(admin, nome, minutos, faixa, gen, dir, ehAtivo, ehDublado, resumo),
                    () -> titulo.setText(""));
        });
        return TemaAstra.paginaFormulario("Cadastrar filme", form, 630);
    }

    private JPanel criarPainelProduto() {
        JPanel form = TemaAstra.formulario();
        JTextField nome = TemaAstra.campo(22);
        JTextField descricao = TemaAstra.campo(22);
        JTextField preco = TemaAstra.campo(22);
        JTextField estoque = TemaAstra.campo(22);
        JComboBox<Produto.CategoriaProduto> categoria = new JComboBox<>(Produto.CategoriaProduto.values());
        JButton salvar = TemaAstra.botao("Salvar produto");
        TemaAstra.linha(form, 0, "Nome do produto", nome);
        TemaAstra.linha(form, 1, "Descrição", descricao);
        TemaAstra.linha(form, 2, "Preço (R$)", preco);
        TemaAstra.linha(form, 3, "Quantidade em estoque", estoque);
        TemaAstra.linha(form, 4, "Categoria", categoria);
        TemaAstra.acoes(form, 5, salvar);
        salvar.addActionListener(e -> {
            final double valor; final int quantidade;
            try {
                valor = Double.parseDouble(preco.getText().trim().replace(',', '.'));
                quantidade = Integer.parseInt(estoque.getText().trim());
            } catch (NumberFormatException ex) {
                TemaAstra.erro(this, "Preço e quantidade devem ser números válidos."); return;
            }
            String n = nome.getText().trim(), d = descricao.getText().trim();
            if (n.isEmpty() || valor < 0 || quantidade < 0 || !Double.isFinite(valor)) {
                TemaAstra.erro(this, "Informe o nome, um preço não negativo e um estoque válido."); return;
            }
            Produto.CategoriaProduto cat = (Produto.CategoriaProduto) categoria.getSelectedItem();
            executar(salvar, "Produto salvo com sucesso.", () ->
                            produtoService.cadastrarProduto(admin, n, d, valor, quantidade, cat),
                    () -> nome.setText(""));
        });
        return TemaAstra.paginaFormulario("Cadastrar produto", form, 630);
    }

    private JPanel criarPainelSessao() {
        JPanel form = TemaAstra.formulario();
        JTextField sala = TemaAstra.campo(20); sala.setText("1");
        JTextField data = TemaAstra.campo(20);
        data.setText(LocalDateTime.now().plusDays(1).withHour(21).withMinute(0).format(FORMATO_DATA));
        JTextField preco = TemaAstra.campo(20); preco.setText("15,00");
        JButton salvar = TemaAstra.botao("Salvar sessão");
        TemaAstra.linha(form, 0, "Filme", comboFilmesSessao);
        TemaAstra.linha(form, 1, "Número da sala", sala);
        TemaAstra.linha(form, 2, "Dia e hora (dd/MM/aaaa HH:mm)", data);
        TemaAstra.linha(form, 3, "Preço do ingresso inteiro (R$)", preco);
        TemaAstra.acoes(form, 4, salvar);
        salvar.addActionListener(e -> {
            final int numeroSala; final LocalDateTime horario; final double valor;
            try {
                numeroSala = Integer.parseInt(sala.getText().trim());
                horario = LocalDateTime.parse(data.getText().trim(), FORMATO_DATA);
                valor = Double.parseDouble(preco.getText().trim().replace(',', '.'));
            } catch (DateTimeParseException ex) {
                TemaAstra.erro(this, "Use o formato dd/MM/aaaa HH:mm para a sessão."); return;
            } catch (NumberFormatException ex) {
                TemaAstra.erro(this, "Número da sala e preço devem ser números válidos."); return;
            }
            Filme filme = (Filme) comboFilmesSessao.getSelectedItem();
            if (filme == null || numeroSala <= 0 || !Double.isFinite(valor) || valor < 0) {
                TemaAstra.erro(this, "Escolha um filme, sala válida e preço não negativo."); return;
            }
            executar(salvar, "Sessão salva com sucesso.", () ->
                            sessaoService.cadastrarSessao(admin, filme, numeroSala, horario, valor),
                    () -> { });
        });
        return TemaAstra.paginaFormulario("Cadastrar sessão", form, 630);
    }

    /** O carregamento do SQLite ocorre fora da thread da interface. */
    private void carregarFilmes() {
        new SwingWorker<java.util.List<Filme>, Void>() {
            @Override protected java.util.List<Filme> doInBackground() throws Exception { return filmeService.listarFilmes(); }
            @Override protected void done() {
                try {
                    comboFilmesSessao.removeAllItems();
                    for (Filme filme : get()) if (filme.isAtivo()) comboFilmesSessao.addItem(filme);
                } catch (Exception ex) { erroDoWorker("Não foi possível carregar os filmes", ex); }
            }
        }.execute();
    }

    private JPanel criarPainelPedidos() {
        JPanel p = TemaAstra.painel(); p.setLayout(new BorderLayout(0, 18));
        JTable tabela = new JTable(pedidosModelo);
        TemaAstra.configurarTabela(tabela);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(new JScrollPane(tabela), BorderLayout.CENTER);
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        acoes.setOpaque(false);
        JButton atualizar = TemaAstra.botaoSecundario("Atualizar lista");
        JButton pronto = TemaAstra.botao("Marcar como pronto");
        JButton retirado = TemaAstra.botao("Marcar como retirado");
        acoes.add(atualizar); acoes.add(pronto); acoes.add(retirado);
        atualizar.addActionListener(e -> carregarPedidos());
        pronto.addActionListener(e -> alterarPedido(tabela.getSelectedRow(), tabela, false));
        retirado.addActionListener(e -> alterarPedido(tabela.getSelectedRow(), tabela, true));
        p.add(acoes, BorderLayout.SOUTH);
        return TemaAstra.pagina("Pedidos dos clientes", p);
    }

    private void carregarPedidos() {
        new SwingWorker<java.util.List<Pedido>, Void>() {
            @Override protected java.util.List<Pedido> doInBackground() throws Exception {
                return pedidoRepositorio.listarTodos();
            }
            @Override protected void done() {
                try {
                    pedidosListados.clear(); pedidosListados.addAll(get());
                    pedidosModelo.setRowCount(0);
                    for (Pedido p : pedidosListados) pedidosModelo.addRow(new Object[]{p.getId(),
                            p.getCliente().getNome(), TemaAstra.dinheiro(p.calcularTotal()), p.getStatus()});
                } catch (Exception ex) { erroDoWorker("Não foi possível carregar os pedidos", ex); }
            }
        }.execute();
    }

    private void alterarPedido(int linhaVisual, JTable tabela, boolean retirar) {
        int linha = linhaVisual < 0 ? -1 : tabela.convertRowIndexToModel(linhaVisual);
        if (linha < 0 || linha >= pedidosListados.size()) {
            TemaAstra.erro(this, "Selecione um pedido na tabela."); return;
        }
        Pedido pedido = pedidosListados.get(linha);
        try {
            if (retirar) pedido.retirar(); else pedido.marcarComoPronto();
        } catch (IllegalStateException ex) { TemaAstra.erro(this, ex.getMessage()); return; }
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                pedidoRepositorio.atualizarStatus(pedido.getId(), pedido.getStatus()); return null;
            }
            @Override protected void done() {
                try { get(); TemaAstra.sucesso(TelaAdmin.this, "Status atualizado."); }
                catch (Exception ex) { erroDoWorker("Não foi possível atualizar o pedido", ex); }
                carregarPedidos();
            }
        }.execute();
    }

    @FunctionalInterface
    private interface Tarefa { void executar() throws Exception; }

    private void executar(JButton botao, String mensagem, Tarefa tarefa, Runnable depois) {
        botao.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception { tarefa.executar(); return null; }
            @Override protected void done() {
                botao.setEnabled(true);
                try { get(); TemaAstra.sucesso(TelaAdmin.this, mensagem); depois.run(); }
                catch (Exception ex) { erroDoWorker("Não foi possível salvar", ex); }
            }
        }.execute();
    }

    private void erroDoWorker(String contexto, Exception ex) {
        Throwable causa = ex instanceof ExecutionException && ex.getCause() != null ? ex.getCause() : ex;
        TemaAstra.erro(this, contexto + ": " + causa.getMessage());
    }
}
