package astra.interfacegrafica;

import astra.modelo.Administrador;
import astra.modelo.Filme;
import astra.modelo.Pedido;
import astra.modelo.Produto;
import astra.modelo.RegistroDeAuditoria;
import astra.modelo.Sessao;
import astra.servico.FilmeService;
import astra.servico.PedidoService;
import astra.servico.ProdutoService;
import astra.servico.SessaoService;
import astra.servico.TrilhaDeAuditoria;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Área do administrador: cada aba lista o que já existe (filmes, produtos, sessões, pedidos) e
 * oferece as operações pertinentes, com os formulários em janelas de diálogo.
 *
 * <ul>
 *   <li><b>Filmes:</b> cadastrar, editar e ativar/desativar (pelo campo "Filme ativo").</li>
 *   <li><b>Produtos:</b> cadastrar, editar dados e repor estoque.</li>
 *   <li><b>Sessões:</b> cadastrar, editar e cancelar (só sem ingressos vendidos).</li>
 *   <li><b>Pedidos:</b> acompanhar e marcar como pronto ou retirado.</li>
 *   <li><b>Auditoria:</b> consultar quem fez o quê (somente leitura).</li>
 * </ul>
 *
 * A tela só interpreta o que foi digitado e mostra o resultado: as regras ficam nos serviços.
 * Toda consulta ou gravação roda em segundo plano (SwingWorker), sem travar a janela.
 */
public class TelaAdmin extends JFrame {
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_DATA_COM_SEGUNDOS = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final Font FONTE_TEXTO = new Font("SansSerif", Font.PLAIN, 16);

    /** Administrador logado: identifica quem altera os dados. */
    private final Administrador admin;
    /** Casos de uso de filmes. */
    private final FilmeService filmeService;
    /** Casos de uso de produtos e estoque. */
    private final ProdutoService produtoService;
    /** Casos de uso de sessões. */
    private final SessaoService sessaoService;
    /** Acompanhamento dos pedidos. */
    private final PedidoService pedidoService;
    /** Consulta da trilha de auditoria. */
    private final TrilhaDeAuditoria trilha;

    /** Linhas da tabela de filmes. */
    private final DefaultTableModel filmesModelo = modeloDeTabela("Título", "Gênero", "Duração", "Classificação", "Situação");
    /** Linhas da tabela de produtos. */
    private final DefaultTableModel produtosModelo = modeloDeTabela("Produto", "Categoria", "Preço", "Estoque");
    /** Linhas da tabela de sessões. */
    private final DefaultTableModel sessoesModelo = modeloDeTabela("Filme", "Sala", "Data e hora", "Preço", "Vendidos", "Situação");
    /** Linhas da tabela de pedidos. */
    private final DefaultTableModel pedidosModelo = modeloDeTabela("Pedido", "Cliente", "Data", "Total", "Status");
    /** Linhas da tabela de auditoria. */
    private final DefaultTableModel auditoriaModelo = modeloDeTabela("Quando", "Usuário (id)", "Ação", "Detalhe");

    /** Filmes mostrados na tabela, na mesma ordem das linhas. */
    private final List<Filme> filmesListados = new ArrayList<>();
    /** Produtos mostrados na tabela, na mesma ordem das linhas. */
    private final List<Produto> produtosListados = new ArrayList<>();
    /** Sessões mostradas na tabela, na mesma ordem das linhas. */
    private final List<Sessao> sessoesListadas = new ArrayList<>();
    /** Pedidos mostrados na tabela, na mesma ordem das linhas. */
    private final List<Pedido> pedidosListados = new ArrayList<>();

    /**
     * Monta a janela da administração e carrega as quatro listas.
     *
     * @param admin             administrador logado (identifica quem altera os dados)
     * @param filmeService      casos de uso de filmes
     * @param produtoService    casos de uso de produtos e estoque
     * @param sessaoService     casos de uso de sessões
     * @param pedidoService     acompanhamento dos pedidos (listar e atualizar o status)
     * @param trilha            consulta da trilha de auditoria
     */
    public TelaAdmin(Administrador admin, FilmeService filmeService, ProdutoService produtoService,
                     SessaoService sessaoService, PedidoService pedidoService, TrilhaDeAuditoria trilha) {
        this.admin = admin;
        this.filmeService = filmeService;
        this.produtoService = produtoService;
        this.sessaoService = sessaoService;
        this.pedidoService = pedidoService;
        this.trilha = trilha;
        TemaAstra.configurarJanela(this, "Administração · " + admin.getNome(), 1120, 700);

        JTabbedPane abas = new JTabbedPane();
        abas.setFont(new Font("SansSerif", Font.BOLD, 16));
        abas.addTab("Filmes", criarAbaFilmes());
        abas.addTab("Produtos", criarAbaProdutos());
        abas.addTab("Sessões", criarAbaSessoes());
        abas.addTab("Pedidos", criarAbaPedidos());
        abas.addTab("Auditoria", criarAbaAuditoria());
        abas.addChangeListener(e -> recarregarAbaSelecionada(abas.getSelectedIndex()));
        setContentPane(TemaAstra.areaPrincipal("Painel administrativo", admin.getNome(), abas));

        carregarFilmes();
        carregarProdutos();
        carregarSessoes();
        carregarPedidos();
        carregarAuditoria();
    }

    private void recarregarAbaSelecionada(int indice) {
        switch (indice) {
            case 0: carregarFilmes(); break;
            case 1: carregarProdutos(); break;
            case 2: carregarFilmes(); carregarSessoes(); break;
            case 3: carregarPedidos(); break;
            default: carregarAuditoria(); break;
        }
    }

    // =====================================================================
    // ABA FILMES
    // =====================================================================

    private JPanel criarAbaFilmes() {
        JTable tabela = criarTabela(filmesModelo);
        JButton atualizar = TemaAstra.botaoSecundario("Atualizar lista");
        JButton editar = TemaAstra.botaoSecundario("Editar selecionado");
        JButton novo = TemaAstra.botao("Novo filme");
        atualizar.addActionListener(e -> carregarFilmes());
        novo.addActionListener(e -> abrirFormularioFilme(null));
        editar.addActionListener(e -> {
            Filme escolhido = selecionado(tabela, filmesListados);
            if (escolhido != null) abrirFormularioFilme(escolhido);
        });
        ligarSelecao(tabela, editar, editar);
        return TemaAstra.pagina("Filmes", painelDaLista(tabela, atualizar, editar, novo));
    }

    private void carregarFilmes() {
        executar(this, filmeService::listarFilmes, lista -> {
            filmesListados.clear();
            filmesListados.addAll(lista);
            filmesModelo.setRowCount(0);
            for (Filme f : lista) {
                filmesModelo.addRow(new Object[]{f.getTitulo(), f.getGenero(), f.getDuracaoMinutos() + " min",
                        f.getClassificacaoIndicativa(), f.isAtivo() ? "Ativo" : "Inativo"});
            }
        });
    }

    /** Abre o formulário de filme; com {@code existente == null} é um cadastro novo, senão é edição. */
    private void abrirFormularioFilme(Filme existente) {
        boolean edicao = existente != null;
        JTextField titulo = TemaAstra.campo(24);
        JTextField duracao = TemaAstra.campo(10);
        JTextField genero = TemaAstra.campo(12);
        JTextField diretor = TemaAstra.campo(12);
        JComboBox<Filme.ClassificacaoIndicativa> classificacao = new JComboBox<>(Filme.ClassificacaoIndicativa.values());
        JTextArea sinopse = new JTextArea(3, 24);
        sinopse.setLineWrap(true);
        sinopse.setWrapStyleWord(true);
        sinopse.setFont(FONTE_TEXTO);
        sinopse.setBackground(TemaAstra.CAMPO);
        sinopse.setForeground(TemaAstra.TEXTO);
        sinopse.setCaretColor(TemaAstra.TEXTO);
        sinopse.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane rolagemSinopse = new JScrollPane(sinopse);
        rolagemSinopse.setPreferredSize(new Dimension(300, 86));
        JCheckBox dublado = caixa("Dublado", false);
        JCheckBox ativo = caixa("Filme ativo (aparece para o cliente)", true);
        if (edicao) {
            titulo.setText(existente.getTitulo());
            duracao.setText(String.valueOf(existente.getDuracaoMinutos()));
            genero.setText(existente.getGenero());
            diretor.setText(existente.getDiretor());
            classificacao.setSelectedItem(existente.getClassificacaoIndicativa());
            sinopse.setText(existente.getSinopse());
            dublado.setSelected(existente.isDublado());
            ativo.setSelected(existente.isAtivo());
        }

        JPanel form = formularioEmGrade();
        adicionar(form, 0, 0, 2, "Título do filme", titulo);
        adicionar(form, 0, 1, 1, "Duração (minutos)", duracao);
        adicionar(form, 1, 1, 1, "Classificação indicativa", classificacao);
        adicionar(form, 0, 2, 1, "Gênero", genero);
        adicionar(form, 1, 2, 1, "Diretor", diretor);
        adicionar(form, 0, 3, 2, "Sinopse", rolagemSinopse);
        JPanel opcoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        opcoes.setOpaque(false);
        opcoes.add(dublado);
        opcoes.add(ativo);
        adicionar(form, 0, 4, 2, "Opções", opcoes);

        JButton cancelar = TemaAstra.botaoSecundario("Cancelar");
        JButton salvar = TemaAstra.botao(edicao ? "Salvar alterações" : "Cadastrar filme");
        JDialog dialogo = criarDialogo(edicao ? "Editar filme" : "Novo filme", form, 5, cancelar, salvar);
        cancelar.addActionListener(e -> dialogo.dispose());
        salvar.addActionListener(e -> {
            final int minutos;
            try {
                minutos = Integer.parseInt(duracao.getText().trim());
            } catch (NumberFormatException ex) {
                TemaAstra.erro(dialogo, "Informe a duração em minutos, só com números.");
                duracao.requestFocusInWindow();
                return;
            }
            Filme.ClassificacaoIndicativa faixa = (Filme.ClassificacaoIndicativa) classificacao.getSelectedItem();
            String nome = titulo.getText(), gen = genero.getText(), dir = diretor.getText(), resumo = sinopse.getText();
            boolean ehAtivo = ativo.isSelected(), ehDublado = dublado.isSelected();
            salvar.setEnabled(false);
            executar(dialogo, () -> edicao
                            ? filmeService.atualizarFilme(admin, existente.getId(), nome, minutos, faixa, gen, dir, ehAtivo, ehDublado, resumo)
                            : filmeService.cadastrarFilme(admin, nome, minutos, faixa, gen, dir, ehAtivo, ehDublado, resumo),
                    filme -> {
                        dialogo.dispose();
                        carregarFilmes();
                        TemaAstra.sucesso(this, "Filme \"" + filme.getTitulo() + "\" salvo com sucesso.");
                    },
                    () -> salvar.setEnabled(true));
        });
        mostrar(dialogo, titulo);
    }

    // =====================================================================
    // ABA PRODUTOS
    // =====================================================================

    private JPanel criarAbaProdutos() {
        JTable tabela = criarTabela(produtosModelo);
        JButton atualizar = TemaAstra.botaoSecundario("Atualizar lista");
        JButton editar = TemaAstra.botaoSecundario("Editar selecionado");
        JButton repor = TemaAstra.botaoSecundario("Repor estoque");
        JButton novo = TemaAstra.botao("Novo produto");
        atualizar.addActionListener(e -> carregarProdutos());
        novo.addActionListener(e -> abrirFormularioProduto(null));
        editar.addActionListener(e -> {
            Produto escolhido = selecionado(tabela, produtosListados);
            if (escolhido != null) abrirFormularioProduto(escolhido);
        });
        repor.addActionListener(e -> {
            Produto escolhido = selecionado(tabela, produtosListados);
            if (escolhido != null) abrirReposicaoDeEstoque(escolhido);
        });
        ligarSelecao(tabela, editar, editar, repor);
        return TemaAstra.pagina("Produtos", painelDaLista(tabela, atualizar, repor, editar, novo));
    }

    private void carregarProdutos() {
        executar(this, produtoService::listarProdutos, lista -> {
            produtosListados.clear();
            produtosListados.addAll(lista);
            produtosModelo.setRowCount(0);
            for (Produto p : lista) {
                produtosModelo.addRow(new Object[]{p.getNome(), p.getCategoria(), TemaAstra.dinheiro(p.getPreco()),
                        p.getEstoque() + (p.getEstoque() == 0 ? "  (esgotado)" : "")});
            }
        });
    }

    /** Cadastro ou edição de produto. Na edição o estoque não aparece: ele só muda pela reposição ou pela venda. */
    private void abrirFormularioProduto(Produto existente) {
        boolean edicao = existente != null;
        JTextField nome = TemaAstra.campo(24);
        JTextField descricao = TemaAstra.campo(24);
        JTextField preco = TemaAstra.campo(10);
        JTextField estoque = TemaAstra.campo(10);
        JComboBox<Produto.CategoriaProduto> categoria = new JComboBox<>(Produto.CategoriaProduto.values());
        if (edicao) {
            nome.setText(existente.getNome());
            descricao.setText(existente.getDescricao());
            preco.setText(String.format(PT_BR, "%.2f", existente.getPreco()));
            categoria.setSelectedItem(existente.getCategoria());
        } else {
            estoque.setText("0");
        }

        JPanel form = formularioEmGrade();
        adicionar(form, 0, 0, 2, "Nome do produto", nome);
        adicionar(form, 0, 1, 2, "Descrição", descricao);
        adicionar(form, 0, 2, 1, "Preço (R$)", preco);
        adicionar(form, 1, 2, 1, "Categoria", categoria);
        if (edicao) {
            JLabel aviso = TemaAstra.rotulo("Estoque atual: " + existente.getEstoque()
                    + ". Para alterar, use o botão \"Repor estoque\".");
            adicionar(form, 0, 3, 2, "Estoque", aviso);
        } else {
            adicionar(form, 0, 3, 1, "Quantidade em estoque", estoque);
        }

        JButton cancelar = TemaAstra.botaoSecundario("Cancelar");
        JButton salvar = TemaAstra.botao(edicao ? "Salvar alterações" : "Cadastrar produto");
        JDialog dialogo = criarDialogo(edicao ? "Editar produto" : "Novo produto", form, 4, cancelar, salvar);
        cancelar.addActionListener(e -> dialogo.dispose());
        salvar.addActionListener(e -> {
            final double valor;
            final int quantidade;
            try {
                valor = lerDecimal(preco.getText());
                quantidade = edicao ? 0 : Integer.parseInt(estoque.getText().trim());
            } catch (NumberFormatException ex) {
                TemaAstra.erro(dialogo, "Preço e quantidade devem ser números válidos.");
                return;
            }
            String n = nome.getText(), d = descricao.getText();
            Produto.CategoriaProduto cat = (Produto.CategoriaProduto) categoria.getSelectedItem();
            salvar.setEnabled(false);
            executar(dialogo, () -> edicao
                            ? produtoService.atualizarProduto(admin, existente.getId(), n, d, valor, cat)
                            : produtoService.cadastrarProduto(admin, n, d, valor, quantidade, cat),
                    produto -> {
                        dialogo.dispose();
                        carregarProdutos();
                        TemaAstra.sucesso(this, "Produto \"" + produto.getNome() + "\" salvo com sucesso.");
                    },
                    () -> salvar.setEnabled(true));
        });
        mostrar(dialogo, nome);
    }

    /** Pergunta quantas unidades chegaram e soma ao estoque (a soma é feita pelo banco, em uma instrução). */
    private void abrirReposicaoDeEstoque(Produto produto) {
        JSpinner quantidade = new JSpinner(new SpinnerNumberModel(10, 1, ProdutoService.LIMITE_DE_REPOSICAO, 1));
        JPanel form = formularioEmGrade();
        adicionar(form, 0, 0, 2, "Produto", TemaAstra.rotulo(produto.getNome() + "  •  estoque atual: " + produto.getEstoque()));
        adicionar(form, 0, 1, 1, "Unidades recebidas", quantidade);

        JButton cancelar = TemaAstra.botaoSecundario("Cancelar");
        JButton confirmar = TemaAstra.botao("Repor estoque");
        JDialog dialogo = criarDialogo("Repor estoque", form, 2, cancelar, confirmar);
        cancelar.addActionListener(e -> dialogo.dispose());
        confirmar.addActionListener(e -> {
            final int unidades;
            try {
                quantidade.commitEdit();
                unidades = ((Number) quantidade.getValue()).intValue();
            } catch (java.text.ParseException ex) {
                TemaAstra.erro(dialogo, "Digite uma quantidade inteira válida.");
                return;
            }
            confirmar.setEnabled(false);
            executar(dialogo, () -> produtoService.reporEstoque(admin, produto.getId(), unidades),
                    atualizado -> {
                        dialogo.dispose();
                        carregarProdutos();
                        TemaAstra.sucesso(this, "Estoque de \"" + atualizado.getNome() + "\" agora é " + atualizado.getEstoque() + ".");
                    },
                    () -> confirmar.setEnabled(true));
        });
        mostrar(dialogo, quantidade);
    }

    // =====================================================================
    // ABA SESSÕES
    // =====================================================================

    private JPanel criarAbaSessoes() {
        JTable tabela = criarTabela(sessoesModelo);
        JButton atualizar = TemaAstra.botaoSecundario("Atualizar lista");
        JButton cancelarSessao = TemaAstra.botaoSecundario("Cancelar sessão");
        JButton editar = TemaAstra.botaoSecundario("Editar selecionada");
        JButton nova = TemaAstra.botao("Nova sessão");
        atualizar.addActionListener(e -> carregarSessoes());
        nova.addActionListener(e -> abrirFormularioSessao(null));
        editar.addActionListener(e -> {
            Sessao escolhida = selecionado(tabela, sessoesListadas);
            if (escolhida != null) abrirFormularioSessao(escolhida);
        });
        cancelarSessao.addActionListener(e -> {
            Sessao escolhida = selecionado(tabela, sessoesListadas);
            if (escolhida != null) cancelarSessao(escolhida);
        });
        ligarSelecao(tabela, editar, editar, cancelarSessao);
        return TemaAstra.pagina("Sessões", painelDaLista(tabela, atualizar, cancelarSessao, editar, nova));
    }

    private void carregarSessoes() {
        executar(this, sessaoService::listarSessoes, lista -> {
            sessoesListadas.clear();
            sessoesListadas.addAll(lista);
            sessoesModelo.setRowCount(0);
            for (Sessao s : lista) {
                sessoesModelo.addRow(new Object[]{s.getFilme().getTitulo(), "Sala " + s.getSala().getNumero(),
                        s.getDataHora().format(FORMATO_DATA), TemaAstra.dinheiro(s.getPrecoBase()),
                        sessaoService.contarIngressosVendidos(s), situacaoDa(s)});
            }
        });
    }

    private String situacaoDa(Sessao sessao) {
        if (sessao.getStatus() == Sessao.StatusSessao.CANCELADA) return "Cancelada";
        if (sessao.getStatus() == Sessao.StatusSessao.ENCERRADA) return "Encerrada";
        return sessao.getDataHora().isBefore(LocalDateTime.now()) ? "Já realizada" : "Agendada";
    }

    /** Cadastro ou edição de sessão. As regras (filme ativo, data futura, sem ingressos vendidos) ficam no serviço. */
    private void abrirFormularioSessao(Sessao existente) {
        boolean edicao = existente != null;
        List<Filme> opcoesDeFilme = new ArrayList<>();
        for (Filme f : filmesListados) {
            if (f.isAtivo() || (edicao && f.getId() == existente.getFilme().getId())) opcoesDeFilme.add(f);
        }
        if (opcoesDeFilme.isEmpty()) {
            TemaAstra.erro(this, "Cadastre ou ative um filme antes de criar uma sessão.");
            return;
        }
        JComboBox<Filme> filme = new JComboBox<>(opcoesDeFilme.toArray(new Filme[0]));
        JTextField sala = TemaAstra.campo(8);
        JTextField data = TemaAstra.campo(14);
        JTextField preco = TemaAstra.campo(10);
        if (edicao) {
            for (Filme f : opcoesDeFilme) {
                if (f.getId() == existente.getFilme().getId()) filme.setSelectedItem(f);
            }
            sala.setText(String.valueOf(existente.getSala().getNumero()));
            data.setText(existente.getDataHora().format(FORMATO_DATA));
            preco.setText(String.format(PT_BR, "%.2f", existente.getPrecoBase()));
        } else {
            sala.setText("1");
            data.setText(LocalDateTime.now().plusDays(1).withHour(21).withMinute(0).format(FORMATO_DATA));
            preco.setText("15,00");
        }

        JPanel form = formularioEmGrade();
        adicionar(form, 0, 0, 2, "Filme", filme);
        adicionar(form, 0, 1, 1, "Número da sala", sala);
        adicionar(form, 1, 1, 1, "Preço do ingresso inteiro (R$)", preco);
        adicionar(form, 0, 2, 2, "Dia e hora (dd/MM/aaaa HH:mm)", data);

        JButton cancelar = TemaAstra.botaoSecundario("Cancelar");
        JButton salvar = TemaAstra.botao(edicao ? "Salvar alterações" : "Cadastrar sessão");
        JDialog dialogo = criarDialogo(edicao ? "Editar sessão" : "Nova sessão", form, 3, cancelar, salvar);
        cancelar.addActionListener(e -> dialogo.dispose());
        salvar.addActionListener(e -> {
            final int numeroSala;
            final LocalDateTime horario;
            final double valor;
            try {
                numeroSala = Integer.parseInt(sala.getText().trim());
                horario = LocalDateTime.parse(data.getText().trim(), FORMATO_DATA);
                valor = lerDecimal(preco.getText());
            } catch (DateTimeParseException ex) {
                TemaAstra.erro(dialogo, "Use o formato dd/MM/aaaa HH:mm para o dia e a hora.");
                data.requestFocusInWindow();
                return;
            } catch (NumberFormatException ex) {
                TemaAstra.erro(dialogo, "Número da sala e preço devem ser números válidos.");
                return;
            }
            Filme escolhido = (Filme) filme.getSelectedItem();
            salvar.setEnabled(false);
            executar(dialogo, () -> edicao
                            ? sessaoService.atualizarSessao(admin, existente.getId(), escolhido, numeroSala, horario, valor)
                            : sessaoService.cadastrarSessao(admin, escolhido, numeroSala, horario, valor),
                    sessao -> {
                        dialogo.dispose();
                        carregarSessoes();
                        TemaAstra.sucesso(this, "Sessão salva com sucesso.");
                    },
                    () -> salvar.setEnabled(true));
        });
        mostrar(dialogo, data);
    }

    private void cancelarSessao(Sessao sessao) {
        int resposta = JOptionPane.showConfirmDialog(this,
                "Cancelar a sessão de \"" + sessao.getFilme().getTitulo() + "\" em "
                        + sessao.getDataHora().format(FORMATO_DATA) + "?\nEla deixará de aparecer para os clientes.",
                "Cancelar sessão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (resposta != JOptionPane.YES_OPTION) return;
        executar(this, () -> sessaoService.cancelarSessao(admin, sessao.getId()), cancelada -> {
            carregarSessoes();
            TemaAstra.sucesso(this, "Sessão cancelada.");
        });
    }

    // =====================================================================
    // ABA PEDIDOS
    // =====================================================================

    private JPanel criarAbaPedidos() {
        JTable tabela = criarTabela(pedidosModelo);
        JButton atualizar = TemaAstra.botaoSecundario("Atualizar lista");
        JButton pronto = TemaAstra.botao("Marcar como pronto");
        JButton retirado = TemaAstra.botao("Marcar como retirado");
        atualizar.addActionListener(e -> carregarPedidos());
        pronto.addActionListener(e -> alterarPedido(tabela, false));
        retirado.addActionListener(e -> alterarPedido(tabela, true));
        ligarSelecao(tabela, null, pronto, retirado);
        return TemaAstra.pagina("Pedidos dos clientes", painelDaLista(tabela, atualizar, pronto, retirado));
    }

    private void carregarPedidos() {
        executar(this, () -> pedidoService.listarTodos(admin), lista -> {
            pedidosListados.clear();
            pedidosListados.addAll(lista);
            pedidosModelo.setRowCount(0);
            for (Pedido p : lista) {
                pedidosModelo.addRow(new Object[]{p.getId(), p.getCliente().getNome(), p.getData().format(FORMATO_DATA),
                        TemaAstra.dinheiro(p.calcularTotal()), p.getStatus()});
            }
        });
    }

    private void alterarPedido(JTable tabela, boolean retirar) {
        Pedido pedido = selecionado(tabela, pedidosListados);
        if (pedido == null) return;
        // a regra (só pago vira pronto, só pronto vira retirado) é do serviço; a tela mostra o resultado
        executar(this, () -> retirar
                        ? pedidoService.marcarComoRetirado(admin, pedido.getId())
                        : pedidoService.marcarComoPronto(admin, pedido.getId()),
                atualizado -> {
                    carregarPedidos();
                    TemaAstra.sucesso(this, "Status atualizado.");
                }, this::carregarPedidos);
    }

    // =====================================================================
    // ABA AUDITORIA (somente leitura)
    // =====================================================================

    private JPanel criarAbaAuditoria() {
        JTable tabela = criarTabela(auditoriaModelo);
        // a ação (SESSAO_CANCELADA, COMPRA_CONCLUIDA...) é o dado principal: precisa de coluna larga o bastante
        tabela.getColumnModel().getColumn(0).setPreferredWidth(170);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(210);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(480);
        JButton atualizar = TemaAstra.botaoSecundario("Atualizar lista");
        atualizar.addActionListener(e -> carregarAuditoria());
        JPanel p = painelDaLista(tabela, atualizar);
        return TemaAstra.pagina("Auditoria: quem fez o quê", p);
    }

    private void carregarAuditoria() {
        executar(this, () -> trilha.listarRecentes(admin, 300), lista -> {
            auditoriaModelo.setRowCount(0);
            for (RegistroDeAuditoria r : lista) {
                auditoriaModelo.addRow(new Object[]{r.quando().format(FORMATO_DATA_COM_SEGUNDOS),
                        r.usuarioId() == null ? "-" : r.usuarioId(), r.acao(), r.detalhe()});
            }
        });
    }

    // =====================================================================
    // APOIO: tabelas, diálogos, formulários e tarefas em segundo plano
    // =====================================================================

    private static DefaultTableModel modeloDeTabela(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int linha, int coluna) { return false; }
        };
    }

    private JTable criarTabela(DefaultTableModel modelo) {
        JTable tabela = new JTable(modelo);
        TemaAstra.configurarTabela(tabela);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // a primeira coluna (nome do filme ou do produto) recebe mais espaço, para o título não ser cortado
        tabela.getColumnModel().getColumn(0).setPreferredWidth(modelo.getColumnName(0).equals("Pedido") ? 80 : 260);
        return tabela;
    }

    /** A linha selecionada corresponde, na mesma posição, ao item da lista; devolve null se nada estiver selecionado. */
    private <T> T selecionado(JTable tabela, List<T> lista) {
        int visual = tabela.getSelectedRow();
        if (visual < 0) {
            TemaAstra.erro(this, "Selecione uma linha na tabela.");
            return null;
        }
        int linha = tabela.convertRowIndexToModel(visual);
        return linha < lista.size() ? lista.get(linha) : null;
    }

    /**
     * Habilita os botões só quando há uma linha selecionada; dar dois cliques na linha aciona o botão principal.
     *
     * @param principal botão acionado pelo duplo clique (pode ser null)
     * @param botoes    botões que dependem de uma linha selecionada
     */
    private void ligarSelecao(JTable tabela, JButton principal, JButton... botoes) {
        for (JButton b : botoes) b.setEnabled(false);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                for (JButton b : botoes) b.setEnabled(tabela.getSelectedRow() >= 0);
            }
        });
        if (principal != null) {
            tabela.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && principal.isEnabled()) principal.doClick();
                }
            });
        }
    }

    private JPanel painelDaLista(JTable tabela, JButton... acoes) {
        JPanel p = TemaAstra.painel();
        p.setLayout(new BorderLayout(0, 18));
        p.add(new JScrollPane(tabela), BorderLayout.CENTER);
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        barra.setOpaque(false);
        for (JButton b : acoes) barra.add(b);
        p.add(barra, BorderLayout.SOUTH);
        return p;
    }

    private JPanel formularioEmGrade() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        return form;
    }

    /** Coloca o rótulo e o campo na grade de duas colunas do formulário. */
    private void adicionar(JPanel form, int coluna, int linha, int largura, String rotulo, JComponent campo) {
        if (campo instanceof JTextField || campo instanceof JComboBox<?> || campo instanceof JSpinner) {
            TemaAstra.prepararEntrada(campo);
        }
        JPanel bloco = new JPanel(new BorderLayout(0, 6));
        bloco.setOpaque(false);
        bloco.add(TemaAstra.destaque(rotulo), BorderLayout.NORTH);
        bloco.add(campo, BorderLayout.CENTER);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = coluna;
        g.gridy = linha;
        g.gridwidth = largura;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, coluna == 0 ? 0 : 8, 14, (coluna == 0 && largura == 1) ? 8 : 0);
        form.add(bloco, g);
    }

    private JCheckBox caixa(String texto, boolean marcada) {
        JCheckBox caixa = new JCheckBox(texto, marcada);
        caixa.setForeground(TemaAstra.TEXTO);
        caixa.setOpaque(false);
        caixa.setFont(FONTE_TEXTO);
        caixa.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 24));
        return caixa;
    }

    /**
     * Monta a janela de diálogo: título, formulário e barra de botões. Enter aciona o último botão
     * (o principal) e Esc fecha.
     *
     * @param linhaDosBotoes linha da grade em que a barra de botões é colocada (logo abaixo do último campo)
     */
    private JDialog criarDialogo(String titulo, JPanel form, int linhaDosBotoes, JButton... botoes) {
        JDialog dialogo = new JDialog(this, titulo, true);
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        barra.setOpaque(false);
        for (JButton b : botoes) barra.add(b);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = linhaDosBotoes;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 0, 0);
        form.add(barra, g);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(TemaAstra.FUNDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        JLabel cabecalho = TemaAstra.titulo(titulo);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        raiz.add(cabecalho, BorderLayout.NORTH);
        raiz.add(form, BorderLayout.CENTER);
        dialogo.setContentPane(raiz);
        dialogo.getRootPane().setDefaultButton(botoes[botoes.length - 1]);
        dialogo.getRootPane().registerKeyboardAction(e -> dialogo.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        return dialogo;
    }

    private void mostrar(JDialog dialogo, JComponent primeiroCampo) {
        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        SwingUtilities.invokeLater(primeiroCampo::requestFocusInWindow);
        dialogo.setVisible(true);
    }

    private double lerDecimal(String texto) {
        double valor = Double.parseDouble(texto.trim().replace(',', '.'));
        if (!Double.isFinite(valor)) throw new NumberFormatException("valor não finito");
        return valor;
    }

    /** Roda a tarefa fora da thread da interface e trata o resultado de volta nela. */
    private <T> void executar(Component pai, Callable<T> tarefa, Consumer<T> aoConcluir) {
        executar(pai, tarefa, aoConcluir, () -> { });
    }

    /**
     * @param pai      componente sobre o qual a mensagem de falha aparece
     * @param aoFalhar chamado depois de mostrar a falha (por exemplo, para reabilitar o botão de salvar)
     */
    private <T> void executar(Component pai, Callable<T> tarefa, Consumer<T> aoConcluir, Runnable aoFalhar) {
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return tarefa.call(); }
            @Override protected void done() {
                try {
                    aoConcluir.accept(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause() == null ? ex : ex.getCause();
                    TemaAstra.erro(pai, causa.getMessage() == null ? causa.getClass().getSimpleName() : causa.getMessage());
                    aoFalhar.run();
                }
            }
        }.execute();
    }
}