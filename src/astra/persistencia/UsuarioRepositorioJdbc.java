package astra.persistencia;

import astra.excecoes.PersistenciaException;
import astra.modelo.Administrador;
import astra.modelo.Cliente;
import astra.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Implementação de {@link UsuarioRepositorio} com JDBC (SQLite).
 *
 * Na tabela usuario, Cliente e Administrador ficam juntos, separados pela coluna "tipo"
 * (herança "tabela única"). O CPF só é preenchido para CLIENTE.
 */
public class UsuarioRepositorioJdbc implements UsuarioRepositorio {

    private static final String TIPO_CLIENTE = "CLIENTE";
    private static final String TIPO_ADMINISTRADOR = "ADMINISTRADOR";
    private static final String COLUNAS = "id, tipo, nome, email, senha_hash, cpf";

    private final ConexaoBanco banco;

    public UsuarioRepositorioJdbc(ConexaoBanco banco) {
        if (banco == null) {
            throw new IllegalArgumentException("A conexão com o banco é obrigatória!");
        }
        this.banco = banco;
    }

    @Override
    public Usuario salvar(Usuario usuario, String senhaPura) throws PersistenciaException {
        if (usuario == null) {
            throw new IllegalArgumentException("O usuário é obrigatório!");
        }
        if (usuario.getId() > 0) {
            throw new IllegalArgumentException("Só é possível cadastrar usuário novo (id 0).");
        }
        if (senhaPura == null || senhaPura.isBlank()) {
            throw new IllegalArgumentException("A senha é obrigatória!");
        }

        String tipo;
        String cpf = null;
        if (usuario instanceof Cliente) {
            tipo = TIPO_CLIENTE;
            cpf = ((Cliente) usuario).getCpf();
        } else if (usuario instanceof Administrador) {
            tipo = TIPO_ADMINISTRADOR;
        } else {
            throw new IllegalArgumentException("Tipo de usuário desconhecido: " + usuario.getClass().getSimpleName());
        }

        String hash = CriptografiaSenha.gerarHash(senhaPura); // nunca grava a senha pura
        String sql = "INSERT INTO usuario (tipo, nome, email, senha_hash, cpf) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tipo);
            ps.setString(2, usuario.getNome());
            ps.setString(3, usuario.getEmail());
            ps.setString(4, hash);
            ps.setString(5, cpf);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new PersistenciaException("O banco não devolveu o id do usuário gravado.");
                }
                return montar(chaves.getInt(1), tipo, usuario.getNome(), usuario.getEmail(), hash, cpf);
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new PersistenciaException("Já existe um usuário com o e-mail " + usuario.getEmail() + ".", e);
            }
            throw new PersistenciaException("Erro ao gravar o usuário " + usuario.getEmail() + ".", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) throws PersistenciaException {
        String sql = "SELECT " + COLUNAS + " FROM usuario WHERE id = ?";
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar o usuário " + id + ".", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) throws PersistenciaException {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        // lower() nos dois lados: "Cliente@Email.com" e "cliente@email.com" são o mesmo e-mail
        String sql = "SELECT " + COLUNAS + " FROM usuario WHERE lower(email) = lower(?)";
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar o usuário " + email + ".", e);
        }
    }

    @Override
    public Optional<Usuario> autenticar(String email, String senhaPura) throws PersistenciaException {
        if (email == null || senhaPura == null) {
            return Optional.empty();
        }
        String sql = "SELECT " + COLUNAS + " FROM usuario WHERE lower(email) = lower(?)";
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && CriptografiaSenha.confere(senhaPura, rs.getString("senha_hash"))) {
                    return Optional.of(mapear(rs));
                }
                return Optional.empty(); // e-mail inexistente OU senha errada: não diz qual
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao autenticar o usuário.", e);
        }
    }

    // ------------------------------------------------------------------

    private Usuario mapear(ResultSet rs) throws SQLException {
        return montar(rs.getInt("id"), rs.getString("tipo"), rs.getString("nome"),
                rs.getString("email"), rs.getString("senha_hash"), rs.getString("cpf"));
    }

    /**
     * Cria o Cliente ou o Administrador conforme o tipo gravado. O campo "senha" do objeto
     * recebe o HASH vindo do banco (a senha pura não existe mais depois do cadastro).
     */
    private Usuario montar(int id, String tipo, String nome, String email, String hash, String cpf) {
        if (TIPO_CLIENTE.equals(tipo)) {
            return new Cliente(id, nome, email, hash, cpf);
        }
        return new Administrador(id, nome, email, hash);
    }
}