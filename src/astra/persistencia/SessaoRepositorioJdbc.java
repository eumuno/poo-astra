package astra.persistencia;

import astra.excecoes.PersistenciaException;
import astra.modelo.Assento;
import astra.modelo.Filme;
import astra.modelo.Sala;
import astra.modelo.Sessao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementação de {@link SessaoRepositorio} com JDBC (SQLite).
 *
 * Uma Sessao do modelo carrega o Filme, a Sala (com os assentos) e o conjunto de
 * assentos ocupados. Aqui monta tudo isso a partir de várias tabelas:
 * sessao + filme + sala + assento + ingresso.
 */
public class SessaoRepositorioJdbc implements SessaoRepositorio {

    /**
     * A tabela sessao tem a coluna preco_base (NOT NULL), mas a classe Sessao ainda não tem
     * esse atributo. Enquanto o modelo não for ajustado, grava o mesmo valor que antes
     * ficava fixo em Ingresso (valorReferencia = 15). Favor, arrumar quando possível.
     */
    static final double PRECO_BASE_PADRAO = 15.0;

    private final ConexaoBanco banco;
    private final FilmeRepositorio filmes;

    public SessaoRepositorioJdbc(ConexaoBanco banco, FilmeRepositorio filmes) {
        if (banco == null || filmes == null) {
            throw new IllegalArgumentException("A conexão e o repositório de filmes são obrigatórios!");
        }
        this.banco = banco;
        this.filmes = filmes;
    }

    @Override
    public Sessao salvar(Sessao sessao) throws PersistenciaException {
        if (sessao == null) {
            throw new IllegalArgumentException("A sessão é obrigatória!");
        }
        if (sessao.getFilme().getId() <= 0) {
            throw new PersistenciaException("Grave o filme antes de criar uma sessão para ele.");
        }

        String sql = "INSERT INTO sessao (filme_id, sala_id, data_hora, status, preco_base) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = banco.abrirConexao()) {
            int salaId = buscarIdDaSala(con, sessao.getSala().getNumero());
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, sessao.getFilme().getId());
                ps.setInt(2, salaId);
                ps.setString(3, sessao.getDataHora().toString()); // ISO-8601
                ps.setString(4, sessao.getStatus().name());       // enum como texto
                ps.setDouble(5, PRECO_BASE_PADRAO);
                ps.executeUpdate();
                try (ResultSet chaves = ps.getGeneratedKeys()) {
                    if (!chaves.next()) {
                        throw new PersistenciaException("O banco não devolveu o id da sessão gravada.");
                    }
                    return new Sessao(chaves.getInt(1), sessao.getFilme(), sessao.getSala(),
                            sessao.getDataHora(), sessao.getStatus());
                }
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new PersistenciaException("A sala " + sessao.getSala().getNumero()
                        + " já tem uma sessão nesse horário.", e);
            }
            throw new PersistenciaException("Erro ao gravar a sessão.", e);
        }
    }

    @Override
    public List<Sessao> listarTodos() throws PersistenciaException {
        List<LinhaSessao> linhas = new ArrayList<>();
        String sql = "SELECT id, filme_id, sala_id, data_hora, status FROM sessao ORDER BY data_hora, id";
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                linhas.add(new LinhaSessao(rs.getInt("id"), rs.getInt("filme_id"), rs.getInt("sala_id"),
                        rs.getString("data_hora"), rs.getString("status")));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar as sessões.", e);
        }

        // a consulta acima já foi fechada; agora monta cada Sessao completa
        List<Sessao> sessoes = new ArrayList<>();
        for (LinhaSessao linha : linhas) {
            sessoes.add(montar(linha));
        }
        return sessoes;
    }

    @Override
    public Optional<Sessao> buscarPorId(int id) throws PersistenciaException {
        String sql = "SELECT id, filme_id, sala_id, data_hora, status FROM sessao WHERE id = ?";
        LinhaSessao linha = null;
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    linha = new LinhaSessao(rs.getInt("id"), rs.getInt("filme_id"), rs.getInt("sala_id"),
                            rs.getString("data_hora"), rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar a sessão " + id + ".", e);
        }
        return linha == null ? Optional.empty() : Optional.of(montar(linha));
    }

    // ------------------------------------------------------------------

    /** Junta filme + sala + assentos ocupados numa Sessao do modelo. */
    private Sessao montar(LinhaSessao linha) throws PersistenciaException {
        Filme filme = filmes.buscarPorId(linha.filmeId).orElseThrow(() ->
                new PersistenciaException("A sessão " + linha.id + " aponta para um filme que não existe."));

        try (Connection con = banco.abrirConexao()) {
            Sala sala = carregarSala(con, linha.salaId);
            Sessao sessao = new Sessao(linha.id, filme, sala, LocalDateTime.parse(linha.dataHora),
                    Sessao.statusSessao.valueOf(linha.status));
            for (int numero : carregarAssentosOcupados(con, linha.id)) {
                sessao.ocuparAssento(numero);
            }
            return sessao;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao carregar os dados da sessão " + linha.id + ".", e);
        }
    }

    private Sala carregarSala(Connection con, int salaId) throws SQLException, PersistenciaException {
        int numeroSala;
        try (PreparedStatement ps = con.prepareStatement("SELECT numero FROM sala WHERE id = ?")) {
            ps.setInt(1, salaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new PersistenciaException("Sala " + salaId + " não existe.");
                }
                numeroSala = rs.getInt("numero");
            }
        }

        List<Assento> assentos = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT numero, tipo FROM assento WHERE sala_id = ? ORDER BY numero")) {
            ps.setInt(1, salaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    assentos.add(new Assento(rs.getInt("numero"), Assento.tipoAssento.valueOf(rs.getString("tipo"))));
                }
            }
        }
        return new Sala(numeroSala, assentos);
    }

    /** Números dos assentos que já têm ingresso vendido nessa sessão. */
    private List<Integer> carregarAssentosOcupados(Connection con, int sessaoId) throws SQLException {
        List<Integer> numeros = new ArrayList<>();
        String sql = "SELECT a.numero FROM ingresso i JOIN assento a ON a.id = i.assento_id "
                + "WHERE i.sessao_id = ? ORDER BY a.numero";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sessaoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    numeros.add(rs.getInt("numero"));
                }
            }
        }
        return numeros;
    }

    /** O modelo Sala só conhece o número; no banco a chave é o id. */
    private int buscarIdDaSala(Connection con, int numeroSala) throws SQLException, PersistenciaException {
        try (PreparedStatement ps = con.prepareStatement("SELECT id FROM sala WHERE numero = ?")) {
            ps.setInt(1, numeroSala);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new PersistenciaException("A sala " + numeroSala + " não está cadastrada no banco.");
                }
                return rs.getInt("id");
            }
        }
    }

    /** Dados crus de uma linha da tabela sessao. */
    private static class LinhaSessao {
        final int id;
        final int filmeId;
        final int salaId;
        final String dataHora;
        final String status;

        LinhaSessao(int id, int filmeId, int salaId, String dataHora, String status) {
            this.id = id;
            this.filmeId = filmeId;
            this.salaId = salaId;
            this.dataHora = dataHora;
            this.status = status;
        }
    }
}
