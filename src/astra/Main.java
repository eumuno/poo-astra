package astra;

import astra.excecoes.PersistenciaException;
import astra.interfacegrafica.AstraFrame;
import astra.persistencia.*;
import astra.servico.FilmeService;
import astra.servico.ProdutoService;
import astra.servico.SessaoService;
import astra.servico.VendaService;
import astra.servico.pagamento.FormaPagamento;
import astra.servico.pagamento.FormaPagamentoCartao;
import astra.servico.pagamento.FormaPagamentoPix;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.List;

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

        // PARTE 2: monta os repositórios JDBC (o único lugar que conhece as classes concretas)
        FilmeRepositorio filmes = new FilmeRepositorioJdbc(banco);
        ProdutoRepositorio produtos = new ProdutoRepositorioJdbc(banco);
        SessaoRepositorio sessoes = new SessaoRepositorioJdbc(banco, filmes);
        UsuarioRepositorio usuarios = new UsuarioRepositorioJdbc(banco);
        IngressoRepositorio ingressos = new IngressoRepositorioJdbc(banco, sessoes, usuarios);
        PedidoRepositorio pedidos = new PedidoRepositorioJdbc(banco, usuarios, produtos, ingressos);

        // serviços recebem os repositórios pelas interfaces
        List<FormaPagamento> formasPagamento = List.of(new FormaPagamentoPix(), new FormaPagamentoCartao());
        FilmeService filmeService = new FilmeService(filmes);
        ProdutoService produtoService = new ProdutoService(produtos);
        SessaoService sessaoService = new SessaoService(sessoes);
        VendaService vendaService = new VendaService(formasPagamento, pedidos, ingressos);

        // inicia a GUI d forma segura
        SwingUtilities.invokeLater(() -> {
            AstraFrame tela = new AstraFrame(filmeService, produtoService, sessaoService, vendaService, usuarios); // cria janela
            tela.setVisible(true); // dx janela visível
        });

    }
}
