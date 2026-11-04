package astra;

import astra.excecoes.PersistenciaException;
import astra.interfacegrafica.AstraFrame;
import astra.persistencia.ConexaoBanco;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        // cria as tabelas e os dados iniciais no astra.db (se ainda não existirem)
        ConexaoBanco banco = new ConexaoBanco();
        try {
            banco.inicializar();
        } catch (PersistenciaException e) {
            String detalhe = e.getCause() != null ? "\n\nDetalhe: " + e.getCause().getMessage() : "";
            JOptionPane.showMessageDialog(null, e.getMessage() + detalhe,
                    "Erro no banco de dados", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // inicia a GUI d forma segura
        SwingUtilities.invokeLater(() -> {
            AstraFrame tela = new AstraFrame(); // cria janela
            tela.setVisible(true); // dx janela visível
        });

    }
}
