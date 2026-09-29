package astra.interfacegrafica;

import astra.servico.CinemaService;
import astra.modelo.Administrador;
import astra.modelo.Filme;
import javax.swing.*;
import java.awt.*;

public class TelaAdmin extends JFrame {

    private CinemaService servico = new CinemaService();

    // CAMPOS D TEXTO P FORMULARIO
    private JTextField campoTitulo = new JTextField();
    private JTextField campoDuracao = new JTextField();
    private JButton botaoSalvar = new JButton("Salvar Filme");

    public TelaAdmin() {
        setTitle("Área do Administrador - Cadastrar Filme");
        setSize(400, 200);
        setLocationRelativeTo(null); // Centraliza a janela
        setLayout(new GridLayout(3, 2, 10, 10)); // Layout em grade

        // Adicionando os componentes na tela
        add(new JLabel("Título do Filme:"));
        add(campoTitulo);
        add(new JLabel("Duração (minutos):"));
        add(campoDuracao);
        add(new JLabel("")); // Espaço vazio para alinhar o botão
        add(botaoSalvar);

        configurarEventos();
    }

    private void configurarEventos() {
        botaoSalvar.addActionListener(evento -> {
            try {
                String titulo = campoTitulo.getText();
                int duracao = Integer.parseInt(campoDuracao.getText());

                // Criando um admin fictício só para passar no método
                Administrador admin = new Administrador(1, "Admin", "admin@astra.com", "123");

                // Chamando o serviço para cadastrar o filme
                servico.cadastrarFilme(admin, titulo, duracao, Filme.classificacaoIndicativa.LIVRE, "Ação", "Diretor X", true, true, "Sinopse teste");

                JOptionPane.showMessageDialog(this, "Filme '" + titulo + "' cadastrado com sucesso!");
                dispose(); // Fecha a tela após salvar

            } catch (Exception ex) {
                // Captura erros (ex: se deixar o título em branco ou digitar letras na duração)
                JOptionPane.showMessageDialog(this, "Erro ao cadastrar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}