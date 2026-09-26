package astra.modelo;

public class Administrador extends Usuario {

    // CONSTRUTOR
    public Administrador(int id, String nome, String email, String senha) {
        super(id, nome, email, senha);
    }

    // SOBRESCRITA DO METODO ABSTRATO
    @Override
    public String getTipoUsuario() {
        return "Administrador";
    }

    // OUTROS MÉTODOS COMO CADASTRAR FILME, CADASTRAR SESSAO E CADASTRAR PRODUTO ESTARÃO NO CINEMASERVICE (eu acho)

}
