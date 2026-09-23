package astra.modelo;

public class Cliente extends Usuario {

    // ATRIBUTO (exclusivo do cliente)
    private String cpf;

    // CONSTRUTOR
    public Cliente(int id, String nome, String email, String senha, String cpf) {
        super(id, nome, email, senha); // chama o construtor de usuario
        this.cpf = cpf;
    }

    // SOBRESCRITA DO MÉTODO ABSTRATO
    @Override
    public String getTipoUsuario() {
        return "Cliente";
    }

    // MÉTODOS (a lógica será criada dps)
    // public ReservaIngresso criarReserva() {...}
    // public Pedido criarPedido() {...}

}