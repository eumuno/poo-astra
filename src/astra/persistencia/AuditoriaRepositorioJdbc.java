package astra.persistencia;

import astra.excecoes.PersistenciaException;
import astra.modelo.RegistroDeAuditoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação de {@link AuditoriaRepositorio} com JDBC (SQLite). Todos os comandos usam parâmetros
 * ({@code PreparedStatement}): o texto da ação e do detalhe nunca é montado dentro do SQL.
 */
public class AuditoriaRepositorioJdbc implements AuditoriaRepositorio {

    private static final int LIMITE_MAXIMO = 1000;

    private final ConexaoBanco banco;

    /**
     * Cria o repositório sobre a conexão recebida.
     *
     * @param banco de onde as conexões são abertas
     * @throws IllegalArgumentException se a conexão for nula
     */
    public AuditoriaRepositorioJdbc(ConexaoBanco banco) {
        if (banco == null) {
            throw new IllegalArgumentException("A conexão com o banco é obrigatória!");
        }
        this.banco = banco;
    }

    @Override
    public void registrar(Integer usuarioId, String acao, String detalhe) throws PersistenciaException {
        String sql = "INSERT INTO auditoria (quando, usuario_id, acao, detalhe) VALUES (?, ?, ?, ?)";
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, LocalDateTime.now().toString());
            if (usuarioId == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, usuarioId);
            }
            ps.setString(3, acao);
            ps.setString(4, detalhe);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao gravar o registro de auditoria.", e);
        }
    }

    @Override
    public List<RegistroDeAuditoria> listarRecentes(int limite) throws PersistenciaException {
        if (limite < 1 || limite > LIMITE_MAXIMO) {
            throw new IllegalArgumentException("O limite deve estar entre 1 e " + LIMITE_MAXIMO + ".");
        }
        String sql = "SELECT id, quando, usuario_id, acao, detalhe FROM auditoria ORDER BY id DESC LIMIT ?";
        List<RegistroDeAuditoria> registros = new ArrayList<>();
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int usuario = rs.getInt("usuario_id");
                    Integer usuarioId = rs.wasNull() ? null : usuario;
                    registros.add(new RegistroDeAuditoria(rs.getInt("id"), LocalDateTime.parse(rs.getString("quando")),
                            usuarioId, rs.getString("acao"), rs.getString("detalhe")));
                }
            }
            return registros;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar a trilha de auditoria.", e);
        }
    }
}