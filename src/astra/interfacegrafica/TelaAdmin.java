package astra.interfacegrafica;

import astra.servico.CinemaService;
import astra.modelo.Administrador;
import astra.modelo.Filme;
import astra.modelo.Produto;

import javax.swing.*;
import java.awt.*;

public class TelaAdmin extends JFrame {

    private CinemaService servico = new CinemaService();

    public TelaAdmin() {
        setTitle("Área do Administrador - Sistema Astra");
        setSize(500, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // fecha só essa janela, não td

        // CRIA AS ABAS
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Cadastrar Filme", criarPainelFilme());
        abas.addTab("Cadastrar Produto", criarPainelProduto());

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
        JComboBox<Filme.classificacaoIndicativa> comboClassificacao = new JComboBox<>(Filme.classificacaoIndicativa.values());
        JCheckBox checkDublado = new JCheckBox("É dublado?");
        JCheckBox checkAtivo = new JCheckBox("Está ativo?");
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
                Administrador admin = new Administrador(1, "Admin", "admin@astra", "123");

                servico.cadastrarFilme(admin, campoTitulo.getText(), duracao,
                        (Filme.classificacaoIndicativa) comboClassificacao.getSelectedItem(),
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
                Administrador admin = new Administrador(1, "Admin", "admin@astra", "123");

                servico.cadastrarProduto(admin, campoNome.getText(), campoDescricao.getText(),
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
}