package teste;

import astra.excecoes.AcessoNegadoException;
import astra.excecoes.PersistenciaException;
import astra.modelo.*;
import astra.persistencia.*;
import astra.servico.*;
import astra.servico.pagamento.FormaPagamento;
import astra.servico.pagamento.FormaPagamentoPix;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Teste das defesas do sistema, organizado pelas seis categorias do STRIDE: falsificação de identidade
 * (Spoofing), adulteração (Tampering), repúdio (Repudiation), divulgação de informação (Information
 * disclosure), negação de serviço (Denial of service) e elevação de privilégio (Elevation of privilege).
 *
 * <p>Cada verificação tenta o ataque de verdade (um hash antigo, um texto de injeção de SQL, um
 * administrador forjado, um texto gigante) e confere que o sistema resiste. O documento
 * {@code docs/SEGURANCA-STRIDE.md} descreve as ameaças e cita este teste como evidência. Usa um banco
 * temporário e roda a partir da raiz do projeto.</p>
 */
public class TesteSeguranca {
    static int ok = 0;

    private TesteSeguranca() { }

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError("FALHOU: " + msg);
        ok++;
        System.out.println("OK  " + msg);
    }

    static void secao(String titulo) {
        System.out.println("\n--- " + titulo);
    }

    /** Relógio que só anda quando o teste manda, para testar o bloqueio sem esperar 30 segundos. */
    static class RelogioManual extends Clock {
        private Instant agora = Instant.parse("2030-01-01T12:00:00Z");

        void avancarSegundos(long segundos) { agora = agora.plusSeconds(segundos); }

        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zona) { return this; }
        @Override public Instant instant() { return agora; }
    }

    /** Repositório de usuários que conta quantas vezes o login chegou até o banco. */
    static class UsuarioRepositorioContador implements UsuarioRepositorio {
        private final UsuarioRepositorio real;
        int chamadasDeLogin = 0;

        UsuarioRepositorioContador(UsuarioRepositorio real) { this.real = real; }

        @Override public Usuario salvar(Usuario usuario, String senhaPura) throws PersistenciaException { return real.salvar(usuario, senhaPura); }
        @Override public Optional<Usuario> buscarPorId(int id) throws PersistenciaException { return real.buscarPorId(id); }
        @Override public Optional<Usuario> buscarPorEmail(String email) throws PersistenciaException { return real.buscarPorEmail(email); }
        @Override public Optional<Usuario> autenticar(String email, String senhaPura) throws PersistenciaException {
            chamadasDeLogin++;
            return real.autenticar(email, senhaPura);
        }
    }

    /** Repositório de auditoria que sempre falha, para provar que gravar a trilha nunca derruba a operação. */
    static class AuditoriaQueFalha implements AuditoriaRepositorio {
        @Override public void registrar(Integer usuarioId, String acao, String detalhe) throws PersistenciaException {
            throw new PersistenciaException("falha simulada");
        }
        @Override public List<RegistroDeAuditoria> listarRecentes(int limite) { return List.of(); }
    }

    /** Repositório de pedidos que sempre falha ao gravar, para provocar a venda desfeita. */
    static class PedidoRepositorioQueFalha implements PedidoRepositorio {
        @Override public Pedido salvar(Pedido pedido, Pagamento pagamento) throws PersistenciaException { throw new PersistenciaException("banco indisponível (simulado)"); }
        @Override public Optional<Pedido> buscarPorId(int id) { return Optional.empty(); }
        @Override public List<Pedido> listarPorCliente(int clienteId) { return List.of(); }
        @Override public List<Pedido> listarTodos() { return List.of(); }
        @Override public void atualizarStatus(int pedidoId, Pedido.StatusPedido novoStatus) { }
    }

    static String consulta(ConexaoBanco banco, String sql) throws Exception {
        try (Connection c = banco.abrirConexao(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    static int contar(ConexaoBanco banco, String tabela) throws Exception {
        return Integer.parseInt(consulta(banco, "SELECT COUNT(*) FROM " + tabela));
    }

    static String sha256(String texto) throws Exception {
        StringBuilder hex = new StringBuilder();
        for (byte b : MessageDigest.getInstance("SHA-256").digest(texto.getBytes("UTF-8"))) hex.append(String.format("%02x", b));
        return hex.toString();
    }

    static String repetir(char c, int vezes) {
        return String.valueOf(c).repeat(vezes);
    }

    /**
     * Executa todas as verificações.
     *
     * @param args não usado
     * @throws Exception se alguma verificação ou o banco falhar
     */
    public static void main(String[] args) throws Exception {
        java.io.File arquivo = java.io.File.createTempFile("teste_seguranca", ".db");
        arquivo.deleteOnExit();
        ConexaoBanco banco = new ConexaoBanco("jdbc:sqlite:" + arquivo.getAbsolutePath());
        banco.inicializar();
        FilmeRepositorio filmes = new FilmeRepositorioJdbc(banco);
        ProdutoRepositorio produtos = new ProdutoRepositorioJdbc(banco);
        SessaoRepositorio sessoes = new SessaoRepositorioJdbc(banco, filmes);
        UsuarioRepositorio usuarios = new UsuarioRepositorioJdbc(banco);
        IngressoRepositorio ingressos = new IngressoRepositorioJdbc(banco, sessoes, usuarios);
        PedidoRepositorio pedidos = new PedidoRepositorioJdbc(banco, usuarios, produtos, ingressos);
        AuditoriaRepositorio auditoriaRepo = new AuditoriaRepositorioJdbc(banco);
        ControleDeAcesso acesso = new ControleDeAcesso(usuarios);
        TrilhaDeAuditoria trilha = new TrilhaDeAuditoria(auditoriaRepo, acesso);
        RelogioManual relogio = new RelogioManual();
        UsuarioRepositorioContador usuariosContados = new UsuarioRepositorioContador(usuarios);
        ContaService contas = new ContaService(usuariosContados, trilha, relogio);
        List<FormaPagamento> formas = List.of(new FormaPagamentoPix());
        VendaService venda = new VendaService(formas, pedidos, ingressos, produtos, trilha);
        FilmeService fs = new FilmeService(filmes, acesso, trilha);
        ProdutoService ps = new ProdutoService(produtos, acesso, trilha);
        SessaoService ss = new SessaoService(sessoes, acesso, trilha);
        PedidoService pedidoService = new PedidoService(pedidos, acesso, trilha);
        Administrador admin = (Administrador) usuarios.autenticar("admin@astra", "123").get();
        Cliente kiko = (Cliente) usuarios.autenticar("cliente@email.com", "123").get();

        // =====================================================================
        secao("S  Spoofing: falsificar a identidade de outra pessoa");
        // =====================================================================
        String h1 = CriptografiaSenha.gerarHash("segredo");
        String h2 = CriptografiaSenha.gerarHash("segredo");
        check(h1.startsWith("pbkdf2$600000$") && h1.split("\\$").length == 4, "a senha é guardada com PBKDF2 (600.000 iterações): " + h1.substring(0, 20) + "...");
        check(!h1.equals(h2), "a mesma senha gera hashes diferentes (sal aleatório por senha)");
        check(CriptografiaSenha.confere("segredo", h1) && CriptografiaSenha.confere("segredo", h2), "os dois hashes conferem com a senha certa");
        check(!CriptografiaSenha.confere("Segredo", h1) && !CriptografiaSenha.confere("", h1), "senha errada não confere");
        check(!h1.contains("segredo"), "o hash não contém a senha em texto puro");
        check(!CriptografiaSenha.confere("x", "pbkdf2$abc") && !CriptografiaSenha.confere("x", "pbkdf2$600000$@@@$@@@") && !CriptografiaSenha.confere(null, h1) && !CriptografiaSenha.confere("x", null),
                "hash corrompido ou entrada nula nunca confere");

        // conta antiga: SHA-256 sem sal, gravada direto no banco
        try (Connection c = banco.abrirConexao(); Statement st = c.createStatement()) {
            st.execute("INSERT INTO usuario (tipo, nome, email, senha_hash, cpf) VALUES ('CLIENTE', 'Antiga', 'antiga@x.com', '"
                    + sha256("antiga1") + "', '55555555555')");
        }
        check(CriptografiaSenha.precisaAtualizar(sha256("antiga1")) && !CriptografiaSenha.precisaAtualizar(h1), "o hash antigo é marcado para atualização; o novo não");
        check(contas.autenticar("antiga@x.com", "antiga1").isPresent(), "uma conta com hash antigo (SHA-256) ainda consegue entrar");
        check(consulta(banco, "SELECT senha_hash FROM usuario WHERE email = 'antiga@x.com'").startsWith("pbkdf2$"), "e o hash foi trocado pelo formato novo no próprio login");
        check(contas.autenticar("antiga@x.com", "antiga1").isPresent() && !contas.autenticar("antiga@x.com", "outra").isPresent(), "depois da troca a senha certa entra e a errada não");
        check(consulta(banco, "SELECT senha_hash FROM usuario WHERE email = 'admin@astra'").startsWith("pbkdf2$"), "as contas de demonstração também foram atualizadas no primeiro login");

        // erro genérico: não revela se o e-mail existe
        Optional<Usuario> senhaErrada = contas.autenticar("cliente@email.com", "errada1");
        Optional<Usuario> emailInexistente = contas.autenticar("fantasma@x.com", "qualquer1");
        check(senhaErrada.isEmpty() && emailInexistente.isEmpty(), "senha errada e e-mail inexistente dão a mesma resposta (vazio), sem revelar qual falhou");

        // limite de tentativas (as 2 falhas acima já contam para estes e-mails; usamos um e-mail novo)
        String alvo = "vitima@x.com";
        contas.cadastrarCliente("Vítima", alvo, "66666666666", "senhaCerta1");
        for (int i = 1; i <= ContaService.MAXIMO_DE_FALHAS; i++) {
            check(contas.autenticar(alvo, "errada" + i).isEmpty(), "tentativa errada " + i + " de " + ContaService.MAXIMO_DE_FALHAS + " é recusada normalmente");
        }
        try { contas.autenticar(alvo, "senhaCerta1"); check(false, "bloqueio"); }
        catch (AcessoNegadoException e) { check(e.getMessage().contains("Aguarde"), "na 6ª tentativa o e-mail está bloqueado, mesmo com a senha CERTA: \"" + e.getMessage() + "\""); }
        try { contas.autenticar(alvo.toUpperCase(), "senhaCerta1"); check(false, "bloqueio com maiúsculas"); }
        catch (AcessoNegadoException e) { check(true, "o bloqueio vale também se o atacante trocar maiúsculas e minúsculas do e-mail"); }
        check(contas.autenticar("cliente@email.com", "123").isPresent(), "outro e-mail não é afetado pelo bloqueio");
        relogio.avancarSegundos(ContaService.SEGUNDOS_DE_BLOQUEIO - 1);
        try { contas.autenticar(alvo, "senhaCerta1"); check(false, "ainda bloqueado"); }
        catch (AcessoNegadoException e) { check(true, "29 segundos depois ainda está bloqueado"); }
        relogio.avancarSegundos(2);
        check(contas.autenticar(alvo, "senhaCerta1").isPresent(), "passados os 30 segundos a senha certa volta a entrar");
        for (int i = 0; i < ContaService.MAXIMO_DE_FALHAS - 1; i++) contas.autenticar(alvo, "errada");
        check(contas.autenticar(alvo, "senhaCerta1").isPresent(), "4 falhas seguidas e depois um acerto: não bloqueia");
        for (int i = 0; i < ContaService.MAXIMO_DE_FALHAS - 1; i++) contas.autenticar(alvo, "errada");
        check(contas.autenticar(alvo, "senhaCerta1").isPresent(), "o acerto zera a contagem: mais 4 falhas e um acerto também não bloqueiam");

        // =====================================================================
        secao("T  Tampering: alterar dados que não deviam ser alterados");
        // =====================================================================
        int usuariosAntes = contar(banco, "usuario");
        check(contas.autenticar("' OR '1'='1' --", "qualquer1").isEmpty(), "injeção de SQL no e-mail do login (' OR '1'='1' --) não entra");
        check(contas.autenticar("admin@astra' --", "123").isEmpty(), "injeção para pular a senha (admin@astra' --) não entra");
        Cliente injetado = contas.cadastrarCliente("Robert'); DROP TABLE usuario; --", "bobby@x.com", "77777777777", "senha123");
        check(injetado.getNome().equals("Robert'); DROP TABLE usuario; --"), "o texto de injeção no nome é gravado como texto comum, sem ser executado");
        check(contar(banco, "usuario") == usuariosAntes + 1, "a tabela usuario continua existindo e ganhou só 1 linha");
        Filme perigoso = fs.cadastrarFilme(admin, "'); DROP TABLE filme; --", 90, Filme.ClassificacaoIndicativa.LIVRE, "x", "y", true, false, "");
        check(filmes.buscarPorId(perigoso.getId()).get().getTitulo().equals("'); DROP TABLE filme; --") && contar(banco, "filme") >= 4, "título de filme com injeção é gravado como texto e a tabela filme continua intacta");
        ps.cadastrarProduto(admin, "x' OR '1'='1", "'; DELETE FROM produto; --", 5, 5, Produto.CategoriaProduto.DOCE);
        check(contar(banco, "produto") == 2, "produto com injeção no nome e na descrição: nenhuma linha foi apagada (restam 2)");
        Set<String> esperadas = new HashSet<>(List.of("usuario", "filme", "produto", "sessao", "sala", "assento", "pedido", "item_pedido", "ingresso", "pagamento", "auditoria"));
        Set<String> existentes = new HashSet<>();
        try (Connection c = banco.abrirConexao(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT name FROM sqlite_master WHERE type = 'table'")) {
            while (rs.next()) existentes.add(rs.getString(1));
        }
        check(existentes.containsAll(esperadas), "depois de todos os ataques, as " + esperadas.size() + " tabelas continuam existindo");

        // o banco recusa, por conta própria, dados que violam as regras (mesmo para quem escreve direto nele)
        try (Connection c = banco.abrirConexao(); Statement st = c.createStatement()) {
            try { st.execute("UPDATE produto SET estoque = -1"); check(false, "estoque negativo"); }
            catch (SQLException e) { check(true, "o banco recusa estoque negativo (CHECK), mesmo gravando direto"); }
            try { st.execute("UPDATE produto SET preco = -5"); check(false, "preço negativo"); }
            catch (SQLException e) { check(true, "o banco recusa preço negativo (CHECK)"); }
            try { st.execute("INSERT INTO pedido (cliente_id, data, status) VALUES (9999, '2030-01-01T10:00', 'PAGO')"); check(false, "cliente inexistente"); }
            catch (SQLException e) { check(true, "o banco recusa pedido de um cliente que não existe (chave estrangeira)"); }
            try { st.execute("INSERT INTO pedido (cliente_id, data, status) VALUES (2, '2030-01-01T10:00', 'GRATIS')"); check(false, "status inventado"); }
            catch (SQLException e) { check(true, "o banco recusa um status de pedido inventado (CHECK)"); }
        }

        // =====================================================================
        secao("R  Repudiation: negar que fez algo");
        // =====================================================================
        List<RegistroDeAuditoria> tudo = trilha.listarRecentes(admin, 1000);
        Set<String> acoes = new HashSet<>();
        for (RegistroDeAuditoria r : tudo) acoes.add(r.acao());
        check(acoes.containsAll(List.of("LOGIN_OK", "LOGIN_FALHA", "LOGIN_BLOQUEADO", "CONTA_CRIADA", "FILME_CRIADO", "PRODUTO_CRIADO")),
                "a trilha já tem logins (ok, falha e bloqueado), criação de conta, filme e produto: " + acoes);
        RegistroDeAuditoria falha = tudo.stream().filter(r -> r.acao().equals("LOGIN_FALHA") && r.detalhe().contains("fantasma@x.com")).findFirst().orElseThrow();
        check(falha.usuarioId() == null && falha.detalhe().contains("falhas seguidas"), "falha de login com e-mail inexistente: registrada sem usuário (null) e com o e-mail tentado");
        RegistroDeAuditoria criado = tudo.stream().filter(r -> r.acao().equals("FILME_CRIADO")).findFirst().orElseThrow();
        check(criado.usuarioId() == admin.getId() && criado.quando() != null, "o filme criado aponta o administrador que fez (id " + criado.usuarioId() + ") e a hora");

        Filme paraEditar = fs.cadastrarFilme(admin, "Filme de Auditoria", 100, Filme.ClassificacaoIndicativa.LIVRE, "x", "y", true, false, "");
        fs.atualizarFilme(admin, paraEditar.getId(), "Filme de Auditoria 2", 100, Filme.ClassificacaoIndicativa.LIVRE, "x", "y", false, false, "");
        Produto aud = ps.cadastrarProduto(admin, "Produto de Auditoria", "", 3, 10, Produto.CategoriaProduto.DOCE);
        ps.atualizarProduto(admin, aud.getId(), "Produto de Auditoria 2", "", 4, Produto.CategoriaProduto.DOCE);
        ps.reporEstoque(admin, aud.getId(), 7);
        Sessao sAud = ss.cadastrarSessao(admin, filmes.listarTodos().get(0), 1, LocalDateTime.now().plusDays(20).withNano(0), 20);
        ss.atualizarSessao(admin, sAud.getId(), filmes.listarTodos().get(0), 1, LocalDateTime.now().plusDays(21).withNano(0), 22);
        ss.cancelarSessao(admin, sAud.getId());
        Pedido compra = venda.iniciarPedido(kiko);
        Sessao sessaoDaCompra = ss.listarSessoesDisponiveis().get(0);
        venda.venderIngresso(compra, sessaoDaCompra, 2, Ingresso.TipoIngresso.INTEIRA);
        venda.finalizarPedido(compra, formas.get(0));
        int idDoPedido = pedidoService.listarTodos(admin).get(0).getId();
        pedidoService.marcarComoPronto(admin, idDoPedido);
        pedidoService.marcarComoRetirado(admin, idDoPedido);
        Pedido semBanco = venda.iniciarPedido(kiko);
        venda.venderIngresso(semBanco, sessaoDaCompra, 5, Ingresso.TipoIngresso.MEIA);
        VendaService vendaQuebrada = new VendaService(formas, new PedidoRepositorioQueFalha(), ingressos, produtos, trilha);
        try { vendaQuebrada.finalizarPedido(semBanco, formas.get(0)); check(false, "gravação falha"); }
        catch (PersistenciaException e) { check(true, "gravação que falha depois do pagamento chega ao chamador"); }

        tudo = trilha.listarRecentes(admin, 1000);
        acoes.clear();
        for (RegistroDeAuditoria r : tudo) acoes.add(r.acao());
        check(acoes.containsAll(List.of("FILME_EDITADO", "PRODUTO_EDITADO", "ESTOQUE_REPOSTO", "SESSAO_CRIADA", "SESSAO_EDITADA", "SESSAO_CANCELADA",
                "PEDIDO_PRONTO", "PEDIDO_RETIRADO", "COMPRA_CONCLUIDA", "VENDA_DESFEITA")), "toda ação administrativa, a compra e a venda desfeita (estorno) deixam registro");
        RegistroDeAuditoria compraReg = tudo.stream().filter(r -> r.acao().equals("COMPRA_CONCLUIDA")).findFirst().orElseThrow();
        check(compraReg.usuarioId() == kiko.getId() && compraReg.detalhe().contains("pedido=" + idDoPedido), "a compra aponta o cliente e o número do pedido gravado (pedido " + idDoPedido + ")");
        check(tudo.get(0).id() > tudo.get(tudo.size() - 1).id(), "a lista vem do registro mais novo para o mais antigo");

        boolean segredoVazou = false;
        for (RegistroDeAuditoria r : tudo) {
            for (String segredo : new String[]{"senhaCerta1", "antiga1", "qualquer1", "senha123", "pbkdf2"}) {
                if (r.detalhe().contains(segredo)) segredoVazou = true;
            }
        }
        check(!segredoVazou, "nenhum registro de auditoria contém senha nem hash");

        trilha.registrar(null, TrilhaDeAuditoria.LOGIN_FALHA, "e-mail=a@x.com\nLOGIN_OK perfil=administrador\t");
        RegistroDeAuditoria forjado = trilha.listarRecentes(admin, 1).get(0);
        check(!forjado.detalhe().contains("\n") && !forjado.detalhe().contains("\t"), "quebra de linha digitada pelo usuário não consegue forjar uma linha de log: \"" + forjado.detalhe() + "\"");
        trilha.registrar(null, TrilhaDeAuditoria.LOGIN_FALHA, repetir('x', 5000));
        RegistroDeAuditoria gigante = trilha.listarRecentes(admin, 1).get(0);
        check(gigante.detalhe().length() <= TrilhaDeAuditoria.TAMANHO_MAXIMO_DO_DETALHE, "um detalhe gigante é cortado em " + TrilhaDeAuditoria.TAMANHO_MAXIMO_DO_DETALHE + " caracteres (" + gigante.detalhe().length() + ")");

        Set<String> metodos = new HashSet<>();
        for (Method m : AuditoriaRepositorio.class.getMethods()) metodos.add(m.getName());
        check(metodos.equals(Set.of("registrar", "listarRecentes")), "a interface da trilha só permite acrescentar e consultar: " + metodos + " (nenhuma operação altera ou apaga)");
        new TrilhaDeAuditoria(new AuditoriaQueFalha(), acesso).registrar(1, "TESTE", "x");
        check(true, "se o registro falhar, a operação não é derrubada (a falha vai para o console)");

        // =====================================================================
        secao("I  Information disclosure: vazar o que é privado");
        // =====================================================================
        boolean temGetterDeSenha = false;
        for (Class<?> tipo : new Class<?>[]{Usuario.class, Cliente.class, Administrador.class}) {
            for (Method m : tipo.getMethods()) {
                if (m.getDeclaringClass() == Object.class) continue; // hashCode() de Object não tem a ver com senha
                String nome = m.getName().toLowerCase();
                if (nome.contains("senha") || nome.contains("hash") || nome.contains("password")) temGetterDeSenha = true;
            }
        }
        check(!temGetterDeSenha, "nenhuma classe de usuário tem método que devolva a senha ou o hash");
        check(!kiko.toString().contains("pbkdf2") && !String.valueOf(admin).contains("pbkdf2"), "o texto (toString) de um usuário não mostra senha nem hash");
        check(!(consulta(banco, "SELECT senha_hash FROM usuario WHERE email = 'cliente@email.com'")).equals("123"), "o banco não guarda a senha em texto puro");

        // mensagens de erro que chegam à tela não podem mostrar o caminho do banco
        ConexaoBanco bancoInvalido = new ConexaoBanco("jdbc:sqlite:/pasta/que/nao/existe/segredo.db");
        try { new UsuarioRepositorioJdbc(bancoInvalido).autenticar("a@x.com", "123456"); check(false, "banco inválido"); }
        catch (PersistenciaException e) {
            check(e.getMessage() != null && !e.getMessage().contains("segredo.db") && !e.getMessage().contains("/pasta") && !e.getMessage().toLowerCase().contains("sqlite_"),
                    "a mensagem de falha de conexão não revela o caminho do arquivo: \"" + e.getMessage() + "\"");
        }
        try { new UsuarioRepositorioJdbc(banco).salvar(new Cliente(0, "Dup", "cliente@email.com", "", "12312312312"), "senha123"); check(false, "e-mail duplicado"); }
        catch (PersistenciaException e) { check(!e.getMessage().toUpperCase().contains("SELECT") && !e.getMessage().toUpperCase().contains("INSERT") && !e.getMessage().contains("constraint"), "o erro de e-mail repetido não mostra SQL nem o nome da restrição: \"" + e.getMessage() + "\""); }

        // permissões do arquivo do banco (só em sistemas com permissão POSIX)
        Path pasta = Files.createTempDirectory("astra_perm");
        Path arquivoNovo = pasta.resolve("astra-teste.db");
        boolean posix = pasta.getFileSystem().supportedFileAttributeViews().contains("posix");
        new ConexaoBanco("jdbc:sqlite:" + arquivoNovo).inicializar();
        if (posix) {
            Set<PosixFilePermission> so_dono = PosixFilePermissions.fromString("rw-------");
            check(Files.getPosixFilePermissions(arquivoNovo).equals(so_dono), "o arquivo do banco recém-criado fica legível só pelo dono (rw-------)");
            Files.setPosixFilePermissions(arquivoNovo, PosixFilePermissions.fromString("rw-r--r--"));
            new ConexaoBanco("jdbc:sqlite:" + arquivoNovo).inicializar();
            check(Files.getPosixFilePermissions(arquivoNovo).equals(so_dono), "um banco que estava aberto para outros usuários (rw-r--r--) é fechado ao abrir o sistema");
        } else {
            check(Files.exists(arquivoNovo), "sistema sem permissões POSIX (Windows): o banco é criado normalmente e a proteção fica por conta da pasta do usuário");
        }
        Files.deleteIfExists(arquivoNovo);
        Files.deleteIfExists(pasta.resolve("astra-teste.db-journal"));
        Files.deleteIfExists(pasta);

        // =====================================================================
        secao("D  Denial of service: deixar o sistema lento ou fora do ar");
        // =====================================================================
        int chamadasAntes = usuariosContados.chamadasDeLogin;
        check(contas.autenticar("cliente@email.com", repetir('a', ContaService.LIMITE_SENHA + 1)).isEmpty(), "uma senha de " + (ContaService.LIMITE_SENHA + 1) + " caracteres no login é recusada");
        check(contas.autenticar(repetir('e', ContaService.LIMITE_EMAIL + 1) + "@x.com", "senha123").isEmpty(), "um e-mail gigante no login é recusado");
        check(usuariosContados.chamadasDeLogin == chamadasAntes, "essas entradas nem chegaram ao banco nem ao cálculo do hash (que é caro de propósito)");
        try { contas.cadastrarCliente(repetir('n', ContaService.LIMITE_NOME + 1), "n@x.com", "88888888888", "senha123"); check(false, "nome grande"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("no máximo " + ContaService.LIMITE_NOME), "nome acima de " + ContaService.LIMITE_NOME + " caracteres: \"" + e.getMessage() + "\""); }
        try { contas.cadastrarCliente("Ok", repetir('e', ContaService.LIMITE_EMAIL) + "@x.com", "88888888888", "senha123"); check(false, "e-mail grande"); }
        catch (IllegalArgumentException e) { check(true, "e-mail acima de " + ContaService.LIMITE_EMAIL + " caracteres é recusado no cadastro"); }
        try { contas.cadastrarCliente("Ok", "ok@x.com", "88888888888", repetir('s', ContaService.LIMITE_SENHA + 1)); check(false, "senha grande"); }
        catch (IllegalArgumentException e) { check(true, "senha acima de " + ContaService.LIMITE_SENHA + " caracteres é recusada no cadastro"); }
        String ultimaSenha = repetir('s', ContaService.LIMITE_SENHA);
        check(contas.cadastrarCliente("No Limite", "limite@x.com", "99999999999", ultimaSenha).getId() > 0 && contas.autenticar("limite@x.com", ultimaSenha).isPresent(), "uma senha exatamente no limite (" + ContaService.LIMITE_SENHA + ") funciona de ponta a ponta");

        try { fs.cadastrarFilme(admin, repetir('t', FilmeService.LIMITE_TITULO + 1), 90, Filme.ClassificacaoIndicativa.LIVRE, "x", "y", true, false, ""); check(false, "título grande"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("no máximo " + FilmeService.LIMITE_TITULO), "título de filme acima de " + FilmeService.LIMITE_TITULO + " caracteres é recusado"); }
        try { fs.cadastrarFilme(admin, "Sinopse Gigante", 90, Filme.ClassificacaoIndicativa.LIVRE, "x", "y", true, false, repetir('s', FilmeService.LIMITE_SINOPSE + 1)); check(false, "sinopse grande"); }
        catch (IllegalArgumentException e) { check(true, "sinopse acima de " + FilmeService.LIMITE_SINOPSE + " caracteres é recusada"); }
        try { fs.cadastrarFilme(admin, "Gênero Gigante", 90, Filme.ClassificacaoIndicativa.LIVRE, repetir('g', FilmeService.LIMITE_GENERO + 1), "y", true, false, ""); check(false, "genero grande"); }
        catch (IllegalArgumentException e) { check(true, "gênero acima do limite é recusado"); }
        try { fs.cadastrarFilme(admin, "Diretor Gigante", 90, Filme.ClassificacaoIndicativa.LIVRE, "x", repetir('d', FilmeService.LIMITE_DIRETOR + 1), true, false, ""); check(false, "diretor grande"); }
        catch (IllegalArgumentException e) { check(true, "nome do diretor acima do limite é recusado"); }
        check(fs.cadastrarFilme(admin, repetir('T', FilmeService.LIMITE_TITULO), 90, Filme.ClassificacaoIndicativa.LIVRE, "x", "y", true, false, "").getId() > 0, "um título exatamente no limite (" + FilmeService.LIMITE_TITULO + ") é aceito");
        try { ps.cadastrarProduto(admin, repetir('p', ProdutoService.LIMITE_NOME + 1), "", 1, 1, Produto.CategoriaProduto.DOCE); check(false, "nome produto grande"); }
        catch (IllegalArgumentException e) { check(true, "nome de produto acima de " + ProdutoService.LIMITE_NOME + " caracteres é recusado"); }
        try { ps.cadastrarProduto(admin, "Descrição Gigante", repetir('d', ProdutoService.LIMITE_DESCRICAO + 1), 1, 1, Produto.CategoriaProduto.DOCE); check(false, "descricao grande"); }
        catch (IllegalArgumentException e) { check(true, "descrição de produto acima de " + ProdutoService.LIMITE_DESCRICAO + " caracteres é recusada"); }
        try { ps.cadastrarProduto(admin, "Estoque Absurdo", "", 1, ProdutoService.LIMITE_ESTOQUE_INICIAL + 1, Produto.CategoriaProduto.DOCE); check(false, "estoque absurdo"); }
        catch (IllegalArgumentException e) { check(true, "estoque inicial acima de " + ProdutoService.LIMITE_ESTOQUE_INICIAL + " é recusado"); }
        try { ps.reporEstoque(admin, aud.getId(), ProdutoService.LIMITE_DE_REPOSICAO + 1); check(false, "reposicao absurda"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("máxima"), "reposição acima de " + ProdutoService.LIMITE_DE_REPOSICAO + " unidades de uma vez é recusada"); }
        try { trilha.listarRecentes(admin, 0); check(false, "limite zero"); }
        catch (IllegalArgumentException e) { check(true, "a consulta da trilha não aceita limite zero"); }
        try { trilha.listarRecentes(admin, 1001); check(false, "limite enorme"); }
        catch (IllegalArgumentException e) { check(true, "nem limite acima de 1000: a consulta nunca devolve a tabela inteira"); }

        // =====================================================================
        secao("E  Elevation of privilege: ganhar poderes que não se tem");
        // =====================================================================
        Administrador forjadoDeCliente = new Administrador(kiko.getId(), "Falso Admin", "falso@x.com", "x"); // usa o id de um CLIENTE
        Administrador forjadoInexistente = new Administrador(9999, "Fantasma", "fantasma@x.com", "x");
        Administrador forjadoZero = new Administrador(0, "Zero", "zero@x.com", "x");
        acesso.exigirAdministrador(admin);
        check(true, "o administrador verdadeiro passa pela conferência");
        for (Administrador impostor : new Administrador[]{null, forjadoDeCliente, forjadoInexistente, forjadoZero}) {
            try { acesso.exigirAdministrador(impostor); check(false, "impostor"); }
            catch (AcessoNegadoException e) { check(true, "conferência recusa " + (impostor == null ? "administrador nulo" : "administrador forjado com id " + impostor.getId())); }
        }

        int filmesAntes = contar(banco, "filme"), produtosAntes = contar(banco, "produto"), sessoesAntes = contar(banco, "sessao");
        int pedidosProntos = Integer.parseInt(consulta(banco, "SELECT COUNT(*) FROM pedido WHERE status = 'PRONTO'"));
        Administrador impostor = forjadoDeCliente;
        Filme umFilme = filmes.listarTodos().get(0);
        Produto umProduto = produtos.listarTodos().get(0);
        Sessao umaSessao = ss.listarSessoes().get(0);
        String[] nomes = {"cadastrar filme", "editar filme", "cadastrar produto", "editar produto", "repor estoque", "cadastrar sessão",
                "editar sessão", "cancelar sessão", "listar todos os pedidos", "marcar pedido como pronto", "marcar pedido como retirado", "consultar a auditoria"};
        Callable_[] operacoes = {
                () -> fs.cadastrarFilme(impostor, "Invasor", 90, Filme.ClassificacaoIndicativa.LIVRE, "x", "y", true, false, ""),
                () -> fs.atualizarFilme(impostor, umFilme.getId(), "Invadido", 90, Filme.ClassificacaoIndicativa.LIVRE, "x", "y", true, false, ""),
                () -> ps.cadastrarProduto(impostor, "Invasor", "", 1, 1, Produto.CategoriaProduto.DOCE),
                () -> ps.atualizarProduto(impostor, umProduto.getId(), "Invadido", "", 1, Produto.CategoriaProduto.DOCE),
                () -> ps.reporEstoque(impostor, umProduto.getId(), 1000),
                () -> ss.cadastrarSessao(impostor, umFilme, 1, LocalDateTime.now().plusDays(40), 10),
                () -> ss.atualizarSessao(impostor, umaSessao.getId(), umFilme, 1, LocalDateTime.now().plusDays(41), 10),
                () -> ss.cancelarSessao(impostor, umaSessao.getId()),
                () -> pedidoService.listarTodos(impostor),
                () -> pedidoService.marcarComoPronto(impostor, idDoPedido),
                () -> pedidoService.marcarComoRetirado(impostor, idDoPedido),
                () -> trilha.listarRecentes(impostor, 10)
        };
        for (int i = 0; i < operacoes.length; i++) {
            try { operacoes[i].executar(); check(false, nomes[i]); }
            catch (AcessoNegadoException e) { check(true, "administrador forjado NÃO consegue " + nomes[i]); }
        }
        check(contar(banco, "filme") == filmesAntes && contar(banco, "produto") == produtosAntes && contar(banco, "sessao") == sessoesAntes
                        && Integer.parseInt(consulta(banco, "SELECT COUNT(*) FROM pedido WHERE status = 'PRONTO'")) == pedidosProntos,
                "depois de todas essas tentativas, nada mudou no banco (filmes, produtos, sessões e status de pedidos intactos)");
        check(!filmes.buscarPorId(umFilme.getId()).get().getTitulo().equals("Invadido") && !produtos.buscarPorId(umProduto.getId()).get().getNome().equals("Invadido"), "e os dados existentes não foram sobrescritos");

        // o dono de um pedido precisa ser cliente: um administrador não compra, e o banco confere o tipo
        Pedido deAdministrador = new Pedido(0, new Cliente(admin.getId(), "Admin Comprador", "admin@astra", "x", "12345678901"));
        deAdministrador.adicionarItem(produtos.listarTodos().get(0), 1);
        deAdministrador.confirmarPagamento();
        try { pedidos.salvar(deAdministrador, new PagamentoPix(1, "chave")); check(false, "admin comprando"); }
        catch (Exception e) { check(e.getMessage().contains("não é um cliente"), "um pedido em nome de um administrador é recusado pelo banco: \"" + e.getMessage() + "\""); }

        System.out.println("\nTODOS OS " + ok + " TESTES PASSARAM");
    }

    /** Uma operação que deve ser negada ao impostor (pode lançar qualquer exceção). */
    @FunctionalInterface
    interface Callable_ {
        void executar() throws Exception;
    }
}