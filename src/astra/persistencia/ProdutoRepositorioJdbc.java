package astra.persistencia;

import astra.excecoes.PersistenciaException;
import astra.modelo.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implementação de {@link ProdutoRepositorio} com JDBC (SQLite). */
public class ProdutoRepositorioJdbc implements ProdutoRepositorio {

    private static final String COLUNAS = "id, nome, descricao, preco, estoque, categoria";

    private final ConexaoBanco banco;

    /**
     * Cria o repositório com as dependências recebidas.
     *
     * @param banco banco de onde as conexões são abertas
     * @throws IllegalArgumentException se alguma dependência for nula
     */
    public ProdutoRepositorioJdbc(ConexaoBanco banco) {
        if (banco == null) {
            throw new IllegalArgumentException("A conexão com o banco é obrigatória!");
        }
        this.banco = banco;
    }

    @Override
    public Produto salvar(Produto produto) throws PersistenciaException {
        if (produto == null) {
            throw new IllegalArgumentException("O produto é obrigatório!");
        }
        return produto.getId() > 0 ? atualizar(produto) : inserir(produto);
    }

    private Produto inserir(Produto produto) throws PersistenciaException {
        String sql = "INSERT INTO produto (nome, descricao, preco, estoque, categoria) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherDados(ps, produto);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new PersistenciaException("O banco não devolveu o id do produto gravado.");
                }
                return new Produto(chaves.getInt(1), produto.getNome(), produto.getDescricao(),
                        produto.getPreco(), produto.getEstoque(), produto.getCategoria());
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao gravar o produto \"" + produto.getNome() + "\".", e);
        }
    }

    private Produto atualizar(Produto produto) throws PersistenciaException {
        String sql = "UPDATE produto SET nome = ?, descricao = ?, preco = ?, estoque = ?, categoria = ? WHERE id = ?";
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            preencherDados(ps, produto);
            ps.setInt(6, produto.getId());
            if (ps.executeUpdate() == 0) {
                throw new PersistenciaException("Não existe produto com id " + produto.getId() + " para atualizar.");
            }
            return produto;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar o produto \"" + produto.getNome() + "\".", e);
        }
    }

    @Override
    public List<Produto> listarTodos() throws PersistenciaException {
        String sql = "SELECT " + COLUNAS + " FROM produto ORDER BY id";
        List<Produto> produtos = new ArrayList<>();
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                produtos.add(mapear(rs));
            }
            return produtos;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar os produtos.", e);
        }
    }

    @Override
    public Optional<Produto> buscarPorId(int id) throws PersistenciaException {
        String sql = "SELECT " + COLUNAS + " FROM produto WHERE id = ?";
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar o produto " + id + ".", e);
        }
    }

    @Override
    public Produto atualizarDados(Produto produto) throws PersistenciaException {
        if (produto == null || produto.getId() <= 0) {
            throw new IllegalArgumentException("Só é possível atualizar um produto já gravado.");
        }
        String sql = "UPDATE produto SET nome = ?, descricao = ?, preco = ?, categoria = ? WHERE id = ?";
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, produto.getNome());
            ps.setString(2, produto.getDescricao());
            ps.setDouble(3, produto.getPreco());
            ps.setString(4, produto.getCategoria().name());
            ps.setInt(5, produto.getId());
            if (ps.executeUpdate() == 0) {
                throw new PersistenciaException("Não existe produto com id " + produto.getId() + " para atualizar.");
            }
            return lerProduto(con, produto.getId());
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar o produto \"" + produto.getNome() + "\".", e);
        }
    }

    @Override
    public Produto adicionarEstoque(int produtoId, int quantidade) throws PersistenciaException {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade a adicionar deve ser maior que zero.");
        }
        String sql = "UPDATE produto SET estoque = estoque + ? WHERE id = ?";
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quantidade);
            ps.setInt(2, produtoId);
            if (ps.executeUpdate() == 0) {
                throw new PersistenciaException("Não existe produto com id " + produtoId + " para repor o estoque.");
            }
            return lerProduto(con, produtoId);
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao repor o estoque do produto " + produtoId + ".", e);
        }
    }

    // ------------------------------------------------------------------

    /** Lê o produto na conexão já aberta (usado para devolver o estado final depois de um UPDATE). */
    private Produto lerProduto(Connection con, int id) throws SQLException, PersistenciaException {
        try (PreparedStatement ps = con.prepareStatement("SELECT " + COLUNAS + " FROM produto WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new PersistenciaException("Não existe produto com id " + id + ".");
                }
                return mapear(rs);
            }
        }
    }

    private void preencherDados(PreparedStatement ps, Produto produto) throws SQLException {
        ps.setString(1, produto.getNome());
        ps.setString(2, produto.getDescricao());
        ps.setDouble(3, produto.getPreco());
        ps.setInt(4, produto.getEstoque());
        ps.setString(5, produto.getCategoria().name()); // enum gravado como texto
    }

    private Produto mapear(ResultSet rs) throws SQLException {
        return new Produto(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("descricao"),
                rs.getDouble("preco"),
                rs.getInt("estoque"),
                Produto.CategoriaProduto.valueOf(rs.getString("categoria")));
    }
}