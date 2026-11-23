package astra.modelo;

/**
 * Usuário que gerencia o cinema: cadastra e edita filmes, produtos e sessões e acompanha os
 * pedidos. Usa os mesmos dados de identificação de {@link Usuario}.
 */
public class Administrador extends Usuario {

    // CONSTRUTOR
    /**
     * Cria um administrador.
     *
     * @param id    identificador do administrador (0 enquanto não foi gravado no banco)
     * @param nome  nome completo
     * @param email e-mail usado para entrar no sistema
     * @param senha senha do administrador
     */
    public Administrador(int id, String nome, String email, String senha) {
        super(id, nome, email, senha);
    }

    // SOBRESCRITA DO METODO ABSTRATO
    /**
     * Informa o tipo deste usuário.
     *
     * @return o texto "Administrador"
     */
    @Override
    public String getTipoUsuario() {
        return "Administrador";
    }

    // OUTROS MÉTODOS COMO CADASTRAR FILME, CADASTRAR SESSAO E CADASTRAR PRODUTO ESTARÃO NO CINEMASERVICE

}
