package astra.interfacegrafica;

import javax.swing.*;
import java.awt.*;

public class AstraFrame extends JFrame {

    // COMPONENTES DA TELA
    private final JLabel textoBoasVindas = new JLabel("Bem-vindo(a) ao Sistema do Astra", SwingConstants.CENTER);
    private final JButton botaoCliente =  new JButton("Entrar como Cliente");
    private final JButton botaoAdmin = new JButton("Entrar como Administrador");

    // CONSTRUTOR DA JANELA
    public AstraFrame() {
        mostrarTela();
        configurarEventos();
    }

    private void mostrarTela() {
        setTitle("Astra — Início"); // título da interface
        setDefaultCloseOperation(EXIT_ON_CLOSE); // encerra o programa ao fechar no x
        setSize(400, 200); // tamanho da tela
        setLocationRelativeTo(null); // centraliza a janela na tela
        setLayout(new BorderLayout(8, 8)); // layout principal
        textoBoasVindas.setFont(new Font("Arial", Font.BOLD, 16));
        add(textoBoasVindas, BorderLayout.NORTH); // add componentes na janela

        // organizar os botões lado a lado
        JPanel painelBotoes = new JPanel();
        painelBotoes.add(botaoCliente);
        painelBotoes.add(botaoAdmin);

        add(painelBotoes, BorderLayout.CENTER);
    }

    private void configurarEventos() {
        // evento do botao cliente
        botaoCliente.addActionListener(evento -> {
            JOptionPane.showMessageDialog(this, "Abrindo a tela de compra de ingressos...");
            // ainda em desenvolvimento
        });

        // evento do botao admin
        botaoAdmin.addActionListener(evento -> {
            JOptionPane.showMessageDialog(this, "Abrindo a tela de cadastro de filmes e sessões...");
            // ainda em desenvolvimento
        });
    }

}
