package astra;

import astra.interfacegrafica.AstraFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        // inicia a GUI d forma segura
        SwingUtilities.invokeLater(() -> {
            AstraFrame tela = new AstraFrame(); // cria janela
            tela.setVisible(true); // dx janela visível
        });

    }
}