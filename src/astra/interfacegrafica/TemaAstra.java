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
 * Design system do Cinema Astra (Swing + FlatLaf), na identidade cinematográfica: fundo quase preto,
 * cartões em roxo profundo, detalhes violeta e dourado, títulos serifados e rótulos monoespaçados.
 * Centraliza cores, tipografia, componentes e tamanhos sem misturar regras do domínio.
 */
public final class TemaAstra {
    /** Fundo geral das janelas (quase preto, #07050D). */
    public static final Color FUNDO = new Color(7, 5, 13);
    /** Fundo dos cartões e painéis, um tom acima do fundo (#100C1B). */
    public static final Color CARTAO = new Color(16, 12, 27);
    /** Fundo alternativo: botões secundários e destaques sobre o cartão (#1F182F). */
    public static final Color CARTAO_ALT = new Color(31, 24, 47);
    /** Cor principal da marca: botões primários e itens selecionados (#8334F0). */
    public static final Color ROXO = new Color(131, 52, 240);
    /** Violeta luminoso, usado em filetes, destaques e no estado de foco (#B06CFF). */
    public static final Color ROXO_LUZ = new Color(176, 108, 255);
    /** Roxo escuro, usado em linhas selecionadas e listas. */
    public static final Color ROXO_ESCURO = new Color(85, 35, 145);
    /** Cor padrão do texto sobre fundo escuro (#F6F2FF). */
    public static final Color TEXTO = new Color(246, 242, 255);
    /** Texto secundário (rótulos e legendas) sobre fundo escuro (#B9AED3). */
    public static final Color SUAVE = new Color(185, 174, 211);
    /** Texto discreto, só para detalhes decorativos e itens desabilitados (#6D6386). */
    public static final Color DIM = new Color(109, 99, 134);
    /** Dourado dos pequenos rótulos e da hierarquia visual (#E9C58A). */
    public static final Color DOURADO = new Color(233, 197, 138);
    /** Poltrona disponível (verde, #22C07A). */
    public static final Color LIVRE = new Color(34, 192, 122);
    /** Poltrona ocupada (vermelho, #E0465F). */
    public static final Color OCUPADO = new Color(224, 70, 95);
    /** Fundo dos campos de entrada (escuro, com texto claro). */
    public static final Color CAMPO = new Color(21, 16, 33);
    /** Borda de campos e componentes. */
    public static final Color BORDA = new Color(58, 48, 78);
    /** Borda discreta dos cartões sobre fundo escuro (#2A2240). */
    public static final Color BORDA_ESCURO = new Color(42, 34, 64);
    /** Contorno do componente que está com o foco do teclado. */
    public static final Color FOCO = new Color(230, 183, 255);

    // Fontes nativas do Windows, para não depender de arquivos externos.
    private static final String FAMILIA = "Segoe UI";
    private static final String FAMILIA_SERIF = "Georgia";
    private static final String FAMILIA_MONO = "Consolas";

    /** Fonte padrão do texto (sem serifa, 16 pt). */
    public static final Font FONTE = new Font(FAMILIA, Font.PLAIN, 16);
    /** Fonte dos botões e das abas (negrito). */
    public static final Font FONTE_BOTAO = new Font(FAMILIA, Font.BOLD, 16);
    /** Fonte das legendas de campos (negrito, um pouco menor). */
    public static final Font FONTE_ROTULO = new Font(FAMILIA, Font.BOLD, 15);
    /** Fonte serifada dos títulos de página. */
    public static final Font FONTE_TITULO = new Font(FAMILIA_SERIF, Font.BOLD, 30);
    /** Fonte serifada de apoio (frases de destaque). */
    public static final Font FONTE_SERIF = new Font(FAMILIA_SERIF, Font.PLAIN, 26);
    /** Fonte monoespaçada dos pequenos rótulos em maiúsculas. */
    public static final Font FONTE_MONO = new Font(FAMILIA_MONO, Font.BOLD, 12);

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
        UIManager.put("Panel.foreground", TEXTO);
        UIManager.put("Viewport.background", CARTAO);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("Label.font", FONTE);

        UIManager.put("Button.font", FONTE_BOTAO);
        UIManager.put("Button.background", ROXO);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.arc", 14);
        UIManager.put("Button.disabledText", new Color(124, 114, 150)); // legível, mas claramente inativo
        UIManager.put("Button.minimumHeight", 46);
        UIManager.put("Button.margin", new Insets(12, 19, 12, 19));

        UIManager.put("Component.arc", 12);
        UIManager.put("Component.focusWidth", 2);
        UIManager.put("Component.focusColor", FOCO);
        UIManager.put("TextComponent.arc", 12);

        UIManager.put("TextField.font", FONTE);
        UIManager.put("TextField.background", CAMPO);
        UIManager.put("TextField.foreground", TEXTO);
        UIManager.put("TextField.caretForeground", TEXTO);
        UIManager.put("TextField.selectionBackground", ROXO_ESCURO);
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("PasswordField.font", FONTE);
        UIManager.put("PasswordField.background", CAMPO);
        UIManager.put("PasswordField.foreground", TEXTO);
        UIManager.put("PasswordField.caretForeground", TEXTO);
        UIManager.put("ComboBox.font", FONTE);
        UIManager.put("ComboBox.background", CAMPO);
        UIManager.put("ComboBox.foreground", TEXTO);
        UIManager.put("ComboBox.selectionBackground", ROXO_ESCURO);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("Spinner.font", FONTE);
        UIManager.put("Spinner.background", CAMPO);
        UIManager.put("Spinner.foreground", TEXTO);
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
        UIManager.put("TabbedPane.unselectedForeground", SUAVE);
        UIManager.put("TabbedPane.underlineColor", ROXO_LUZ);
        UIManager.put("TabbedPane.tabHeight", 50);
        UIManager.put("TabbedPane.showTabSeparators", false);
        UIManager.put("TabbedPane.contentAreaColor", FUNDO);

        UIManager.put("Table.font", FONTE);
        UIManager.put("TableHeader.font", FONTE_BOTAO);
        UIManager.put("Table.rowHeight", 46);
        UIManager.put("Table.background", CARTAO);
        UIManager.put("Table.foreground", TEXTO);
        UIManager.put("Table.selectionBackground", ROXO_ESCURO);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("Table.gridColor", BORDA_ESCURO);

        UIManager.put("OptionPane.background", CARTAO);
        UIManager.put("OptionPane.messageForeground", TEXTO);
        UIManager.put("OptionPane.messageFont", FONTE);
        UIManager.put("OptionPane.buttonFont", FONTE_BOTAO);
        UIManager.put("OptionPane.buttonBackground", CARTAO_ALT);
        UIManager.put("OptionPane.buttonForeground", TEXTO);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollBar.thumb", BORDA);
        UIManager.put("ScrollBar.track", CARTAO);
        UIManager.put("ScrollPane.smoothScrolling", true);
        UIManager.put("ToolTip.background", CARTAO_ALT);
        UIManager.put("ToolTip.foreground", TEXTO);
        UIManager.put("ToolTip.font", FONTE);
    }

    /**
     * Cria um cartão escuro com borda discreta e espaçamento consistente.
     *
     * @return o painel pronto para receber componentes
     */
    public static JPanel painel() {
        JPanel p = new JPanel();
        p.setBackground(CARTAO);
        p.setBorder(new CompoundBorder(
                new LineBorder(BORDA_ESCURO, 1, true),
                new EmptyBorder(25, 30, 25, 30)));
        p.putClientProperty("FlatLaf.style", "arc: 18;");
        return p;
    }

    /**
     * Cria um título grande, serifado e em negrito.
     *
     * @param texto o texto do título
     * @return o rótulo formatado
     */
    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FONTE_TITULO);
        l.setForeground(TEXTO);
        return l;
    }

    /**
     * Cria um rótulo de apoio, com cor suave (descrições e informações secundárias).
     *
     * @param texto o texto do rótulo
     * @return o rótulo formatado
     */
    public static JLabel rotulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FONTE);
        l.setForeground(SUAVE);
        return l;
    }

    /**
     * Cria um rótulo em negrito, usado como legenda de campos e como destaque.
     *
     * @param texto o texto do rótulo
     * @return o rótulo formatado
     */
    public static JLabel destaque(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FONTE_ROTULO);
        l.setForeground(TEXTO);
        return l;
    }

    /**
     * Cria um pequeno rótulo em maiúsculas, monoespaçado e dourado (como "ASTRA / FILMES"),
     * usado para dar hierarquia visual acima de títulos e seções.
     *
     * @param texto o texto do rótulo (é convertido para maiúsculas)
     * @return o rótulo formatado
     */
    public static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto.toUpperCase(Locale.ROOT));
        l.setFont(FONTE_MONO);
        l.setForeground(DOURADO);
        return l;
    }

    /**
     * Cria o botão principal de uma tela (roxo, em destaque).
     *
     * @param texto o texto do botão
     * @return o botão formatado
     */
    public static JButton botao(String texto) {
        return criarBotao(texto, ROXO, Color.WHITE, true);
    }

    /**
     * Cria um botão secundário (fundo neutro), para ações que não são a principal da tela.
     *
     * @param texto o texto do botão
     * @return o botão formatado
     */
    public static JButton botaoSecundario(String texto) {
        return criarBotao(texto, CARTAO_ALT, TEXTO, false);
    }

    /**
     * Cria um botão pequeno, como o "−" e o "+" de quantidade. É desenhado à mão (como o botão de poltrona)
     * para que o texto nunca seja abreviado com "...": os botões comuns reservam margem e espaço de foco
     * dos dois lados e, em telas com escala de 125% ou 150%, sobra quase nada para o símbolo.
     * A largura é calculada pela medida do texto e o botão mostra estado desabilitado, foco do teclado e hover.
     *
     * @param texto     o símbolo exibido, por exemplo "−" ou "+"
     * @param descricao o que o botão faz, usada como dica e como nome para leitores de tela
     * @return o botão pronto para uso
     */
    public static JButton botaoCompacto(String texto, String descricao) {
        JButton botao = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    boolean ativo = isEnabled();
                    Color fundo = !ativo ? CARTAO
                            : getModel().isPressed() ? new Color(32, 23, 45)
                            : getModel().isRollover() ? new Color(48, 36, 64) : CARTAO_ALT;
                    g.setColor(fundo);
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g.setColor(ativo ? BORDA : BORDA_ESCURO);
                    g.setStroke(new BasicStroke(1f));
                    g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                    if (isFocusOwner()) {
                        g.setColor(FOCO);
                        g.setStroke(new BasicStroke(2f));
                        g.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 12, 12);
                    }
                    g.setFont(getFont());
                    g.setColor(ativo ? TEXTO : DIM);
                    FontMetrics medidas = g.getFontMetrics();
                    int x = (getWidth() - medidas.stringWidth(getText())) / 2;
                    int y = (getHeight() - medidas.getHeight()) / 2 + medidas.getAscent();
                    g.drawString(getText(), x, y);
                } finally {
                    g.dispose();
                }
            }
        };
        botao.setFont(new Font(FAMILIA, Font.BOLD, 20));
        botao.setForeground(TEXTO);
        botao.setContentAreaFilled(false);
        botao.setBorderPainted(false);
        botao.setOpaque(false);
        botao.setFocusPainted(false); // o foco é desenhado em paintComponent
        botao.setRolloverEnabled(true);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        FontMetrics medidas = botao.getFontMetrics(botao.getFont());
        Dimension tamanho = new Dimension(Math.max(58, medidas.stringWidth(texto) + 28),
                Math.max(46, medidas.getHeight() + 20));
        botao.setPreferredSize(tamanho);
        botao.setMinimumSize(tamanho);
        botao.setToolTipText(descricao);
        botao.getAccessibleContext().setAccessibleName(descricao);
        return botao;
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
            b.setBorder(new CompoundBorder(
                    new LineBorder(principal ? ROXO : BORDA, 1, true),
                    new EmptyBorder(10, 14, 10, 14)));
        }
        b.setPreferredSize(new Dimension(Math.max(165, b.getPreferredSize().width + 24), 48));
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.putClientProperty("JComponent.minimumHeight", 46);
        b.putClientProperty("FlatLaf.style",
                "arc: 14; focusWidth: 2; borderWidth: " + (principal ? "0" : "1")
                        + "; borderColor: #3A304E;"
                        + (principal
                        ? " hoverBackground: #B06CFF; pressedBackground: #6320B0;"
                        : " hoverBackground: #302440; pressedBackground: #20172D;"));
        return b;
    }

    /**
     * Cria o botão de uma poltrona, com desenho próprio para manter o número legível.
     * O FlatLaf continua responsável pelos outros componentes, mas não pode
     * abreviar "01" como "..." quando a largura da grade é recalculada.
     * O número é escuro sobre cores claras (poltrona livre) e branco sobre as demais, para manter o contraste.
     *
     * @param numero o número exibido no botão, por exemplo "03"
     * @param cor    cor de fundo (livre, ocupada ou selecionada)
     * @return o botão da poltrona
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
                    int arco = Math.min(altura, 34);
                    Color fundo = getModel().isPressed() ? cor.darker()
                            : getModel().isRollover() ? cor.brighter() : cor;

                    g.setColor(new Color(0, 0, 0, 45));
                    g.fillRoundRect(3, 4, Math.max(1, largura - 4), Math.max(1, altura - 4), arco, arco);
                    g.setColor(fundo);
                    g.fillRoundRect(1, 1, Math.max(1, largura - 3), Math.max(1, altura - 3), arco, arco);

                    boolean selecionada = ROXO.equals(cor);
                    g.setColor(selecionada ? FOCO : new Color(255, 255, 255, 45));
                    g.setStroke(new BasicStroke(selecionada ? 2.2f : 1f));
                    g.drawRoundRect(1, 1, Math.max(1, largura - 3), Math.max(1, altura - 3), arco, arco);

                    if (isFocusOwner()) {
                        g.setColor(FOCO);
                        g.setStroke(new BasicStroke(2));
                        g.drawRoundRect(4, 4, Math.max(1, largura - 9), Math.max(1, altura - 9), arco - 4, arco - 4);
                    }

                    g.setFont(getFont());
                    g.setColor(luminancia(cor) > 0.35 ? FUNDO : Color.WHITE);
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
        botao.setFont(new Font(FAMILIA_MONO, Font.BOLD, 16));
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

    /** Luminância relativa (0 a 1) de uma cor, para escolher entre texto claro e escuro. */
    private static double luminancia(Color cor) {
        double[] canais = {cor.getRed() / 255.0, cor.getGreen() / 255.0, cor.getBlue() / 255.0};
        for (int i = 0; i < canais.length; i++) {
            canais[i] = canais[i] <= 0.03928 ? canais[i] / 12.92 : Math.pow((canais[i] + 0.055) / 1.055, 2.4);
        }
        return 0.2126 * canais[0] + 0.7152 * canais[1] + 0.0722 * canais[2];
    }

    /**
     * Cria um campo de texto com a aparência padrão do sistema.
     *
     * @param colunas largura do campo, em colunas
     * @return o campo formatado
     */
    public static JTextField campo(int colunas) {
        JTextField c = new JTextField(colunas);
        prepararEntrada(c);
        return c;
    }

    /**
     * Cria um campo de senha com a aparência padrão do sistema.
     *
     * @param colunas largura do campo, em colunas
     * @return o campo formatado
     */
    public static JPasswordField senha(int colunas) {
        JPasswordField c = new JPasswordField(colunas);
        prepararEntrada(c);
        return c;
    }

    /**
     * Aplica a um campo de entrada a aparência padrão: letra de 16 px, fundo escuro e texto claro.
     *
     * @param componente campo de texto, lista suspensa ou seletor numérico
     */
    public static void prepararEntrada(JComponent componente) {
        componente.setFont(FONTE);
        componente.setBackground(CAMPO);
        componente.setForeground(TEXTO);
        componente.putClientProperty("JComponent.roundRect", true);
        // A altura sai da medida real da letra e das bordas do componente. Uma altura fixa de 44 px cortava a
        // parte de baixo das letras (o "g" de "gmail") em telas com escala de 125% ou 150%, onde a letra e as
        // margens crescem e a altura fixa, não.
        FontMetrics medidas = componente.getFontMetrics(FONTE);
        int alturaNecessaria = medidas.getHeight() + 24; // listas e seletores: letra + folga do contorno e da seta
        if (componente instanceof JTextField campo) {
            campo.setCaretColor(TEXTO);
            campo.setSelectionColor(ROXO_ESCURO);
            campo.setSelectedTextColor(Color.WHITE);
            campo.setMargin(new Insets(4, 12, 4, 12));
            Insets bordas = campo.getInsets(); // já inclui a margem e a borda do tema ativo
            alturaNecessaria = medidas.getHeight() + bordas.top + bordas.bottom + 2;
        }
        int altura = Math.max(44, alturaNecessaria);
        componente.putClientProperty("JComponent.minimumHeight", altura);
        if (componente instanceof JComboBox<?> combo) {
            // só troca o desenho padrão: quem definiu um renderer próprio (como o da bomboniere, que mostra
            // o preço formatado) mantém o seu mesmo que este método seja chamado de novo por TemaAstra.linha
            if (combo.getRenderer() instanceof javax.swing.plaf.UIResource) {
                combo.setRenderer((lista, valor, indice, selecionado, foco) -> {
                    JLabel label = new JLabel(valor == null ? "Selecione..." : valor.toString());
                    label.setOpaque(true);
                    label.setFont(FONTE);
                    label.setBorder(new EmptyBorder(9, 12, 9, 12));
                    label.setBackground(selecionado ? ROXO_ESCURO : CAMPO);
                    label.setForeground(selecionado ? Color.WHITE : TEXTO);
                    return label;
                });
            }
            combo.setMaximumRowCount(8);
        }
        if (componente instanceof JSpinner spinner && spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
            JTextField tf = editor.getTextField();
            tf.setFont(FONTE);
            tf.setBackground(CAMPO);
            tf.setForeground(TEXTO);
            tf.setCaretColor(TEXTO);
            tf.setSelectionColor(ROXO_ESCURO);
            tf.setSelectedTextColor(Color.WHITE);
        }
        Dimension d = componente.getPreferredSize();
        componente.setPreferredSize(new Dimension(Math.max(160, d.width), altura));
        componente.setMinimumSize(new Dimension(120, altura));
    }

    /**
     * Cria um cartão com {@link GridBagLayout}, pronto para receber linhas de formulário
     * (veja {@link #linha} e {@link #acoes}).
     *
     * @return o painel do formulário
     */
    public static JPanel formulario() {
        JPanel p = painel();
        p.setLayout(new GridBagLayout());
        return p;
    }

    /**
     * Coloca a legenda acima do respectivo campo, nunca ao lado do valor.
     *
     * @param p      formulário criado por {@link #formulario()}
     * @param indice posição da linha, começando em 0
     * @param texto  legenda do campo (vazia: o campo fica sem legenda)
     * @param campo  componente de entrada
     */
    public static void linha(JPanel p, int indice, String texto, JComponent campo) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        if (!texto.isEmpty()) {
            g.gridy = indice * 2;
            g.insets = new Insets(0, 0, 8, 0);
            JLabel rotulo = destaque(texto);
            rotulo.setForeground(SUAVE);
            p.add(rotulo, g);
        }
        if (campo instanceof JTextField || campo instanceof JComboBox<?> || campo instanceof JSpinner) {
            prepararEntrada(campo);
        }
        g.gridy = indice * 2 + 1;
        g.insets = new Insets(0, 0, 18, 0);
        p.add(campo, g);
    }

    /**
     * Coloca no formulário uma barra de botões alinhada à direita.
     *
     * @param p      formulário criado por {@link #formulario()}
     * @param indice posição da linha, começando em 0
     * @param botoes os botões, da esquerda para a direita
     */
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

    /**
     * Formata um valor em reais, no padrão brasileiro.
     *
     * @param valor o valor em reais
     * @return o texto formatado, por exemplo "R$ 25,00"
     */
    public static String dinheiro(double valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
    }

    /**
     * Mostra uma mensagem de erro com o título padrão "ASTRA / ATENÇÃO".
     * Para escolher o título, use {@link #erro(Component, String, String)}.
     *
     * @param pai      componente sobre o qual a janela de aviso aparece
     * @param mensagem o que aconteceu
     */
    public static void erro(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "ASTRA  /  ATENÇÃO", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Algo que o cliente resolve (poltrona vendida, estoque, pagamento recusado): janela com ícone de aviso.
     *
     * @param pai      componente sobre o qual a janela de aviso aparece
     * @param titulo   título curto da janela
     * @param mensagem o que aconteceu e o que fazer
     */
    public static void aviso(Component pai, String titulo, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "ASTRA  /  " + titulo.toUpperCase(Locale.ROOT),
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Falha do sistema (banco fora do ar, erro inesperado): janela com ícone de erro.
     *
     * @param pai      componente sobre o qual a janela de aviso aparece
     * @param titulo   título curto da janela
     * @param mensagem o que aconteceu
     */
    public static void erro(Component pai, String titulo, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "ASTRA  /  " + titulo.toUpperCase(Locale.ROOT),
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Mostra uma mensagem de confirmação com o título "ASTRA / TUDO CERTO".
     *
     * @param pai      componente sobre o qual a janela aparece
     * @param mensagem o que deu certo
     */
    public static void sucesso(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "ASTRA  /  TUDO CERTO", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Aplica o padrão visual e de tamanho a uma janela: título com o prefixo "ASTRA CINEMA", fecha só a própria
     * janela, fundo escuro e centralizada. O tamanho pedido é reduzido, se preciso, para caber na área útil da
     * tela (sem a barra de tarefas), e o tamanho mínimo também.
     *
     * @param janela  a janela a configurar
     * @param titulo  complemento do título da janela
     * @param largura largura desejada, em pixels
     * @param altura  altura desejada, em pixels
     */
    public static void configurarJanela(JFrame janela, String titulo, int largura, int altura) {
        janela.setTitle("ASTRA CINEMA  •  " + titulo);
        janela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // Nunca maior que a área útil da tela (sem a barra de tarefas): em notebooks de 768 px de altura
        // as janelas de 810 e 830 px passavam da tela e cortavam o rodapé e os botões principais.
        Rectangle util = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        janela.setSize(Math.min(largura, util.width), Math.min(altura, util.height));
        janela.setMinimumSize(new Dimension(Math.min(980, util.width), Math.min(700, util.height)));
        janela.setLocationRelativeTo(null);
        janela.getContentPane().setBackground(FUNDO);
    }

    /**
     * Cria a barra superior reutilizada nas áreas de cliente e administração: marca "A S T R A",
     * nome da área em dourado e identificação de quem está logado.
     *
     * @param area          nome da área (por exemplo "Área do cliente")
     * @param identificacao nome de quem está logado
     * @return a barra superior
     */
    public static JPanel cabecalho(String area, String identificacao) {
        JPanel topo = new JPanel(new BorderLayout(18, 0));
        topo.setBackground(CARTAO);
        topo.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDA_ESCURO), new EmptyBorder(13, 28, 13, 28)));
        JPanel marca = new JPanel(new FlowLayout(FlowLayout.LEFT, 13, 0));
        marca.setOpaque(false);
        JLabel simbolo = new JLabel("A", SwingConstants.CENTER);
        simbolo.setOpaque(true);
        simbolo.setBackground(ROXO);
        simbolo.setForeground(Color.WHITE);
        simbolo.setFont(new Font(FAMILIA_SERIF, Font.BOLD, 25));
        simbolo.setPreferredSize(new Dimension(46, 46));
        simbolo.setBorder(new LineBorder(ROXO_LUZ, 1, true));
        simbolo.putClientProperty("FlatLaf.style", "arc: 14;");
        marca.add(simbolo);
        JPanel coluna = new JPanel(); coluna.setOpaque(false);
        coluna.setLayout(new BoxLayout(coluna, BoxLayout.Y_AXIS));
        JLabel nome = new JLabel("A S T R A");
        nome.setFont(new Font(FAMILIA_SERIF, Font.BOLD, 19));
        nome.setForeground(TEXTO);
        coluna.add(nome); coluna.add(Box.createVerticalStrut(3)); coluna.add(etiqueta(area));
        marca.add(coluna);
        topo.add(marca, BorderLayout.WEST);
        JLabel usuario = rotulo(identificacao);
        usuario.setFont(new Font(FAMILIA, Font.BOLD, 14));
        usuario.setBorder(new EmptyBorder(0, 12, 0, 0));
        topo.add(usuario, BorderLayout.EAST);
        return topo;
    }

    /**
     * Monta a área principal de uma janela: barra superior e abas para navegação previsível.
     *
     * @param area          nome da área, mostrado no cabeçalho
     * @param identificacao nome de quem está logado
     * @param abas          as abas da área
     * @return o painel que vira o conteúdo da janela
     */
    public static JPanel areaPrincipal(String area, String identificacao, JTabbedPane abas) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(FUNDO);
        root.add(cabecalho(area, identificacao), BorderLayout.NORTH);
        abas.setFont(FONTE_BOTAO);
        abas.setBackground(FUNDO);
        abas.setForeground(SUAVE);
        abas.putClientProperty("JTabbedPane.tabType", "underlined");
        abas.putClientProperty("JTabbedPane.tabAreaAlignment", "leading");
        abas.putClientProperty("JTabbedPane.showContentSeparator", true);
        abas.putClientProperty("JTabbedPane.contentSeparatorColor", BORDA_ESCURO);
        root.add(abas, BorderLayout.CENTER);
        return root;
    }

    /**
     * Monta uma página: trilha dourada ("ASTRA / TÍTULO"), título serifado, filete violeta e o conteúdo abaixo.
     *
     * @param titulo   título da página
     * @param conteudo o que aparece abaixo do título
     * @return a página
     */
    public static JPanel pagina(String titulo, JComponent conteudo) {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setBackground(FUNDO);
        p.setBorder(new EmptyBorder(20, 27, 24, 27));
        JPanel cab = new JPanel();
        cab.setOpaque(false);
        cab.setLayout(new BoxLayout(cab, BoxLayout.Y_AXIS));
        JLabel categoria = etiqueta("ASTRA  /  " + titulo);
        categoria.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel nome = titulo(titulo);
        nome.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel filete = new JPanel();
        filete.setOpaque(true);
        filete.setBackground(ROXO_LUZ);
        filete.setPreferredSize(new Dimension(54, 2));
        filete.setMaximumSize(new Dimension(54, 2));
        filete.setAlignmentX(Component.LEFT_ALIGNMENT);
        cab.add(categoria); cab.add(Box.createVerticalStrut(5));
        cab.add(nome); cab.add(Box.createVerticalStrut(8));
        cab.add(filete);
        p.add(cab, BorderLayout.NORTH);
        p.add(conteudo, BorderLayout.CENTER);
        return p;
    }

    /**
     * Centraliza um formulário numa página, mantendo a altura natural e rolagem ao reduzir a janela.
     *
     * @param titulo     título da página
     * @param formulario o formulário criado por {@link #formulario()}
     * @param largura    largura do formulário, em pixels
     * @return a página com o formulário
     */
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

    /**
     * Aplica à tabela o tema escuro: linhas alternadas, cabeçalho dourado com filete violeta.
     *
     * @param tabela a tabela a formatar
     */
    public static void configurarTabela(JTable tabela) {
        tabela.setFont(FONTE);
        tabela.setRowHeight(46);
        tabela.setBackground(CARTAO);
        tabela.setForeground(TEXTO);
        tabela.setSelectionBackground(ROXO_ESCURO);
        tabela.setSelectionForeground(Color.WHITE);
        tabela.setGridColor(BORDA_ESCURO);
        tabela.setShowHorizontalLines(true);
        tabela.setShowVerticalLines(false);
        tabela.setFillsViewportHeight(true);
        tabela.setIntercellSpacing(new Dimension(0, 1));
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableCellRenderer cell = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(table, value, selected, focus, row, col);
                setOpaque(true);
                setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDA_ESCURO), new EmptyBorder(0, 13, 0, 10)));
                setBackground(selected ? ROXO_ESCURO : (row % 2 == 0 ? CARTAO : new Color(22, 16, 35)));
                setForeground(selected ? Color.WHITE : TEXTO);
                return this;
            }
        };
        tabela.setDefaultRenderer(Object.class, cell);
        JTableHeader h = tabela.getTableHeader();
        if (h != null) {
            h.setFont(FONTE_BOTAO);
            h.setBackground(CARTAO_ALT);
            h.setForeground(DOURADO);
            h.setPreferredSize(new Dimension(0, 48));
            h.setReorderingAllowed(false);
            h.setBorder(new MatteBorder(0, 0, 2, 0, ROXO));
        }
    }
}
