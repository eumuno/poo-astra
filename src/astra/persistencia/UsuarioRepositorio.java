package astra.persistencia;

import astra.excecoes.PersistenciaException;
import astra.modelo.Usuario;

import java.util.Optional;

/**
 * Contrato de acesso aos usuários (clientes e administradores).
 *
 * A senha nunca trafega nem é guardada em texto puro: no cadastro ela vira hash
 * (CriptografiaSenha) e no login só se compara o hash.
 */
public interface UsuarioRepositorio {

    /**
     * Cadastra um usuário novo (id 0). O banco gera o id.
     *
     * @param senhaPura a senha digitada; o repositório grava só o hash dela
     * @return o usuário gravado, já com o id definitivo
     * @throws PersistenciaException se o e-mail já estiver cadastrado ou se faltar um dado obrigatório (ex.: CPF do cliente)
     */
    Usuario salvar(Usuario usuario, String senhaPura) throws PersistenciaException;

    /** O usuário com esse id (Cliente ou Administrador), ou vazio se não existir. */
    Optional<Usuario> buscarPorId(int id) throws PersistenciaException;

    /** O usuário com esse e-mail, ou vazio se não existir. */
    Optional<Usuario> buscarPorEmail(String email) throws PersistenciaException;

    /**
     * Login: devolve o usuário se o e-mail existir e a senha conferir com o hash gravado.
     * Vazio nos dois casos de erro (e-mail inexistente OU senha errada), para a tela não
     * revelar qual dos dois falhou.
     */
    Optional<Usuario> autenticar(String email, String senhaPura) throws PersistenciaException;
}
