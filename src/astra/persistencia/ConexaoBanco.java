package astra.persistencia;

import astra.excecoes.PersistenciaException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Ponto único de acesso ao banco de dados (SQLite via JDBC).
 *
 * Uso típico (no Main ou no AstraFrame):
 * <pre>
 *     ConexaoBanco banco = new ConexaoBanco();
 *     banco.inicializar();                       // cria tabelas e dados iniciais
 *     FilmeRepositorio filmes = new FilmeRepositorioJdbc(banco);
 * </pre>
 *
 * Nos repositórios, cada operação abre e fecha a própria conexão com
 * try-with-resources:
 * <pre>
 *     try (Connection con = banco.abrirConexao();
 *          PreparedStatement ps = con.prepareStatement("SELECT ... WHERE id = ?")) {
 *         ps.setInt(1, id);
 *         ...
 *     } catch (SQLException e) {
 *         throw new PersistenciaException("Erro ao buscar filme", e);
 *     }
 * </pre>
 *
 * O arquivo do banco é "astra.db" na pasta em que o programa é executado
 * (no IntelliJ, a raiz do projeto). Para usar outro arquivo, rode com
 * -Dastra.banco.url=jdbc:sqlite:outro.db
 */
public class ConexaoBanco {

    /** URL JDBC usada quando nada é informado: o arquivo {@code astra.db} na pasta de execução. */
    public static final String URL_PADRAO = "jdbc:sqlite:astra.db";

    private static final String SCRIPT_SCHEMA = "sql/schema.sql";
    private static final String SCRIPT_SEED = "sql/seed.sql";
    private static final String PASTA_SCRIPTS_FONTE = "src/astra/persistencia";

    private final String url;

    /** Usa o arquivo padrão astra.db (ou o definido em -Dastra.banco.url). */
    public ConexaoBanco() {
        this(System.getProperty("astra.banco.url", URL_PADRAO));
    }

    /**
     * Usa a URL JDBC informada (útil para testes com um arquivo temporário).
     *
     * @param url a URL JDBC, por exemplo {@code jdbc:sqlite:teste.db}
     * @throws IllegalArgumentException se a URL for nula ou estiver em branco
     */
    public ConexaoBanco(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("A URL do banco é obrigatória!");
        }
        this.url = url;
    }

    /**
     * Abre uma nova conexão. Quem chama é responsável por fechá-la
     * (use try-with-resources).
     *
     * @return a conexão aberta, com as chaves estrangeiras ligadas
     * @throws PersistenciaException se não for possível conectar (por exemplo, driver fora do classpath)
     */
    public Connection abrirConexao() throws PersistenciaException {
        try {
            Connection conexao = DriverManager.getConnection(url);
            try (Statement st = conexao.createStatement()) {
                // o SQLite só respeita as FOREIGN KEYs se isso for ligado em cada conexão
                st.execute("PRAGMA foreign_keys = ON");
            }
            return conexao;
        } catch (SQLException e) {
            throw new PersistenciaException(
                    // a URL (que mostra o caminho do arquivo) fica só na causa, para quem depura: esta mensagem
                    // chega à tela do usuário e não deve revelar onde o banco está
                    "Não foi possível conectar ao banco de dados. "
                            + "Verifique se o driver lib/sqlite-jdbc está no classpath.", e);
        }
    }

    /**
     * Cria as tabelas que ainda não existem (schema.sql) e, se o banco
     * estiver vazio, insere os dados iniciais (seed.sql). Pode ser chamado
     * toda vez que o sistema abre: não apaga dados existentes. Ao terminar, deixa o arquivo do banco
     * legível só pelo dono, quando o sistema operacional permite.
     *
     * @throws PersistenciaException se um dos scripts não for encontrado ou o banco falhar
     */
    public void inicializar() throws PersistenciaException {
        try (Connection conexao = abrirConexao()) {
            conexao.setAutoCommit(false); // tudo ou nada
            try {
                executarScript(conexao, SCRIPT_SCHEMA);
                if (bancoVazio(conexao)) {
                    executarScript(conexao, SCRIPT_SEED);
                }
                conexao.commit();
            } catch (SQLException | PersistenciaException e) {
                conexao.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao inicializar o banco de dados.", e);
        }
        restringirPermissoesDoArquivo();
    }

    /**
     * Deixa o arquivo do banco legível e gravável só pelo dono (permissão 600). O arquivo guarda CPFs, e-mails e
     * hashes de senha, e por padrão um arquivo novo costuma ficar legível por outros usuários da mesma máquina.
     * Só tem efeito em sistemas com permissões POSIX (Linux e macOS); no Windows, quem protege o arquivo é a
     * permissão da pasta do usuário, e esta chamada não faz nada. Falhar aqui nunca impede o sistema de abrir.
     */
    private void restringirPermissoesDoArquivo() {
        String prefixo = "jdbc:sqlite:";
        if (!url.startsWith(prefixo)) {
            return;
        }
        Path arquivo = Paths.get(url.substring(prefixo.length()));
        try {
            if (Files.exists(arquivo)) {
                Files.setPosixFilePermissions(arquivo, PosixFilePermissions.fromString("rw-------"));
            }
        } catch (UnsupportedOperationException | IOException | IllegalArgumentException e) {
            // sistema sem permissões POSIX, ou URL que não aponta para um arquivo comum: nada a restringir
        }
    }

    /**
     * Informa a URL JDBC em uso.
     *
     * @return a URL, por exemplo {@code jdbc:sqlite:astra.db}
     */
    public String getUrl() {
        return url;
    }

    // ------------------------------------------------------------------

    private boolean bancoVazio(Connection conexao) throws SQLException {
        try (Statement st = conexao.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM usuario")) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    private void executarScript(Connection conexao, String recurso)
            throws SQLException, PersistenciaException {
        try (Statement st = conexao.createStatement()) {
            for (String comando : lerComandos(recurso)) {
                st.execute(comando);
            }
        }
    }

    /**
     * Lê o arquivo .sql e separa os comandos pelo ';'. Linhas que começam
     * com "--" são comentários e são ignoradas.
     *
     * Procura primeiro no classpath (o IntelliJ copia os .sql junto com os
     * .class) e, se não achar, na pasta src/astra/persistencia/sql do
     * projeto (caso de quem compila com javac pela linha de comando).
     */
    private List<String> lerComandos(String recurso) throws PersistenciaException {
        String texto;
        try (InputStream in = ConexaoBanco.class.getResourceAsStream(recurso)) {
            if (in != null) {
                texto = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                Path arquivo = Paths.get(PASTA_SCRIPTS_FONTE, recurso);
                if (!Files.exists(arquivo)) {
                    throw new PersistenciaException("Script " + recurso + " não encontrado nem no classpath "
                            + "nem em " + arquivo.toAbsolutePath() + ". Execute o programa a partir da raiz do projeto.");
                }
                texto = Files.readString(arquivo, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new PersistenciaException("Erro ao ler o script " + recurso, e);
        }

        StringBuilder semComentarios = new StringBuilder();
        for (String linha : texto.split("\\R")) {
            if (!linha.trim().startsWith("--")) {
                semComentarios.append(linha).append('\n');
            }
        }

        List<String> comandos = new ArrayList<>();
        for (String comando : semComentarios.toString().split(";")) {
            if (!comando.isBlank()) {
                comandos.add(comando.trim());
            }
        }
        return comandos;
    }
}