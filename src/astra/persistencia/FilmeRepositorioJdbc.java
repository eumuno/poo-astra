package astra.persistencia;

import astra.excecoes.PersistenciaException;
import astra.modelo.Filme;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implementação de {@link FilmeRepositorio} com JDBC (SQLite). */
public class FilmeRepositorioJdbc implements FilmeRepositorio {

    private static final String COLUNAS =
            "id, titulo, duracao_minutos, classificacao, genero, diretor, ativo, dublado, sinopse";

    private final ConexaoBanco banco;

    public FilmeRepositorioJdbc(ConexaoBanco banco) {
        if (banco == null) {
            throw new IllegalArgumentException("A conexão com o banco é obrigatória!");
        }
        this.banco = banco;
    }

    @Override
    public Filme salvar(Filme filme) throws PersistenciaException {
        if (filme == null) {
            throw new IllegalArgumentException("O filme é obrigatório!");
        }
        return filme.getId() > 0 ? atualizar(filme) : inserir(filme);
    }

    private Filme inserir(Filme filme) throws PersistenciaException {
        String sql = "INSERT INTO filme (titulo, duracao_minutos, classificacao, genero, diretor, ativo, dublado, sinopse) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherDados(ps, filme);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new PersistenciaException("O banco não devolveu o id do filme gravado.");
                }
                return copiarComId(filme, chaves.getInt(1));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao gravar o filme \"" + filme.getTitulo() + "\".", e);
        }
    }

    private Filme atualizar(Filme filme) throws PersistenciaException {
        String sql = "UPDATE filme SET titulo = ?, duracao_minutos = ?, classificacao = ?, genero = ?, "
                + "diretor = ?, ativo = ?, dublado = ?, sinopse = ? WHERE id = ?";
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            preencherDados(ps, filme);
            ps.setInt(9, filme.getId());
            if (ps.executeUpdate() == 0) {
                throw new PersistenciaException("Não existe filme com id " + filme.getId() + " para atualizar.");
            }
            return filme;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar o filme \"" + filme.getTitulo() + "\".", e);
        }
    }

    @Override
    public List<Filme> listarTodos() throws PersistenciaException {
        String sql = "SELECT " + COLUNAS + " FROM filme ORDER BY id";
        List<Filme> filmes = new ArrayList<>();
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                filmes.add(mapear(rs));
            }
            return filmes;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar os filmes.", e);
        }
    }

    @Override
    public Optional<Filme> buscarPorId(int id) throws PersistenciaException {
        String sql = "SELECT " + COLUNAS + " FROM filme WHERE id = ?";
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar o filme " + id + ".", e);
        }
    }

    // ------------------------------------------------------------------

    private void preencherDados(PreparedStatement ps, Filme filme) throws SQLException {
        ps.setString(1, filme.getTitulo());
        ps.setInt(2, filme.getDuracaoMinutos());
        ps.setString(3, filme.getClassificacaoIndicativa().name()); // enum gravado como texto
        ps.setString(4, filme.getGenero());
        ps.setString(5, filme.getDiretor());
        ps.setInt(6, filme.isAtivo() ? 1 : 0);                        // boolean gravado como 0/1
        ps.setInt(7, filme.isDublado() ? 1 : 0);
        ps.setString(8, filme.getSinopse());
    }

    private Filme mapear(ResultSet rs) throws SQLException {
        return new Filme(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getInt("duracao_minutos"),
                Filme.classificacaoIndicativa.valueOf(rs.getString("classificacao")),
                rs.getString("genero"),
                rs.getString("diretor"),
                rs.getInt("ativo") == 1,
                rs.getInt("dublado") == 1,
                rs.getString("sinopse"));
    }

    /** O Filme não tem setId, então devolve uma cópia com o id gerado pelo banco. */
    private Filme copiarComId(Filme f, int id) {
        return new Filme(id, f.getTitulo(), f.getDuracaoMinutos(), f.getClassificacaoIndicativa(),
                f.getGenero(), f.getDiretor(), f.isAtivo(), f.isDublado(), f.getSinopse());
    }
}
