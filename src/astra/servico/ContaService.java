package astra.servico;

import astra.excecoes.PersistenciaException;
import astra.modelo.Cliente;
import astra.modelo.Usuario;
import astra.persistencia.UsuarioRepositorio;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Casos de uso das contas de acesso: entrar no sistema e criar a conta de um cliente.
 *
 * <p>As regras de validação do cadastro (nome, e-mail, CPF e senha) ficam aqui, e não na tela,
 * para valerem em qualquer interface e poderem ser testadas sem abrir janela. Depende apenas
 * da interface {@link UsuarioRepositorio}.</p>
 */
public class ContaService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int DIGITOS_DO_CPF = 11;
    private static final int TAMANHO_MINIMO_DA_SENHA = 6;

    private final UsuarioRepositorio repositorio;

    /**
     * Cria o serviço sobre o repositório de usuários recebido.
     *
     * @param repositorio onde as contas são lidas e gravadas
     */
    public ContaService(UsuarioRepositorio repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("O repositório de usuários é obrigatório.");
        }
        this.repositorio = repositorio;
    }

    /**
     * Confere e-mail e senha.
     *
     * @param email e-mail digitado
     * @param senha senha digitada
     * @return o usuário (cliente ou administrador), ou vazio se o e-mail não existir ou a senha estiver errada
     *         (os dois casos são indistinguíveis de propósito, para não revelar quais e-mails existem)
     * @throws IllegalArgumentException se o e-mail ou a senha estiverem em branco
     * @throws PersistenciaException    se o banco falhar
     */
    public Optional<Usuario> autenticar(String email, String senha) throws PersistenciaException {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Preencha e-mail e senha.");
        }
        return repositorio.autenticar(email.trim(), senha);
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
        Usuario salvo = repositorio.salvar(new Cliente(0, nome.trim(), email.trim(), "", digitos), senha);
        if (!(salvo instanceof Cliente)) {
            throw new IllegalStateException("O repositório devolveu um usuário que não é cliente.");
        }
        return (Cliente) salvo;
    }
}