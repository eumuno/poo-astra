package astra.persistencia;

import astra.excecoes.PersistenciaException;
import astra.modelo.Assento;
import astra.modelo.Cliente;
import astra.modelo.Ingresso;
import astra.modelo.Sessao;
import astra.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação de {@link IngressoRepositorio} com JDBC (SQLite): só consultas.
 * A gravação dos ingressos acontece dentro de PedidoRepositorioJdbc.salvar().
 */
public class IngressoRepositorioJdbc implements IngressoRepositorio {

    private final ConexaoBanco banco;
    private final SessaoRepositorio sessoes;
    private final UsuarioRepositorio usuarios;

    public IngressoRepositorioJdbc(ConexaoBanco banco, SessaoRepositorio sessoes, UsuarioRepositorio usuarios) {
        if (banco == null || sessoes == null || usuarios == null) {
            throw new IllegalArgumentException("A conexão e os repositórios de sessão e de usuário são obrigatórios!");
        }
        this.banco = banco;
        this.sessoes = sessoes;
        this.usuarios = usuarios;
    }

    @Override
    public boolean assentoEstaOcupado(int sessaoId, int numeroAssento) throws PersistenciaException {
        String sql = "SELECT COUNT(*) FROM ingresso i JOIN assento a ON a.id = i.assento_id "
                + "WHERE i.sessao_id = ? AND a.numero = ?";
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sessaoId);
            ps.setInt(2, numeroAssento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao verificar o assento " + numeroAssento
                    + " da sessão " + sessaoId + ".", e);
        }
    }

    @Override
    public List<Ingresso> listarPorPedido(int pedidoId) throws PersistenciaException {
        // o ingresso não tem cliente_id: o cliente é o dono do pedido (por isso o JOIN com pedido)
        String sql = "SELECT i.id, i.sessao_id, a.numero AS numero_assento, i.tipo, p.cliente_id "
                + "FROM ingresso i "
                + "JOIN assento a ON a.id = i.assento_id "
                + "JOIN pedido p ON p.id = i.pedido_id "
                + "WHERE i.pedido_id = ? ORDER BY i.id";

        List<LinhaIngresso> linhas = new ArrayList<>();
        try (Connection con = banco.abrirConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    linhas.add(new LinhaIngresso(rs.getInt("id"), rs.getInt("sessao_id"),
                            rs.getInt("numero_assento"), rs.getString("tipo"), rs.getInt("cliente_id")));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar os ingressos do pedido " + pedidoId + ".", e);
        }

        // a consulta acima já foi fechada; agora monta cada Ingresso do modelo.
        // O mesmo pedido costuma ter vários ingressos da mesma sessão: guarda para não recarregar.
        Map<Integer, Sessao> sessoesCarregadas = new HashMap<>();
        Map<Integer, Cliente> clientesCarregados = new HashMap<>();
        List<Ingresso> ingressos = new ArrayList<>();
        for (LinhaIngresso linha : linhas) {
            Sessao sessao = sessoesCarregadas.get(linha.sessaoId);
            if (sessao == null) {
                sessao = sessoes.buscarPorId(linha.sessaoId).orElseThrow(() ->
                        new PersistenciaException("O ingresso " + linha.id + " aponta para uma sessão que não existe."));
                sessoesCarregadas.put(linha.sessaoId, sessao);
            }
            Cliente cliente = clientesCarregados.get(linha.clienteId);
            if (cliente == null) {
                cliente = carregarCliente(linha.clienteId);
                clientesCarregados.put(linha.clienteId, cliente);
            }
            Assento assento = sessao.getSala().buscarAssento(linha.numeroAssento);
            if (assento == null) {
                throw new PersistenciaException("O ingresso " + linha.id + " aponta para o assento "
                        + linha.numeroAssento + ", que não existe na sala da sessão.");
            }
            ingressos.add(new Ingresso(linha.id, sessao.getFilme(), sessao, assento, cliente,
                    Ingresso.TipoIngresso.valueOf(linha.tipo)));
        }
        return ingressos;
    }

    private Cliente carregarCliente(int clienteId) throws PersistenciaException {
        Usuario usuario = usuarios.buscarPorId(clienteId).orElseThrow(() ->
                new PersistenciaException("O cliente " + clienteId + " não existe."));
        if (!(usuario instanceof Cliente)) {
            throw new PersistenciaException("O usuário " + clienteId + " não é um cliente.");
        }
        return (Cliente) usuario;
    }

    /** Dados crus de uma linha da consulta de ingressos. */
    private static class LinhaIngresso {
        final int id;
        final int sessaoId;
        final int numeroAssento;
        final String tipo;
        final int clienteId;

        LinhaIngresso(int id, int sessaoId, int numeroAssento, String tipo, int clienteId) {
            this.id = id;
            this.sessaoId = sessaoId;
            this.numeroAssento = numeroAssento;
            this.tipo = tipo;
            this.clienteId = clienteId;
        }
    }
}