package astra.interfacegrafica;

import astra.modelo.*;
import astra.servico.*;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;

/**
 * Tela de entrada do Cinema Astra: escolha do perfil, login e criação de conta de cliente.
 *
 * <p>A tela só lê o que foi digitado e abre a área certa. A conferência de senha e as regras do
 * cadastro estão em {@link ContaService}; a tela não conhece nenhuma classe de persistência.</p>
 */
public class AstraFrame extends JFrame {
    private final ContaService contas;
    private final FilmeService filmes;
    private final ProdutoService produtos;
    private final SessaoService sessoes;
    private final PedidoService pedidos;
    private final CatalogoCliente catalogo;

    /**
     * Monta a tela inicial. Os serviços são entregues às áreas do administrador e do cliente
     * quando o login é aceito.
     *
     * @param contas   login e criação de conta
     * @param filmes   casos de uso de filmes (área do administrador)
     * @param produtos casos de uso de produtos (área do administrador)
     * @param sessoes  casos de uso de sessões (área do administrador)
     * @param pedidos  acompanhamento de pedidos (área do administrador)
     * @param catalogo o que a área do cliente pode fazer
     */
    public AstraFrame(ContaService contas, FilmeService filmes, ProdutoService produtos,
                      SessaoService sessoes, PedidoService pedidos, CatalogoCliente catalogo) {
        this.contas = contas;
        this.filmes = filmes;
        this.produtos = produtos;
        this.sessoes = sessoes;
        this.pedidos = pedidos;
        this.catalogo = catalogo;
        TemaAstra.configurarJanela(this, "Boas-vindas", 1050, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        mostrarInicio();
    }

    /** Tela de entrada com composição clara, sem dados duplicados ou lógica de venda. */
    private void mostrarInicio() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(TemaAstra.FUNDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(0, 0, 22, 0));
        raiz.add(TemaAstra.cabecalho("Bem-vindo ao cinema", "ESCOLHA SEU ACESSO"), BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setBackground(TemaAstra.FUNDO);
        centro.setBorder(BorderFactory.createEmptyBorder(26, 36, 20, 36));
        JPanel mensagem = new JPanel();
        mensagem.setOpaque(false);
        mensagem.setLayout(new BoxLayout(mensagem, BoxLayout.Y_AXIS));
        mensagem.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 40));
        JLabel etiqueta = TemaAstra.destaque("SEU INGRESSO PARA UMA BOA HISTÓRIA");
        etiqueta.setForeground(TemaAstra.SUAVE);
        etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel nome = new JLabel("Cinema ASTRA");
        nome.setFont(new Font("Segoe UI", Font.BOLD, 48));
        nome.setForeground(TemaAstra.TEXTO);
        JLabel frase = new JLabel("O cinema começa aqui.");
        frase.setFont(new Font("Segoe UI", Font.BOLD, 26));
        frase.setForeground(new Color(194, 151, 255));
        JLabel detalhe = new JLabel("<html>Explore as sessões, escolha sua poltrona<br>e aproveite sua experiência no cinema.</html>");
        detalhe.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        detalhe.setForeground(TemaAstra.SUAVE);
        mensagem.add(Box.createVerticalGlue());
        mensagem.add(etiqueta); mensagem.add(Box.createVerticalStrut(20));
        mensagem.add(nome); mensagem.add(Box.createVerticalStrut(13));
        mensagem.add(frase); mensagem.add(Box.createVerticalStrut(23));
        mensagem.add(detalhe); mensagem.add(Box.createVerticalStrut(34));
        mensagem.add(TemaAstra.rotulo("SESSÕES   •   INGRESSOS   •   BOMBONIERE"));
        mensagem.add(Box.createVerticalGlue());

        JPanel acesso = TemaAstra.painel();
        acesso.setLayout(new BoxLayout(acesso, BoxLayout.Y_AXIS));
        JLabel chamada = TemaAstra.titulo("Como deseja entrar?");
        JLabel subtitulo = TemaAstra.rotulo("Escolha o perfil para continuar.");
        chamada.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton cliente = TemaAstra.botao("Continuar como cliente");
        JButton admin = TemaAstra.botaoSecundario("Administrador");
        cliente.setAlignmentX(Component.LEFT_ALIGNMENT);
        admin.setAlignmentX(Component.LEFT_ALIGNMENT);
        cliente.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        admin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        JLabel infoCliente = TemaAstra.rotulo("Consulte filmes, compre ingressos e acompanhe pedidos.");
        JLabel infoAdmin = TemaAstra.rotulo("Gerencie sessões, produtos e pedidos.");
        infoCliente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoCliente.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoAdmin.setAlignmentX(Component.LEFT_ALIGNMENT);
        acesso.add(chamada); acesso.add(Box.createVerticalStrut(10));
        acesso.add(subtitulo); acesso.add(Box.createVerticalStrut(34));
        acesso.add(cliente); acesso.add(Box.createVerticalStrut(8));
        acesso.add(infoCliente); acesso.add(Box.createVerticalStrut(27));
        acesso.add(admin); acesso.add(Box.createVerticalStrut(8));
        acesso.add(infoAdmin);

        GridBagConstraints esq = new GridBagConstraints();
        esq.gridx = 0; esq.gridy = 0; esq.weightx = 1; esq.weighty = 1;
        esq.fill = GridBagConstraints.BOTH;
        esq.insets = new Insets(0, 0, 0, 12);
        centro.add(mensagem, esq);
        GridBagConstraints dir = new GridBagConstraints();
        dir.gridx = 1; dir.gridy = 0; dir.weightx = 1; dir.weighty = 0.6;
        dir.fill = GridBagConstraints.HORIZONTAL;
        dir.insets = new Insets(0, 12, 0, 0);
        centro.add(acesso, dir);

        cliente.addActionListener(e -> mostrarLogin(false));
        admin.addActionListener(e -> mostrarLogin(true));
        raiz.add(centro, BorderLayout.CENTER);
        JLabel rodape = TemaAstra.rotulo("ASTRA  /  UMA EXPERIÊNCIA PARA TODOS");
        rodape.setHorizontalAlignment(SwingConstants.CENTER);
        rodape.setFont(new Font("Segoe UI", Font.BOLD, 12));
        raiz.add(rodape, BorderLayout.SOUTH);
        setContentPane(raiz);
        getRootPane().setDefaultButton(cliente);
        revalidate(); repaint();
    }

    private void mostrarLogin(boolean administrador) {
        JPanel form = TemaAstra.formulario();
        JTextField email = TemaAstra.campo(24);
        JPasswordField senha = TemaAstra.senha(24);
        TemaAstra.linha(form, 0, "E-mail", email);
        TemaAstra.linha(form, 1, "Senha", senha);
        JButton voltar = TemaAstra.botaoSecundario("Voltar");
        JButton entrar = TemaAstra.botao("Entrar");
        TemaAstra.acoes(form, 2, voltar, entrar);
        if (!administrador) {
            JButton cadastro = TemaAstra.botaoSecundario("Criar minha conta");
            TemaAstra.acoes(form, 3, cadastro);
            cadastro.addActionListener(e -> mostrarCadastro());
        }
        voltar.addActionListener(e -> mostrarInicio());
        entrar.addActionListener(e -> autenticar(email.getText().trim(),
                new String(senha.getPassword()), administrador, entrar, email, senha));
        senha.addActionListener(e -> entrar.doClick());
        setContentPane(TemaAstra.paginaFormulario(administrador ? "Acesso do administrador" : "Acesse sua conta", form, 540));
        getRootPane().setDefaultButton(entrar);
        revalidate(); repaint();
        SwingUtilities.invokeLater(email::requestFocusInWindow);
    }

    private void autenticar(String email, String senha, boolean admin, JButton botao,
                            JTextField emailCampo, JPasswordField senhaCampo) {
        botao.setEnabled(false);
        new SwingWorker<Usuario, Void>() {
            @Override protected Usuario doInBackground() throws Exception {
                return contas.autenticar(email, senha).orElse(null);
            }
            @Override protected void done() {
                botao.setEnabled(true);
                try {
                    Usuario usuario = get();
                    if (usuario == null) {
                        TemaAstra.erro(AstraFrame.this,
                                "E-mail ou senha incorretos.");
                        senhaCampo.setText(""); senhaCampo.requestFocusInWindow();
                        return;
                    }
                    if (admin && !(usuario instanceof Administrador) || !admin && !(usuario instanceof Cliente)) {
                        TemaAstra.erro(AstraFrame.this, "A conta existe, mas não pertence ao perfil selecionado.");
                        return;
                    }
                    if (usuario instanceof Administrador a) {
                        new TelaAdmin(a, filmes, produtos, sessoes, pedidos).setVisible(true);
                    } else {
                        new TelaCliente((Cliente) usuario, catalogo).setVisible(true);
                    }
                    // Evita várias janelas idênticas e mantém o caminho de retorno claro.
                    dispose();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    TemaAstra.erro(AstraFrame.this, "A autenticação foi interrompida.");
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause() == null ? ex : ex.getCause();
                    if (causa instanceof IllegalArgumentException) { // campo em branco: o serviço explica
                        TemaAstra.erro(AstraFrame.this, causa.getMessage());
                        (email.isBlank() ? emailCampo : senhaCampo).requestFocusInWindow();
                    } else {
                        TemaAstra.erro(AstraFrame.this, "Falha ao consultar o banco: " + causa.getMessage());
                    }
                }
            }
        }.execute();
    }

    private void mostrarCadastro() {
        JPanel form = TemaAstra.formulario();
        JTextField nome = TemaAstra.campo(24);
        JTextField email = TemaAstra.campo(24);
        JTextField cpf = TemaAstra.campo(24);
        JPasswordField senha = TemaAstra.senha(24);
        TemaAstra.linha(form, 0, "Nome completo", nome);
        TemaAstra.linha(form, 1, "E-mail", email);
        TemaAstra.linha(form, 2, "CPF (11 dígitos)", cpf);
        TemaAstra.linha(form, 3, "Senha (mínimo de 6 caracteres)", senha);
        JButton salvar = TemaAstra.botao("Criar conta");
        JButton voltar = TemaAstra.botaoSecundario("Voltar");
        TemaAstra.acoes(form, 4, voltar, salvar);
        voltar.addActionListener(e -> mostrarLogin(false));
        salvar.addActionListener(e -> {
            String n = nome.getText();
            String em = email.getText();
            String cp = cpf.getText();
            String s = new String(senha.getPassword());
            salvar.setEnabled(false); // as regras do cadastro (e-mail, CPF, senha) são conferidas pelo ContaService
            new SwingWorker<Usuario, Void>() {
                @Override protected Usuario doInBackground() throws Exception {
                    return contas.cadastrarCliente(n, em, cp, s);
                }
                @Override protected void done() {
                    salvar.setEnabled(true);
                    try {
                        get();
                        TemaAstra.sucesso(AstraFrame.this, "Conta criada! Agora entre com seu e-mail e senha.");
                        mostrarLogin(false);
                    } catch (Exception ex) {
                        Throwable causa = ex.getCause() == null ? ex : ex.getCause();
                        TemaAstra.erro(AstraFrame.this, "Não foi possível cadastrar: " + causa.getMessage());
                    }
                }
            }.execute();
        });
        setContentPane(TemaAstra.paginaFormulario("Criar conta de cliente", form, 560));
        revalidate(); repaint();
        SwingUtilities.invokeLater(nome::requestFocusInWindow);
    }
}