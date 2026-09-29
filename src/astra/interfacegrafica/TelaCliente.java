package astra.interfacegrafica;

import astra.excecoes.AssentoIndisponivelException;
import astra.modelo.Assento;
import astra.servico.CinemaService;
import javax.swing.*;
import java.awt.*;

public class TelaCliente extends JFrame {

    private CinemaService servico = new CinemaService();
    private JButton botaoComprar = new JButton("Comprar Assento (Teste Erro)");

    public TelaCliente() {
        setTitle("Área do Cliente - Comprar Ingresso");
        setSize(400, 150);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        add(new JLabel("Clique abaixo para simular a compra de um assento ocupado:"));
        add(botaoComprar);

        configurarEventos();
    }

    private void configurarEventos() {
        botaoComprar.addActionListener(evento -> {
            try {
                // Criando um assento e forçando ele a estar OCUPADO para testar a regra de negócio
                Assento assentoTeste = new Assento(1, Assento.tipoAssento.COMUM);
                assentoTeste.ocupar(); // Já está ocupado!

                // O CinemaService vai tentar vender e vai lançar a sua AssentoIndisponivelException
                servico.venderIngresso(null, null, assentoTeste, null);

                JOptionPane.showMessageDialog(this, "Compra realizada com sucesso!");

            } catch (AssentoIndisponivelException ex) {
                // AQUI GARANTIMOS O PONTO: A interface captura o erro do domínio e mostra na tela!
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Assento Indisponível", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro genérico: " + ex.getMessage());
            }
        });
    }
}