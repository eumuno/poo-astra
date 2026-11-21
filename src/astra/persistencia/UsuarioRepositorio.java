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
     * @param usuario   o usuário novo (Cliente ou Administrador)
     * @param senhaPura a senha digitada; o repositório grava só o hash dela
     * @return o usuário gravado, já com o id definitivo
     * @throws PersistenciaException se o e-mail já estiver cadastrado ou se faltar um dado obrigatório (ex.: CPF do cliente)
     */
    Usuario salvar(Usuario usuario, String senhaPura) throws PersistenciaException;

    /**
     * Busca um usuário pelo id.
     *
     * @param id identificador do usuário
     * @return o usuário (Cliente ou Administrador), ou vazio se não existir
     * @throws PersistenciaException se o banco falhar
     */
    Optional<Usuario> buscarPorId(int id) throws PersistenciaException;

    /**
     * Busca um usuário pelo e-mail, sem diferenciar maiúsculas e minúsculas.
     *
     * @param email o e-mail procurado
     * @return o usuário, ou vazio se não existir
     * @throws PersistenciaException se o banco falhar
     */
    Optional<Usuario> buscarPorEmail(String email) throws PersistenciaException;

    /**
     * Login: devolve o usuário se o e-mail existir e a senha conferir com o hash gravado.
     * Vazio nos dois casos de erro (e-mail inexistente OU senha errada), para a tela não
     * revelar qual dos dois falhou. Se a conta ainda tem o hash antigo (SHA-256 sem sal), o hash é
     * trocado pelo formato atual no mesmo login.
     *
     * @param email     o e-mail digitado
     * @param senhaPura a senha digitada
     * @return o usuário autenticado, ou vazio se o e-mail não existir ou a senha estiver errada
     * @throws PersistenciaException se o banco falhar
     */
    Optional<Usuario> autenticar(String email, String senhaPura) throws PersistenciaException;
}