package astra.interfacegrafica;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Design system do Cinema Astra (Swing + FlatLaf).
 * Centraliza cores, tipografia, componentes e tamanhos sem misturar regras do domínio.
 */
public final class TemaAstra {
    public static final Color FUNDO = new Color(15, 12, 25);
    public static final Color CARTAO = new Color(29, 23, 46);
    public static final Color CARTAO_ALT = new Color(47, 37, 70);
    public static final Color ROXO = new Color(131, 52, 240); // selecionado: roxo vívido
    public static final Color ROXO_ESCURO = new Color(105, 37, 192);
    public static final Color TEXTO = new Color(250, 248, 255);
    public static final Color SUAVE = new Color(205, 195, 225);
    public static final Color LIVRE = new Color(12, 125, 75); // disponível: verde
    public static final Color OCUPADO = new Color(184, 43, 69); // ocupado: vermelho
    public static final Color CAMPO = new Color(250, 249, 254);
    public static final Color TEXTO_ESCURO = new Color(33, 27, 47);
    public static final Color BORDA = new Color(202, 191, 224);
    public static final Color BORDA_ESCURO = new Color(68, 53, 91);
    public static final Color FOCO = new Color(230, 183, 255);

    private static final String FAMILIA = "Segoe UI";
    private static final Font FONTE = new Font(FAMILIA, Font.PLAIN, 16);
    private static final Font FONTE_BOTAO = new Font(FAMILIA, Font.BOLD, 16);
    private static final Font FONTE_ROTULO = new Font(FAMILIA, Font.BOLD, 15);

    private TemaAstra() { }

    /**
     * Instala o FlatLaf escuro antes da construção dos componentes.
     * O projeto também compila pelo javac original; se o FlatLaf ainda não estiver
     * no classpath, há um fallback para a interface básica até importar o Maven.
     */
    public static void instalar() {
        try {
            Class<?> flatDark = Class.forName("com.formdev.flatlaf.FlatDarkLaf");
            Method setup = flatDark.getMethod("setup");
            setup.invoke(null);
        } catch (ReflectiveOperationException | LinkageError erro) {
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
            catch (Exception ignored) { }
            System.err.println("FlatLaf não encontrado: importe o pom.xml no IntelliJ para ativar o tema Astra.");
        }

        // Valores oficiais de customização do FlatLaf, aplicados ANTES de abrir as telas.
        UIManager.put("defaultFont", FONTE);
        UIManager.put("Panel.background", FUNDO);
        UIManager.put("Viewport.background", CARTAO);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("Label.font", FONTE);
        UIManager.put("Button.font", FONTE_BOTAO);
        UIManager.put("Button.arc", 18);
        UIManager.put("Button.minimumHeight", 46);
        UIManager.put("Button.margin", new Insets(12, 19, 12, 19));
        UIManager.put("Component.arc", 14);
        UIManager.put("Component.focusWidth", 2);
        UIManager.put("Component.focusColor", FOCO);
        UIManager.put("TextComponent.arc", 14);
        UIManager.put("TextField.font", FONTE);
        UIManager.put("TextField.background", CAMPO);
        UIManager.put("TextField.foreground", TEXTO_ESCURO);
        UIManager.put("PasswordField.font", FONTE);
        UIManager.put("PasswordField.background", CAMPO);
        UIManager.put("PasswordField.foreground", TEXTO_ESCURO);
        UIManager.put("ComboBox.font", FONTE);
        UIManager.put("ComboBox.background", CAMPO);
        UIManager.put("ComboBox.foreground", TEXTO_ESCURO);
        UIManager.put("Spinner.font", FONTE);
        UIManager.put("Spinner.background", CAMPO);
        UIManager.put("Spinner.foreground", TEXTO_ESCURO);
        UIManager.put("CheckBox.font", FONTE);
        UIManager.put("CheckBox.background", CARTAO);
        UIManager.put("CheckBox.foreground", TEXTO);
        UIManager.put("CheckBox.icon.focusWidth", 2);
        UIManager.put("TabbedPane.font", FONTE_BOTAO);
        UIManager.put("TabbedPane.background", FUNDO);
        UIManager.put("TabbedPane.foreground", SUAVE);
        UIManager.put("TabbedPane.selectedBackground", CARTAO);
        UIManager.put("TabbedPane.selected", CARTAO_ALT); // compatibilidade sem FlatLaf
        UIManager.put("TabbedPane.selectedForeground", TEXTO);
        UIManager.put("TabbedPane.unselectedBackground", FUNDO);
        UIManager.put("TabbedPane.unselectedForeground", TEXTO);
        UIManager.put("TabbedPane.underlineColor", ROXO);
        UIManager.put("TabbedPane.tabHeight", 52);
        UIManager.put("TabbedPane.showTabSeparators", false);
        UIManager.put("TabbedPane.contentAreaColor", FUNDO);
        UIManager.put("Table.font", FONTE);
        UIManager.put("TableHeader.font", FONTE_BOTAO);
        UIManager.put("Table.rowHeight", 46);
        UIManager.put("Table.selectionBackground", ROXO_ESCURO);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("OptionPane.messageFont", FONTE);
        UIManager.put("OptionPane.buttonFont", FONTE_BOTAO);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollPane.smoothScrolling", true);
    }

    /** Cartão com fundo de alto contraste e borda discreta. */
    public static JPanel painel() {
        JPanel p = new JPanel();
        p.setBackground(CARTAO);
        p.setBorder(new CompoundBorder(
                new LineBorder(BORDA_ESCURO, 1, true),
                new EmptyBorder(25, 30, 25, 30)));
        return p;
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font(FAMILIA, Font.BOLD, 30));
        l.setForeground(TEXTO);
        return l;
    }

    public static JLabel rotulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FONTE);
        l.setForeground(SUAVE);
        return l;
    }

    public static JLabel destaque(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FONTE_ROTULO);
        l.setForeground(TEXTO);
        return l;
    }

    public static JButton botao(String texto) {
        return criarBotao(texto, ROXO, Color.WHITE, true);
    }

    public static JButton botaoSecundario(String texto) {
        return criarBotao(texto, CARTAO_ALT, TEXTO, false);
    }

    private static JButton criarBotao(String texto, Color fundo, Color letra, boolean principal) {
        JButton b = new JButton(texto);
        b.setFont(FONTE_BOTAO);
        b.setForeground(letra);
        b.setBackground(fundo);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(true);
        boolean flatLafAtivo = UIManager.getLookAndFeel().getClass().getName().startsWith("com.formdev.flatlaf");
        if (flatLafAtivo) {
            // O FlatLaf desenha o arco, a borda e o hover nativamente.
            b.setOpaque(false);
        } else {
            // Fallback de desenvolvimento: não deixar o botão transparente no Metal.
            b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            b.setOpaque(true);
            b.setBorder(new EmptyBorder(10, 14, 10, 14));
        }
        b.setPreferredSize(new Dimension(Math.max(165, b.getPreferredSize().width + 24), 48));
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.putClientProperty("JComponent.minimumHeight", 46);
        b.putClientProperty("FlatLaf.style", "arc: 18; borderWidth: 0; focusWidth: 2;"
                + (principal ? " hoverBackground: #9B5CFC; pressedBackground: #6320B0;" : " hoverBackground: #45345E; pressedBackground: #251A36;"));
        return b;
    }

    /**
     * Botão de poltrona com desenho próprio para manter o número legível.
     * O FlatLaf continua responsável pelos outros componentes, mas não pode
     * abreviar "01" como "..." quando a largura da grade é recalculada.
     */
    public static JButton botaoAssento(String numero, Color cor) {
        JButton botao = new JButton(numero) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    int largura = getWidth() - 1;
                    int altura = getHeight() - 1;
                    int arco = Math.min(altura, 40);
                    Color fundo = getModel().isPressed() ? cor.darker()
                            : getModel().isRollover() ? cor.brighter() : cor;
                    g.setColor(fundo);
                    g.fillRoundRect(1, 1, Math.max(1, largura - 2), Math.max(1, altura - 2), arco, arco);
                    if (ROXO.equals(cor)) {
                        g.setColor(Color.WHITE);
                        g.setStroke(new BasicStroke(2.5f));
                        g.drawRoundRect(2, 2, Math.max(1, largura - 4), Math.max(1, altura - 4), arco, arco);
                    }
                    if (isFocusOwner()) {
                        g.setColor(FOCO);
                        g.setStroke(new BasicStroke(2));
                        g.drawRoundRect(4, 4, Math.max(1, largura - 8), Math.max(1, altura - 8), arco - 4, arco - 4);
                    }
                    g.setFont(getFont());
                    g.setColor(Color.WHITE);
                    FontMetrics medidas = g.getFontMetrics();
                    String texto = getText();
                    int x = (getWidth() - medidas.stringWidth(texto)) / 2;
                    int y = (getHeight() - medidas.getHeight()) / 2 + medidas.getAscent();
                    g.drawString(texto, x, y);
                } finally {
                    g.dispose();
                }
            }
        };
        botao.setFont(new Font(FAMILIA, Font.BOLD, 17));
        botao.setForeground(Color.WHITE);
        botao.setContentAreaFilled(false);
        botao.setBorderPainted(false);
        botao.setOpaque(false);
        botao.setFocusPainted(false); // foco desenhado em paintComponent
        botao.setRolloverEnabled(true);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setPreferredSize(new Dimension(74, 52));
        botao.setMinimumSize(new Dimension(64, 48));
        botao.setToolTipText("Poltrona " + numero);
        botao.getAccessibleContext().setAccessibleName("Poltrona " + numero);
        return botao;
    }

    public static JTextField campo(int colunas) {
        JTextField c = new JTextField(colunas);
        prepararEntrada(c);
        return c;
    }

    public static JPasswordField senha(int colunas) {
        JPasswordField c = new JPasswordField(colunas);
        prepararEntrada(c);
        return c;
    }

    /** Campos compactos com letra de 16px, fundo claro e texto escuro. */
    public static void prepararEntrada(JComponent componente) {
        componente.setFont(FONTE);
        componente.setBackground(CAMPO);
        componente.setForeground(TEXTO_ESCURO);
        componente.putClientProperty("JComponent.roundRect", true);
        componente.putClientProperty("JComponent.minimumHeight", 44);
        if (componente instanceof JTextField campo) {
            campo.setCaretColor(TEXTO_ESCURO);
            campo.setSelectionColor(ROXO_ESCURO);
            campo.setSelectedTextColor(Color.WHITE);
            campo.setMargin(new Insets(9, 12, 9, 12));
        }
        if (componente instanceof JComboBox<?> combo) {
            combo.setRenderer((lista, valor, indice, selecionado, foco) -> {
                JLabel label = new JLabel(valor == null ? "Selecione..." : valor.toString());
                label.setOpaque(true);
                label.setFont(FONTE);
                label.setBorder(new EmptyBorder(9, 12, 9, 12));
                label.setBackground(selecionado ? ROXO_ESCURO : CAMPO);
                label.setForeground(selecionado ? Color.WHITE : TEXTO_ESCURO);
                return label;
            });
            combo.setMaximumRowCount(8);
        }
        if (componente instanceof JSpinner spinner && spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
            JTextField tf = editor.getTextField();
            tf.setFont(FONTE);
            tf.setBackground(CAMPO);
            tf.setForeground(TEXTO_ESCURO);
            tf.setCaretColor(TEXTO_ESCURO);
        }
        Dimension d = componente.getPreferredSize();
        componente.setPreferredSize(new Dimension(Math.max(160, d.width), 44));
        componente.setMinimumSize(new Dimension(120, 44));
    }

    public static JPanel formulario() {
        JPanel p = painel();
        p.setLayout(new GridBagLayout());
        return p;
    }

    /** Coloca a legenda acima do respectivo campo, nunca ao lado do valor. */
    public static void linha(JPanel p, int indice, String texto, JComponent campo) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        if (!texto.isEmpty()) {
            g.gridy = indice * 2;
            g.insets = new Insets(0, 0, 8, 0);
            p.add(destaque(texto), g);
        }
        if (campo instanceof JTextField || campo instanceof JComboBox<?> || campo instanceof JSpinner) {
            prepararEntrada(campo);
        }
        g.gridy = indice * 2 + 1;
        g.insets = new Insets(0, 0, 18, 0);
        p.add(campo, g);
    }

    public static void acoes(JPanel p, int indice, JButton... botoes) {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        barra.setOpaque(false);
        for (JButton b : botoes) barra.add(b);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = indice * 2; g.gridheight = 2;
        g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 0, 0);
        p.add(barra, g);
    }

    public static String dinheiro(double valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
    }

    public static void erro(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Astra | Atenção", JOptionPane.ERROR_MESSAGE);
    }

    public static void sucesso(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Astra | Tudo certo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void configurarJanela(JFrame janela, String titulo, int largura, int altura) {
        janela.setTitle("ASTRA CINEMA  •  " + titulo);
        janela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        janela.setSize(largura, altura);
        janela.setMinimumSize(new Dimension(980, 700));
        janela.setLocationRelativeTo(null);
        janela.getContentPane().setBackground(FUNDO);
    }

    /** Barra superior reutilizada nas áreas de cliente e administração. */
    public static JPanel cabecalho(String area, String identificacao) {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(CARTAO);
        topo.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDA_ESCURO), new EmptyBorder(15, 28, 15, 28)));
        JPanel marca = new JPanel(new FlowLayout(FlowLayout.LEFT, 13, 0));
        marca.setOpaque(false);
        JLabel simbolo = new JLabel("A", SwingConstants.CENTER);
        simbolo.setOpaque(true);
        simbolo.setBackground(ROXO);
        simbolo.setForeground(Color.WHITE);
        simbolo.setFont(new Font(FAMILIA, Font.BOLD, 26));
        simbolo.setPreferredSize(new Dimension(48, 48));
        marca.add(simbolo);
        JPanel coluna = new JPanel(); coluna.setOpaque(false);
        coluna.setLayout(new BoxLayout(coluna, BoxLayout.Y_AXIS));
        JLabel nome = destaque("ASTRA  CINEMA");
        JLabel categoria = rotulo(area.toUpperCase(Locale.ROOT));
        categoria.setFont(new Font(FAMILIA, Font.BOLD, 12));
        coluna.add(nome); coluna.add(Box.createVerticalStrut(4)); coluna.add(categoria);
        marca.add(coluna);
        topo.add(marca, BorderLayout.WEST);
        JLabel usuario = rotulo(identificacao);
        usuario.setFont(new Font(FAMILIA, Font.BOLD, 15));
        topo.add(usuario, BorderLayout.EAST);
        return topo;
    }

    /** Usa barra superior + abas para navegação previsível. */
    public static JPanel areaPrincipal(String area, String identificacao, JTabbedPane abas) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(FUNDO);
        root.add(cabecalho(area, identificacao), BorderLayout.NORTH);
        abas.setFont(FONTE_BOTAO);
        abas.setBackground(FUNDO);
        abas.putClientProperty("JTabbedPane.tabType", "underlined");
        abas.putClientProperty("JTabbedPane.tabAreaAlignment", "leading");
        root.add(abas, BorderLayout.CENTER);
        return root;
    }

    public static JPanel pagina(String titulo, JComponent conteudo) {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setBackground(FUNDO);
        p.setBorder(new EmptyBorder(20, 27, 24, 27));
        JPanel cab = new JPanel(new BorderLayout(0, 7)); cab.setOpaque(false);
        JLabel categoria = rotulo("CINEMA ASTRA  /  " + titulo.toUpperCase(Locale.ROOT));
        categoria.setFont(new Font(FAMILIA, Font.BOLD, 12));
        cab.add(categoria, BorderLayout.NORTH);
        cab.add(titulo(titulo), BorderLayout.CENTER);
        p.add(cab, BorderLayout.NORTH);
        p.add(conteudo, BorderLayout.CENTER);
        return p;
    }

    /** Centraliza o formulário, mantendo altura natural e rolagem ao reduzir a janela. */
    public static JPanel paginaFormulario(String titulo, JPanel formulario, int largura) {
        JPanel centralizador = new JPanel(new GridBagLayout());
        centralizador.setBackground(FUNDO);
        Dimension natural = formulario.getPreferredSize();
        formulario.setPreferredSize(new Dimension(largura, natural.height));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.weightx = 1; g.weighty = 1;
        g.anchor = GridBagConstraints.NORTH;
        g.insets = new Insets(8, 12, 18, 12);
        centralizador.add(formulario, g);
        JScrollPane scroll = new JScrollPane(centralizador);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(FUNDO);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        return pagina(titulo, scroll);
    }

    /** Linhas claras e alternadas para não misturar textos com o fundo escuro. */
    public static void configurarTabela(JTable tabela) {
        tabela.setFont(FONTE);
        tabela.setRowHeight(46);
        tabela.setBackground(CAMPO);
        tabela.setForeground(TEXTO_ESCURO);
        tabela.setSelectionBackground(ROXO_ESCURO);
        tabela.setSelectionForeground(Color.WHITE);
        tabela.setGridColor(new Color(230, 224, 238));
        tabela.setShowHorizontalLines(true);
        tabela.setShowVerticalLines(false);
        tabela.setFillsViewportHeight(true);
        tabela.setIntercellSpacing(new Dimension(0, 1));
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableCellRenderer cell = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(table, value, selected, focus, row, col);
                setBorder(new EmptyBorder(0, 13, 0, 10));
                setBackground(selected ? ROXO_ESCURO : (row % 2 == 0 ? CAMPO : new Color(241, 237, 249)));
                setForeground(selected ? Color.WHITE : TEXTO_ESCURO);
                return this;
            }
        };
        tabela.setDefaultRenderer(Object.class, cell);
        JTableHeader h = tabela.getTableHeader();
        if (h != null) {
            h.setFont(FONTE_BOTAO);
            h.setBackground(CARTAO_ALT);
            h.setForeground(TEXTO);
            h.setPreferredSize(new Dimension(0, 48));
            h.setReorderingAllowed(false);
        }
    }
}
