


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

    private TemaAstra() {
    }

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
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
            }
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
    }
}