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
    /** Fundo geral das janelas (roxo quase preto). */
    public static final Color FUNDO = new Color(15, 12, 25);
    /** Fundo dos cartões e painéis, um tom acima do fundo. */
    public static final Color CARTAO = new Color(29, 23, 46);
    /** Fundo alternativo: botões secundários e destaques sobre o cartão. */
    public static final Color CARTAO_ALT = new Color(47, 37, 70);
    /** Cor principal da marca: botões primários e itens selecionados. */
    public static final Color ROXO = new Color(131, 52, 240); // selecionado: roxo vívido
    /** Roxo mais escuro, usado em linhas selecionadas e listas. */
    public static final Color ROXO_ESCURO = new Color(105, 37, 192);
    /** Cor padrão do texto sobre fundo escuro. */
    public static final Color TEXTO = new Color(250, 248, 255);
    /** Texto secundário (rótulos e legendas) sobre fundo escuro. */
    public static final Color SUAVE = new Color(205, 195, 225);
    /** Poltrona disponível (verde). */
    public static final Color LIVRE = new Color(12, 125, 75); // disponível: verde
    /** Poltrona ocupada (vermelho). */
    public static final Color OCUPADO = new Color(184, 43, 69); // ocupado: vermelho
    /** Fundo claro dos campos de entrada. */
    public static final Color CAMPO = new Color(250, 249, 254);
    /** Texto sobre fundo claro (campos e tabelas). */
    public static final Color TEXTO_ESCURO = new Color(33, 27, 47);
    /** Borda de campos e componentes claros. */
    public static final Color BORDA = new Color(202, 191, 224);
    /** Borda discreta dos cartões sobre fundo escuro. */
    public static final Color BORDA_ESCURO = new Color(68, 53, 91);
    /** Contorno do componente que está com o foco do teclado. */
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

    /**
     * Cria um cartão com fundo de alto contraste e borda discreta.
     *
     * @return o painel pronto para receber componentes
     */
    public static JPanel painel() {
        JPanel p = new JPanel();
        p.setBackground(CARTAO);
        p.setBorder(new CompoundBorder(
                new LineBorder(BORDA_ESCURO, 1, true),
                new EmptyBorder(25, 30, 25, 30)));
        return p;
    }

    /**
     * Cria um título grande (30 pt, negrito).
     *
     * @param texto o texto do título
     * @return o rótulo formatado
     */
    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font(FAMILIA, Font.BOLD, 30));
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
                            : getModel().isPressed() ? new Color(37, 26, 54)
                            : getModel().isRollover() ? new Color(69, 52, 94) : CARTAO_ALT;
                    g.setColor(fundo);
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    g.setColor(BORDA_ESCURO);
                    g.setStroke(new BasicStroke(1f));
                    g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                    if (isFocusOwner()) {
                        g.setColor(FOCO);
                        g.setStroke(new BasicStroke(2f));
                        g.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 14, 14);
                    }
                    g.setFont(getFont());
                    g.setColor(ativo ? TEXTO : BORDA_ESCURO);
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
     * Cria o botão de uma poltrona, com desenho próprio para manter o número legível.
     * O FlatLaf continua responsável pelos outros componentes, mas não pode
     * abreviar "01" como "..." quando a largura da grade é recalculada.
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
     * Aplica a um campo de entrada a aparência padrão: letra de 16 px, fundo claro e texto escuro.
     *
     * @param componente campo de texto, lista suspensa ou seletor numérico
     */
    public static void prepararEntrada(JComponent componente) {
        componente.setFont(FONTE);
        componente.setBackground(CAMPO);
        componente.setForeground(TEXTO_ESCURO);
        componente.putClientProperty("JComponent.roundRect", true);
        // A altura sai da medida real da letra e das bordas do componente. Uma altura fixa de 44 px cortava a
        // parte de baixo das letras (o "g" de "gmail") em telas com escala de 125% ou 150%, onde a letra e as
        // margens crescem e a altura fixa, não.
        FontMetrics medidas = componente.getFontMetrics(FONTE);
        int alturaNecessaria = medidas.getHeight() + 24; // listas e seletores: letra + folga do contorno e da seta
        if (componente instanceof JTextField campo) {
            campo.setCaretColor(TEXTO_ESCURO);
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
                    label.setForeground(selecionado ? Color.WHITE : TEXTO_ESCURO);
                    return label;
                });
            }
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
            p.add(destaque(texto), g);
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
     * Mostra uma mensagem de erro com o título padrão "Astra | Atenção".
     * Para escolher o título, use {@link #erro(Component, String, String)}.
     *
     * @param pai      componente sobre o qual a janela de aviso aparece
     * @param mensagem o que aconteceu
     */
    public static void erro(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Astra | Atenção", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Algo que o cliente resolve (poltrona vendida, estoque, pagamento recusado): janela com ícone de aviso.
     *
     * @param pai      componente sobre o qual a janela de aviso aparece
     * @param titulo   título curto da janela
     * @param mensagem o que aconteceu e o que fazer
     */
    public static void aviso(Component pai, String titulo, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Astra | " + titulo, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Falha do sistema (banco fora do ar, erro inesperado): janela com ícone de erro.
     *
     * @param pai      componente sobre o qual a janela de aviso aparece
     * @param titulo   título curto da janela
     * @param mensagem o que aconteceu
     */
    public static void erro(Component pai, String titulo, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Astra | " + titulo, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Mostra uma mensagem de confirmação com o título "Astra | Tudo certo".
     *
     * @param pai      componente sobre o qual a janela aparece
     * @param mensagem o que deu certo
     */
    public static void sucesso(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Astra | Tudo certo", JOptionPane.INFORMATION_MESSAGE);
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
     * Cria a barra superior reutilizada nas áreas de cliente e administração.
     *
     * @param area          nome da área (por exemplo "Área do cliente")
     * @param identificacao nome de quem está logado
     * @return a barra superior
     */
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
        abas.putClientProperty("JTabbedPane.tabType", "underlined");
        abas.putClientProperty("JTabbedPane.tabAreaAlignment", "leading");
        root.add(abas, BorderLayout.CENTER);
        return root;
    }

    /**
     * Monta uma página: trilha ("CINEMA ASTRA / TÍTULO"), título e o conteúdo logo abaixo.
     *
     * @param titulo   título da página
     * @param conteudo o que aparece abaixo do título
     * @return a página
     */
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
     * Aplica à tabela linhas claras e alternadas, para não misturar textos com o fundo escuro.
     *
     * @param tabela a tabela a formatar
     */
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