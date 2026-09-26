package astra.modelo;

public abstract class Usuario {

    // ATRIBUTOS
    private int id;
    private String nome;
    private String email;
    private String senha;

    // CONSTRUTOR (usando this p inicializar dados obrigatórios)
    public Usuario(int id, String nome, String email, String senha) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    // METODO ABSTRATO (p sublcasses implementarem)
    public abstract String getTipoUsuario();

    // GETTERS
    public int getId() {return id;}
    public String getNome() {return nome;}
    public String getEmail() {return email;}

}
