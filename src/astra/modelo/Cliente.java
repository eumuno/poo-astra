package astra.modelo;

/**
 * Usuário que compra ingressos e produtos da bomboniere. Além dos dados de {@link Usuario},
 * guarda o CPF.
 */
public class Cliente extends Usuario {

    // ATRIBUTO (exclusivo do cliente)
    private String cpf;

    // CONSTRUTOR
    /**
     * Cria um cliente.
     *
     * @param id    identificador do cliente (0 enquanto não foi gravado no banco)
     * @param nome  nome completo
     * @param email e-mail usado para entrar no sistema
     * @param senha senha do cliente
     * @param cpf   CPF do cliente
     */
    public Cliente(int id, String nome, String email, String senha, String cpf) {
        super(id, nome, email, senha); // chama o construtor de usuario
        this.cpf = cpf;
    }

    // SOBRESCRITA DO METODO ABSTRATO
    /**
     * Informa o tipo deste usuário.
     *
     * @return o texto "Cliente"
     */
    @Override
    public String getTipoUsuario() {
        return "Cliente";
    }

    // GETTER
    /**
     * Retorna o CPF do cliente.
     *
     * @return o CPF do cliente
     */
    public String getCpf() {
        return cpf;
    }

}