package astra.interfacegrafica;

import astra.excecoes.PersistenciaException;
import astra.modelo.Administrador;
import astra.modelo.Cliente;
import astra.modelo.Usuario;
import astra.persistencia.UsuarioRepositorio;
import astra.servico.FilmeService;
import astra.servico.ProdutoService;
import astra.servico.SessaoService;
import astra.servico.VendaService;
import javax.swing.*;
import java.awt.*;

public class AstraFrame extends JFrame {

    // SERVIÇOS MONTADOS NO Main (com os repositórios JDBC)
    private final FilmeService filmeService;
    private final ProdutoService produtoService;
    private final SessaoService sessaoService;
    private final VendaService vendaService;
    private final UsuarioRepositorio usuarios;

    // COMPONENTES DA TELA
    private final JLabel textoBoasVindas = new JLabel("Bem-vindo(a) ao Sistema do Astra", SwingConstants.CENTER);
    private final JButton botaoCliente =  new JButton("Entrar como Cliente");
    private final JButton botaoAdmin = new JButton("Entrar como Administrador");

    // CONSTRUTOR DA JANELA
    public AstraFrame(FilmeService filmeService, ProdutoService produtoService, SessaoService sessaoService,
                      VendaService vendaService, UsuarioRepositorio usuarios) {
        this.filmeService = filmeService;
        this.produtoService = produtoService;
        this.sessaoService = sessaoService;
        this.vendaService = vendaService;
        this.usuarios = usuarios;
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
            Usuario usuario = buscarUsuario("cliente@email.com");
            if (usuario instanceof Cliente cliente) {
                new TelaCliente(cliente, produtoService, sessaoService, vendaService).setVisible(true); // abre tela do cliente
            } else if (usuario != null) {
                mostrarErro("O cliente de demonstração não foi encontrado no banco.");
            }
        });

        // evento do botao admin
        botaoAdmin.addActionListener(evento -> {
            Usuario usuario = buscarUsuario("admin@astra");
            if (usuario instanceof Administrador admin) {
                new TelaAdmin(admin, filmeService, produtoService, sessaoService).setVisible(true);
            } else if (usuario != null) {
                mostrarErro("O administrador de demonstração não foi encontrado no banco.");
            }
        });
    }

    // BUSCA O USUÁRIO DO SEED NO BANCO (ainda não há tela de login)
    private Usuario buscarUsuario(String email) {
        try {
            return usuarios.buscarPorEmail(email).orElse(null);
        } catch (PersistenciaException e) {
            mostrarErro(e.getMessage());
            return null;
        }
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

}
