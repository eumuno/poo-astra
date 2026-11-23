package astra.modelo;

/**
 * Pessoa que usa o sistema Astra. É a base comum de {@link Cliente} e {@link Administrador}:
 * guarda os dados de identificação e deixa para cada subclasse dizer qual é o seu tipo.
 */
public abstract class Usuario {

    // ATRIBUTOS
    private int id;
    private String nome;
    private String email;
    private String senha;

    // CONSTRUTOR (usando this p inicializar dados obrigatórios)
    /**
     * Cria um usuário com os dados de identificação.
     *
     * @param id    identificador do usuário (0 enquanto não foi gravado no banco)
     * @param nome  nome completo
     * @param email e-mail usado para entrar no sistema
     * @param senha senha do usuário
     */
    public Usuario(int id, String nome, String email, String senha) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    // METODO ABSTRATO (p sublcasses implementarem)
    /**
     * Diz qual é o tipo de usuário; cada subclasse devolve o seu.
     *
     * @return o nome do tipo, como "Cliente" ou "Administrador"
     */
    public abstract String getTipoUsuario();

    // GETTERS
    /**
     * Retorna o identificador do usuário.
     *
     * @return o identificador do usuário (0 se ainda não foi gravado no banco)
     */
    public int getId() {return id;}

    /**
     * Retorna o nome do usuário.
     *
     * @return o nome completo
     */
    public String getNome() {return nome;}

    /**
     * Retorna o e-mail do usuário.
     *
     * @return o e-mail usado para entrar no sistema
     */
    public String getEmail() {return email;}

}
