package astra.persistencia;

import astra.excecoes.AssentoIndisponivelException;
import astra.excecoes.EstoqueInsuficienteException;
import astra.excecoes.PersistenciaException;
import astra.modelo.Cliente;
import astra.modelo.Ingresso;
import astra.modelo.ItemPedido;
import astra.modelo.Pagamento;
import astra.modelo.Pedido;
import astra.modelo.Produto;
import astra.modelo.Sessao;
import astra.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementação de {@link PedidoRepositorio} com JDBC (SQLite).
 *
 * Tabelas envolvidas: pedido, item_pedido, ingresso e pagamento.
 * Gravar a venda usa UMA conexão com autoCommit desligado (uma transação): ou as quatro
 * tabelas recebem os dados e o estoque dos produtos é abatido, ou nada disso acontece.
 */
public class PedidoRepositorioJdbc implements PedidoRepositorio {

    private static final String TIPO_CLIENTE = "CLIENTE";

    private final ConexaoBanco banco;
    private final UsuarioRepositorio usuarios;
    private final ProdutoRepositorio produtos;
    private final IngressoRepositorio ingressos;

    public PedidoRepositorioJdbc(ConexaoBanco banco, UsuarioRepositorio usuarios,
                                 ProdutoRepositorio produtos, IngressoRepositorio ingressos) {
        if (banco == null || usuarios == null || produtos == null || ingressos == null) {
            throw new IllegalArgumentException("A conexão e os repositórios de usuário, produto e ingresso são obrigatórios!");
        }
        this.banco = banco;
        this.usuarios = usuarios;
        this.produtos = produtos;
        this.ingressos = ingressos;
    }

    // ==================================================================
    // GRAVAR A VENDA (transação)
    // ==================================================================

    @Override
    public Pedido salvar(Pedido pedido, Pagamento pagamento)
            throws PersistenciaException, AssentoIndisponivelException, EstoqueInsuficienteException {
        if (pedido == null || pagamento == null) {
            throw new IllegalArgumentException("O pedido e o pagamento são obrigatórios!");
        }
        if (pedido.getId() > 0) {
            throw new IllegalArgumentException("Este pedido já foi gravado (id " + pedido.getId() + ").");
        }

        try (Connection con = banco.abrirConexao()) {
            con.setAutoCommit(false); // começa a transação: nada vale até o commit()
            try {
                conferirCliente(con, pedido.getCliente().getId());
                int pedidoId = inserirPedido(con, pedido);

                for (ItemPedido item : pedido.getItens()) {
                    abaterEstoque(con, item);            // 1º: baixa (se faltar estoque, nada é gravado)
                    inserirItem(con, pedidoId, item);    // 2º: registra o item
                }
                List<Ingresso> gravados = new ArrayList<>();
                for (Ingresso ingresso : pedido.getIngressos()) {
                    gravados.add(inserirIngresso(con, pedidoId, ingresso));
                }
                inserirPagamento(con, pedidoId, pagamento);

                con.commit(); // chegou aqui sem erro: confirma tudo de uma vez
                return Pedido.restaurar(pedidoId, pedido.getCliente(), pedido.getData(), pedido.getStatus(),
                        pedido.getItens(), gravados);
            } catch (SQLException | PersistenciaException | AssentoIndisponivelException
                     | EstoqueInsuficienteException | RuntimeException e) {
                con.rollback(); // qualquer erro: desfaz TUDO o que foi inserido acima
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao gravar o pedido.", e);
        }
    }

    /** O pedido só pode pertencer a um CLIENTE que existe (um administrador não compra). */
    private void conferirCliente(Connection con, int clienteId) throws SQLException, PersistenciaException {
        try (PreparedStatement ps = con.prepareStatement("SELECT tipo FROM usuario WHERE id = ?")) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !TIPO_CLIENTE.equals(rs.getString("tipo"))) {
                    throw new PersistenciaException("O usuário " + clienteId
                            + " não é um cliente cadastrado. O pedido não foi gravado.");
                }
            }
        }
    }

    private int inserirPedido(Connection con, Pedido pedido) throws SQLException, PersistenciaException {
        String sql = "INSERT INTO pedido (cliente_id, data, status) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pedido.getCliente().getId());
            ps.setString(2, pedido.getData().toString());   // ISO-8601
            ps.setString(3, pedido.getStatus().name());     // enum como texto
            ps.executeUpdate();
            return lerIdGerado(ps, "pedido");
        }
    }

    /**
     * Abate do estoque a quantidade do item, em um único UPDATE que já confere se há unidades
     * suficientes (WHERE estoque >= ?). Conferir e abater na mesma instrução é o que impede
     * duas janelas de venderem a mesma última unidade: o banco executa uma de cada vez.
     * Se nenhuma linha for atualizada, ou o produto não existe, ou não há estoque suficiente.
     */
    private void abaterEstoque(Connection con, ItemPedido item)
            throws SQLException, PersistenciaException, EstoqueInsuficienteException {
        Produto produto = item.getProduto();
        if (produto.getId() <= 0) {
            throw new PersistenciaException("O produto \"" + produto.getNome() + "\" ainda não foi gravado no banco.");
        }
        String sql = "UPDATE produto SET estoque = estoque - ? WHERE id = ? AND estoque >= ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, item.getQuantidade());
            ps.setInt(2, produto.getId());
            ps.setInt(3, item.getQuantidade());
            if (ps.executeUpdate() > 0) {
                return; // baixa feita
            }
        }

        // nenhuma linha mudou: descobre o motivo para dar uma mensagem útil
        try (PreparedStatement ps = con.prepareStatement("SELECT nome, estoque FROM produto WHERE id = ?")) {
            ps.setInt(1, produto.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new PersistenciaException("O produto \"" + produto.getNome() + "\" não existe no banco.");
                }
                throw new EstoqueInsuficienteException("Estoque insuficiente de \"" + rs.getString("nome")
                        + "\": restam " + rs.getInt("estoque") + " e o pedido pede " + item.getQuantidade() + ".");
            }
        }
    }

    private void inserirItem(Connection con, int pedidoId, ItemPedido item) throws SQLException, PersistenciaException {
        Produto produto = item.getProduto();
        if (produto.getId() <= 0) {
            throw new PersistenciaException("O produto \"" + produto.getNome() + "\" ainda não foi gravado no banco.");
        }
        String sql = "INSERT INTO item_pedido (pedido_id, produto_id, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            ps.setInt(2, produto.getId());
            ps.setInt(3, item.getQuantidade());
            ps.setDouble(4, item.getPrecoUnitario()); // preço da hora da compra
            ps.executeUpdate();
        }
    }

    private Ingresso inserirIngresso(Connection con, int pedidoId, Ingresso ingresso)
            throws SQLException, PersistenciaException, AssentoIndisponivelException {
        Sessao sessao = ingresso.getSessao();
        if (sessao.getId() <= 0) {
            throw new PersistenciaException("A sessão do ingresso ainda não foi gravada no banco.");
        }
        int numeroAssento = ingresso.getAssento().getNumero();
        int assentoId = buscarIdDoAssento(con, sessao.getId(), numeroAssento);

        String sql = "INSERT INTO ingresso (pedido_id, sessao_id, assento_id, tipo, valor_pago) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pedidoId);
            ps.setInt(2, sessao.getId());
            ps.setInt(3, assentoId);
            ps.setString(4, ingresso.getTipoIngresso().name());
            ps.setDouble(5, ingresso.getPrecoIngresso());
            ps.executeUpdate();
            int idGerado = lerIdGerado(ps, "ingresso");
            return new Ingresso(idGerado, ingresso.getFilme(), sessao, ingresso.getAssento(),
                    ingresso.getCliente(), ingresso.getTipoIngresso());
        } catch (SQLException e) {
            // UNIQUE (sessao_id, assento_id): o próprio banco impede vender o mesmo assento duas vezes
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new AssentoIndisponivelException("O assento " + numeroAssento
                        + " já foi vendido para esta sessão.");
            }
            throw e;
        }
    }

    /** O modelo só conhece o número do assento; no banco a chave é o id (sala da sessão + número). */
    private int buscarIdDoAssento(Connection con, int sessaoId, int numeroAssento)
            throws SQLException, PersistenciaException {
        String sql = "SELECT a.id FROM assento a JOIN sessao s ON s.sala_id = a.sala_id "
                + "WHERE s.id = ? AND a.numero = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sessaoId);
            ps.setInt(2, numeroAssento);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new PersistenciaException("O assento " + numeroAssento
                            + " não existe na sala da sessão " + sessaoId + ".");
                }
                return rs.getInt("id");
            }
        }
    }

    private void inserirPagamento(Connection con, int pedidoId, Pagamento pagamento) throws SQLException {
        String sql = "INSERT INTO pagamento (pedido_id, forma, valor, data, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            ps.setString(2, pagamento.getForma().name());   // PIX ou CARTAO
            ps.setDouble(3, pagamento.getValor());
            ps.setString(4, pagamento.getData().toString());
            ps.setString(5, pagamento.getStatus().name());
            ps.executeUpdate();
        }
    }

    private int lerIdGerado(PreparedStatement ps, String tabela) throws SQLException, PersistenciaException {
        try (ResultSet chaves = ps.getGeneratedKeys()) {
            if (!chaves.next()) {
                throw new PersistenciaException("O banco não devolveu o id gerado em " + tabela + ".");
            }
            return chaves.getInt(1);
        }
    }

    // ==================================================================
    // CONSULTAS
    // ==================================================================

    @Override
    public Optional<Pedido> buscarPorId(int id) throws PersistenciaException {
        List<Pedido> encontrados = consultar(
                "SELECT id, cliente_id, data, status FROM pedido WHERE id = ?", id,
                "Erro ao buscar o pedido " + id + ".");
        return encontrados.isEmpty() ? Optional.empty() : Optional.of(encontrados.get(0));
    }

    @Override
    public List<Pedido> listarPorCliente(int clienteId) throws PersistenciaException {
        return consultar("SELECT id, cliente_id, data, status FROM pedido WHERE cliente_id = ? ORDER BY data DESC, id DESC",
                clienteId, "Erro ao listar os pedidos do cliente " + clienteId + ".");
    }

    @Override
    public List<Pedido> listarTodos() throws PersistenciaException {
        return consultar("SELECT id, cliente_id, data, status FROM pedido ORDER BY data DESC, id DESC",
                null, "Erro ao listar os pedidos.");
    }

    @Override
    public void atualizarStatus(int pedidoId, Pedido.StatusPedido novoStatus) throws PersistenciaException {
        if (novoStatus == null) {
            throw new IllegalArgumentException("O novo status é obrigatório!");
        }
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement("UPDATE pedido SET status = ? WHERE id = ?")) {
            ps.setString(1, novoStatus.name());
            ps.setInt(2, pedidoId);
            if (ps.executeUpdate() == 0) {
                throw new PersistenciaException("Não existe pedido com id " + pedidoId + " para atualizar.");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar o status do pedido " + pedidoId + ".", e);
        }
    }

    // ------------------------------------------------------------------

    /** Lê as linhas da tabela pedido (parâmetro opcional) e monta cada Pedido completo. */
    private List<Pedido> consultar(String sql, Integer parametro, String mensagemErro) throws PersistenciaException {
        List<LinhaPedido> linhas = new ArrayList<>();
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    linhas.add(new LinhaPedido(rs.getInt("id"), rs.getInt("cliente_id"),
                            rs.getString("data"), rs.getString("status")));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException(mensagemErro, e);
        }

        // a consulta acima já foi fechada; agora monta cada Pedido (itens + ingressos + cliente)
        Map<Integer, Cliente> clientesCarregados = new HashMap<>();
        List<Pedido> pedidos = new ArrayList<>();
        for (LinhaPedido linha : linhas) {
            Cliente cliente = clientesCarregados.get(linha.clienteId);
            if (cliente == null) {
                cliente = carregarCliente(linha.clienteId);
                clientesCarregados.put(linha.clienteId, cliente);
            }
            pedidos.add(Pedido.restaurar(linha.id, cliente, LocalDateTime.parse(linha.data),
                    Pedido.StatusPedido.valueOf(linha.status),
                    carregarItens(linha.id), ingressos.listarPorPedido(linha.id)));
        }
        return pedidos;
    }

    private List<ItemPedido> carregarItens(int pedidoId) throws PersistenciaException {
        List<LinhaItem> linhas = new ArrayList<>();
        String sql = "SELECT produto_id, quantidade, preco_unitario FROM item_pedido WHERE pedido_id = ? ORDER BY id";
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    linhas.add(new LinhaItem(rs.getInt("produto_id"), rs.getInt("quantidade"),
                            rs.getDouble("preco_unitario")));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao carregar os itens do pedido " + pedidoId + ".", e);
        }

        List<ItemPedido> itens = new ArrayList<>();
        for (LinhaItem linha : linhas) {
            Produto produto = produtos.buscarPorId(linha.produtoId).orElseThrow(() ->
                    new PersistenciaException("Um item do pedido " + pedidoId + " aponta para um produto que não existe."));
            // construtor de 3 parâmetros: mantém o preço da hora da compra, e não o preço de hoje
            itens.add(new ItemPedido(produto, linha.quantidade, linha.precoUnitario));
        }
        return itens;
    }

    private Cliente carregarCliente(int clienteId) throws PersistenciaException {
        Usuario usuario = usuarios.buscarPorId(clienteId).orElseThrow(() ->
                new PersistenciaException("O cliente " + clienteId + " não existe."));
        if (!(usuario instanceof Cliente)) {
            throw new PersistenciaException("O usuário " + clienteId + " não é um cliente.");
        }
        return (Cliente) usuario;
    }

    /** Dados crus de uma linha da tabela pedido. */
    private static class LinhaPedido {
        final int id;
        final int clienteId;
        final String data;
        final String status;

        LinhaPedido(int id, int clienteId, String data, String status) {
            this.id = id;
            this.clienteId = clienteId;
            this.data = data;
            this.status = status;
        }
    }

    /** Dados crus de uma linha da tabela item_pedido. */
    private static class LinhaItem {
        final int produtoId;
        final int quantidade;
        final double precoUnitario;

        LinhaItem(int produtoId, int quantidade, double precoUnitario) {
            this.produtoId = produtoId;
            this.quantidade = quantidade;
            this.precoUnitario = precoUnitario;
        }
    }
}