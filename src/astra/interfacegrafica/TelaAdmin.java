package astra.interfacegrafica;

import astra.modelo.Administrador;
import astra.modelo.Filme;
import astra.modelo.Produto;
import astra.servico.FilmeService;
import astra.servico.ProdutoService;
import astra.servico.SessaoService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TelaAdmin extends JFrame {

    private final Administrador admin;
    private final FilmeService filmeService;
    private final ProdutoService produtoService;
    private final SessaoService sessaoService;

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // COMBO D FILMES DA ABA SESSÃO (recarregado sempre q a aba é aberta)
    private final JComboBox<Filme> comboFilmesSessao = new JComboBox<>();

    public TelaAdmin(Administrador admin, FilmeService filmeService, ProdutoService produtoService, SessaoService sessaoService) {
        this.admin = admin;
        this.filmeService = filmeService;
        this.produtoService = produtoService;
        this.sessaoService = sessaoService;

        setTitle("Área do Administrador - Sistema Astra");
        setSize(500, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // fecha só essa janela, não td

        // CRIA AS ABAS
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Cadastrar Filme", criarPainelFilme());
        abas.addTab("Cadastrar Produto", criarPainelProduto());
        abas.addTab("Cadastrar Sessão", criarPainelSessao());

        // AO ABRIR A ABA D SESSÃO, BUSCA OS FILMES D NOVO (inclui os recém-cadastrados)
        abas.addChangeListener(e -> {
            if (abas.getSelectedIndex() == 2) {
                carregarFilmes();
            }
        });

        add(abas);
    }

    // ABA FILMES
    private JPanel criarPainelFilme() {
        JPanel painel = new JPanel(new GridLayout(10, 2, 5, 5));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // CAMPOS D PREENCHIMENTO DO FILME
        JTextField campoTitulo = new JTextField();
        JTextField campoDuracao = new JTextField();
        JTextField campoGenero = new JTextField();
        JTextField campoDiretor = new JTextField();
        JTextField campoSinopse = new JTextField();
        JComboBox<Filme.ClassificacaoIndicativa> comboClassificacao = new JComboBox<>(Filme.ClassificacaoIndicativa.values());
        JCheckBox checkDublado = new JCheckBox("É dublado?");
        JCheckBox checkAtivo = new JCheckBox("Está ativo?", true);
        JButton botaoSalvar = new JButton("Salvar Filme");

        // ADICIONA NA TELA
        painel.add(new JLabel("Título:")); painel.add(campoTitulo);
        painel.add(new JLabel("Duração (minutos):")); painel.add(campoDuracao);
        painel.add(new JLabel("Gênero:")); painel.add(campoGenero);
        painel.add(new JLabel("Diretor:")); painel.add(campoDiretor);
        painel.add(new JLabel("Classificação:")); painel.add(comboClassificacao);
        painel.add(new JLabel("Sinopse:")); painel.add(campoSinopse);
        painel.add(new JLabel("Opções:")); painel.add(checkDublado);
        painel.add(new JLabel("")); painel.add(checkAtivo);
        painel.add(new JLabel("")); painel.add(botaoSalvar);

        // AÇÃO DO BOTÃO SALVAR FILME
        botaoSalvar.addActionListener(e -> {
            try {
                int duracao = Integer.parseInt(campoDuracao.getText());
                filmeService.cadastrarFilme(admin, campoTitulo.getText(), duracao,
                        (Filme.ClassificacaoIndicativa) comboClassificacao.getSelectedItem(),
                        campoGenero.getText(), campoDiretor.getText(),
                        checkAtivo.isSelected(), checkDublado.isSelected(), campoSinopse.getText());

                JOptionPane.showMessageDialog(this, "Filme cadastrado com sucesso!");
                campoTitulo.setText(""); // limpa campo
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "A duração deve ser um número válido!", "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        return painel;
    }

    // ABA PRODUTOS
    private JPanel criarPainelProduto() {
        JPanel painel = new JPanel(new GridLayout(7, 2, 5, 5));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // CAMPOS D PREENCHIMENTO DOS PRODUTO
        JTextField campoNome = new JTextField();
        JTextField campoDescricao = new JTextField();
        JTextField campoPreco = new JTextField();
        JTextField campoEstoque = new JTextField();
        JComboBox<Produto.CategoriaProduto> comboCategoria = new JComboBox<>(Produto.CategoriaProduto.values());
        JButton botaoSalvar = new JButton("Salvar Produto");

        // ADICIONA NA TELA
        painel.add(new JLabel("Nome do Produto:")); painel.add(campoNome);
        painel.add(new JLabel("Descrição:")); painel.add(campoDescricao);
        painel.add(new JLabel("Preço (R$):")); painel.add(campoPreco);
        painel.add(new JLabel("Quantidade em Estoque:")); painel.add(campoEstoque);
        painel.add(new JLabel("Categoria:")); painel.add(comboCategoria);
        painel.add(new JLabel("")); painel.add(botaoSalvar);

        // AÇÃO DO BOTÃO SALVAR PRODUTO
        botaoSalvar.addActionListener(e -> {
            try {
                double preco = Double.parseDouble(campoPreco.getText().replace(",", "."));
                int estoque = Integer.parseInt(campoEstoque.getText());
                produtoService.cadastrarProduto(admin, campoNome.getText(), campoDescricao.getText(),
                        preco, estoque, (Produto.CategoriaProduto) comboCategoria.getSelectedItem());

                JOptionPane.showMessageDialog(this, "Produto cadastrado com sucesso!");
                campoNome.setText(""); // limpa campo
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Preço ou estoque contêm números inválidos!", "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        return painel;
    }

    // ABA SESSÕES: SEM SESSÃO O FILME NÃO APARECE P O CLIENTE COMPRAR
    private JPanel criarPainelSessao() {
        JPanel painel = new JPanel(new GridLayout(6, 2, 5, 5));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField campoSala = new JTextField("1");
        JTextField campoDataHora = new JTextField(LocalDateTime.now().plusDays(1).withHour(21).withMinute(0).format(FORMATO_DATA));
        JTextField campoPreco = new JTextField("15.0");
        JButton botaoSalvar = new JButton("Salvar Sessão");

        painel.add(new JLabel("Filme:")); painel.add(comboFilmesSessao);
        painel.add(new JLabel("Número da sala:")); painel.add(campoSala);
        painel.add(new JLabel("Data e hora (dd/MM/aaaa HH:mm):")); painel.add(campoDataHora);
        painel.add(new JLabel("Preço base (R$):")); painel.add(campoPreco);
        painel.add(new JLabel("")); painel.add(botaoSalvar);

        botaoSalvar.addActionListener(e -> {
            try {
                int sala = Integer.parseInt(campoSala.getText().trim());
                LocalDateTime dataHora = LocalDateTime.parse(campoDataHora.getText().trim(), FORMATO_DATA);
                double preco = Double.parseDouble(campoPreco.getText().trim().replace(",", "."));

                sessaoService.cadastrarSessao(admin, (Filme) comboFilmesSessao.getSelectedItem(), sala, dataHora, preco);
                JOptionPane.showMessageDialog(this, "Sessão cadastrada com sucesso!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Sala ou preço contêm números inválidos!", "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Use o formato dd/MM/aaaa HH:mm na data.", "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        return painel;
    }

    private void carregarFilmes() {
        comboFilmesSessao.removeAllItems();
        try {
            for (Filme f : filmeService.listarFilmes()) {
                if (f.isAtivo()) {
                    comboFilmesSessao.addItem(f);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
