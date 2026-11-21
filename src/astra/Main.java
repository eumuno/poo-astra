package astra;

import astra.excecoes.PersistenciaException;
import astra.interfacegrafica.AstraFrame;
import astra.persistencia.*;
import astra.servico.AtendimentoCliente;
import astra.servico.CatalogoCliente;
import astra.servico.ContaService;
import astra.servico.ControleDeAcesso;
import astra.servico.FilmeService;
import astra.servico.PedidoService;
import astra.servico.ProdutoService;
import astra.servico.SessaoService;
import astra.servico.TrilhaDeAuditoria;
import astra.servico.VendaService;
import astra.servico.pagamento.FormaPagamento;
import astra.servico.pagamento.FormaPagamentoCartao;
import astra.servico.pagamento.FormaPagamentoPix;
import astra.interfacegrafica.TemaAstra;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.List;

/**
 * Ponto de entrada do Cinema Astra e raiz de composição: é o único lugar que conhece as classes
 * concretas de persistência (JDBC) e as entrega, pelas interfaces, aos serviços e à interface gráfica.
 */
public class Main {

    private Main() { }

    /**
     * Cria o banco (se ainda não existir), monta repositórios, serviços e fachada do cliente e abre a tela inicial.
     * Se o banco não puder ser aberto, mostra o motivo e encerra.
     *
     * @param args não usado
     */
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
        AuditoriaRepositorio auditoria = new AuditoriaRepositorioJdbc(banco);

        // segurança: quem confere se o usuário é mesmo administrador, e a trilha de quem fez o quê
        ControleDeAcesso acesso = new ControleDeAcesso(usuarios);
        TrilhaDeAuditoria trilha = new TrilhaDeAuditoria(auditoria, acesso);

        // serviços recebem os repositórios pelas interfaces
        List<FormaPagamento> formasPagamento = List.of(new FormaPagamentoPix(), new FormaPagamentoCartao());
        FilmeService filmeService = new FilmeService(filmes, acesso, trilha);
        ProdutoService produtoService = new ProdutoService(produtos, acesso, trilha);
        SessaoService sessaoService = new SessaoService(sessoes, acesso, trilha);
        VendaService vendaService = new VendaService(formasPagamento, pedidos, ingressos, produtos, trilha);
        ContaService contaService = new ContaService(usuarios, trilha);
        PedidoService pedidoService = new PedidoService(pedidos, acesso, trilha);
        // a área do cliente só enxerga o que um cliente pode fazer (a interface CatalogoCliente)
        CatalogoCliente catalogo = new AtendimentoCliente(sessaoService, produtoService, vendaService, pedidoService);

        // inicia a GUI d forma segura
        SwingUtilities.invokeLater(() -> {
            TemaAstra.instalar();
            AstraFrame tela = new AstraFrame(contaService, filmeService, produtoService, sessaoService, pedidoService, trilha, catalogo); // cria janela

            tela.setVisible(true); // dx janela visível
        });

    }
}