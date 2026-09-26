package astra.modelo;

public class Cliente extends Usuario {

    // ATRIBUTO (exclusivo do cliente)
    private String cpf;

    // CONSTRUTOR
    public Cliente(int id, String nome, String email, String senha, String cpf) {
        super(id, nome, email, senha); // chama o construtor de usuario
        this.cpf = cpf;
    }

    // SOBRESCRITA DO METODO ABSTRATO
    @Override
    public String getTipoUsuario() {
        return "Cliente";
    }

    // OUTROS MÉTODOS DE COMPRAR INGRESSO E FAZER PEDIDO ESTARÃO NO CINEMASERVICE (eu acho)

}