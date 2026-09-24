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

    // MÉTODOS (a lógica será criada dps)
    // public void cadastrarFilme(...) {...}
    // public void cadastrarSessao(...) {...}
    // public void cadastrarProduto(...) {...}

}
