package astra.servico;

import astra.excecoes.AcessoNegadoException;
import astra.excecoes.PersistenciaException;
import astra.modelo.Administrador;
import astra.modelo.Cliente;
import astra.modelo.Usuario;
import astra.persistencia.UsuarioRepositorio;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Casos de uso das contas de acesso: entrar no sistema e criar a conta de um cliente.
 *
 * <p>As regras de validação do cadastro (nome, e-mail, CPF e senha) ficam aqui, e não na tela,
 * para valerem em qualquer interface e poderem ser testadas sem abrir janela. Depende apenas
 * da interface {@link UsuarioRepositorio}.</p>
 *
 * <p>Proteções do login: depois de {@value #MAXIMO_DE_FALHAS} senhas erradas seguidas para o mesmo e-mail, novas
 * tentativas são recusadas por {@value #SEGUNDOS_DE_BLOQUEIO} segundos (contra adivinhação de senha). Textos enormes
 * de e-mail ou senha são recusados sem nem calcular o hash (contra negação de serviço). Cada login, bem-sucedido ou
 * não, entra na trilha de auditoria.</p>
 */
public class ContaService {

    /** Quantas senhas erradas seguidas, para o mesmo e-mail, levam ao bloqueio temporário. */
    public static final int MAXIMO_DE_FALHAS = 5;
    /** Quanto dura o bloqueio, em segundos. */
    public static final int SEGUNDOS_DE_BLOQUEIO = 30;
    /** Tamanho máximo do nome, em caracteres. */
    public static final int LIMITE_NOME = 100;
    /** Tamanho máximo do e-mail, em caracteres (o limite do padrão de e-mail). */
    public static final int LIMITE_EMAIL = 254;
    /** Tamanho máximo da senha, em caracteres. */
    public static final int LIMITE_SENHA = 128;

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int DIGITOS_DO_CPF = 11;
    private static final int TAMANHO_MINIMO_DA_SENHA = 6;
    private static final int CONTAS_RASTREADAS = 1000;

    private final UsuarioRepositorio repositorio;
    private final TrilhaDeAuditoria trilha;
    private final Clock relogio;
    // e-mail (minúsculas) -> falhas seguidas e fim do bloqueio; limitada para um atacante não encher a memória
    private final Map<String, Tentativas> tentativas = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Tentativas> eldest) {
            return size() > CONTAS_RASTREADAS;
        }
    };

    private static final class Tentativas {
        int falhas;
        Instant bloqueadoAte = Instant.MIN;
    }

    /**
     * Cria o serviço com o relógio do sistema.
     *
     * @param repositorio onde as contas são lidas e gravadas
     * @param trilha      onde logins e cadastros são registrados
     */
    public ContaService(UsuarioRepositorio repositorio, TrilhaDeAuditoria trilha) {
        this(repositorio, trilha, Clock.systemUTC());
    }

    /**
     * Cria o serviço com um relógio informado, o que permite testar o bloqueio sem esperar 30 segundos.
     *
     * @param repositorio onde as contas são lidas e gravadas
     * @param trilha      onde logins e cadastros são registrados
     * @param relogio     de onde vem a hora atual
     * @throws IllegalArgumentException se algum argumento for nulo
     */
    public ContaService(UsuarioRepositorio repositorio, TrilhaDeAuditoria trilha, Clock relogio) {
        if (repositorio == null || trilha == null || relogio == null) {
            throw new IllegalArgumentException("O repositório, a trilha de auditoria e o relógio são obrigatórios.");
        }
        this.repositorio = repositorio;
        this.trilha = trilha;
        this.relogio = relogio;
    }

    /**
     * Confere e-mail e senha.
     *
     * @param email e-mail digitado
     * @param senha senha digitada
     * @return o usuário (cliente ou administrador), ou vazio se o e-mail não existir ou a senha estiver errada
     *         (os dois casos são indistinguíveis de propósito, para não revelar quais e-mails existem)
     * @throws IllegalArgumentException             se o e-mail ou a senha estiverem em branco
     * @throws astra.excecoes.AcessoNegadoException se houve tentativas demais e o e-mail está bloqueado por um tempo
     * @throws PersistenciaException                se o banco falhar
     */
    public Optional<Usuario> autenticar(String email, String senha) throws PersistenciaException {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Preencha e-mail e senha.");
        }
        String chave = email.trim().toLowerCase(Locale.ROOT);
        exigirNaoBloqueado(chave, email);
        if (email.length() > LIMITE_EMAIL || senha.length() > LIMITE_SENHA) {
            registrarFalha(chave, email);   // entrada absurda: nem chega ao hash nem ao banco
            return Optional.empty();
        }
        Optional<Usuario> usuario = repositorio.autenticar(email.trim(), senha);
        if (usuario.isEmpty()) {
            registrarFalha(chave, email);
            return Optional.empty();
        }
        limparFalhas(chave);
        String perfil = usuario.get() instanceof Administrador ? "administrador" : "cliente";
        trilha.registrar(usuario.get().getId(), TrilhaDeAuditoria.LOGIN_OK, "perfil=" + perfil);
        return usuario;
    }

    /**
     * Cria a conta de um cliente novo.
     *
     * @param nome  nome completo (obrigatório)
     * @param email e-mail válido, ainda não cadastrado
     * @param cpf   CPF com 11 dígitos, com ou sem pontuação, ainda não cadastrado
     * @param senha senha com pelo menos 6 caracteres (gravada só como hash)
     * @return o cliente gravado, com o id gerado pelo banco
     * @throws IllegalArgumentException se algum dado for inválido (a mensagem diz qual)
     * @throws PersistenciaException    se o e-mail ou o CPF já estiverem cadastrados, ou se o banco falhar
     */
    public Cliente cadastrarCliente(String nome, String email, String cpf, String senha) throws PersistenciaException {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe seu nome completo.");
        }
        Validacao.exigirNoMaximo(nome, LIMITE_NOME, "O nome");
        Validacao.exigirNoMaximo(email, LIMITE_EMAIL, "O e-mail");
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Informe um e-mail válido, por exemplo nome@dominio.com.");
        }
        String digitos = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (digitos.length() != DIGITOS_DO_CPF) {
            throw new IllegalArgumentException("O CPF deve ter " + DIGITOS_DO_CPF + " dígitos.");
        }
        if (senha == null || senha.length() < TAMANHO_MINIMO_DA_SENHA) {
            throw new IllegalArgumentException("A senha deve ter pelo menos " + TAMANHO_MINIMO_DA_SENHA + " caracteres.");
        }
        Validacao.exigirNoMaximo(senha, LIMITE_SENHA, "A senha");
        Usuario salvo = repositorio.salvar(new Cliente(0, nome.trim(), email.trim(), "", digitos), senha);
        if (!(salvo instanceof Cliente)) {
            throw new IllegalStateException("O repositório devolveu um usuário que não é cliente.");
        }
        trilha.registrar(salvo.getId(), TrilhaDeAuditoria.CONTA_CRIADA, "perfil=cliente");
        return (Cliente) salvo;
    }

    // ------------------------------------------------------------------

    // Recusa o login enquanto o e-mail estiver bloqueado e diz quanto falta
    private synchronized void exigirNaoBloqueado(String chave, String emailDigitado) {
        Tentativas t = tentativas.get(chave);
        Instant agora = relogio.instant();
        if (t != null && t.bloqueadoAte.isAfter(agora)) {
            long restante = Math.max(1, Duration.between(agora, t.bloqueadoAte).toSeconds() + 1);
            trilha.registrar(null, TrilhaDeAuditoria.LOGIN_BLOQUEADO, "e-mail=" + emailDigitado);
            throw new AcessoNegadoException("Muitas tentativas de login. Aguarde " + restante
                    + " segundo(s) e tente de novo.");
        }
    }

    // Conta mais uma falha seguida; ao chegar no máximo, bloqueia por um tempo e recomeça a contagem
    private synchronized void registrarFalha(String chave, String emailDigitado) {
        Tentativas t = tentativas.computeIfAbsent(chave, k -> new Tentativas());
        t.falhas++;
        trilha.registrar(null, TrilhaDeAuditoria.LOGIN_FALHA,
                "e-mail=" + emailDigitado + " falhas seguidas=" + t.falhas + "/" + MAXIMO_DE_FALHAS);
        if (t.falhas >= MAXIMO_DE_FALHAS) {
            t.bloqueadoAte = relogio.instant().plusSeconds(SEGUNDOS_DE_BLOQUEIO);
            t.falhas = 0;
        }
    }

    private synchronized void limparFalhas(String chave) {
        tentativas.remove(chave);
    }
}