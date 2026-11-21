package astra.servico;

import astra.excecoes.AcessoNegadoException;
import astra.excecoes.PersistenciaException;
import astra.modelo.Administrador;
import astra.modelo.Usuario;
import astra.persistencia.UsuarioRepositorio;

/**
 * Decide se quem pede uma operação administrativa realmente é um administrador.
 *
 * <p>Antes, os serviços só conferiam que o objeto {@link Administrador} <b>não era nulo</b>: qualquer objeto
 * criado em código, ou um que sobrou na memória depois de a conta mudar, passava. Agora a conferência vai ao
 * banco, a fonte da verdade: o usuário precisa existir <b>e</b> ser administrador naquele momento. Quem usa
 * os serviços não consegue ganhar privilégio só montando o objeto certo.</p>
 *
 * <p>Limite assumido: o sistema não tem sessões nem tokens, então a identidade de quem fez login é a que a tela
 * guarda. Esta classe protege contra identidades forjadas ou desatualizadas, não contra quem controla o
 * processo.</p>
 */
public class ControleDeAcesso {

    private final UsuarioRepositorio usuarios;

    /**
     * Cria o controle sobre o repositório de usuários recebido.
     *
     * @param usuarios de onde se consulta o perfil real de cada conta
     * @throws IllegalArgumentException se o repositório for nulo
     */
    public ControleDeAcesso(UsuarioRepositorio usuarios) {
        if (usuarios == null) {
            throw new IllegalArgumentException("O repositório de usuários é obrigatório.");
        }
        this.usuarios = usuarios;
    }

    /**
     * Exige que a conta informada exista no banco como administrador.
     *
     * @param admin quem está pedindo a operação
     * @throws AcessoNegadoException se não houver usuário, a conta não existir ou não for de administrador
     * @throws PersistenciaException se o banco falhar
     */
    public void exigirAdministrador(Administrador admin) throws PersistenciaException {
        if (admin == null) {
            throw new AcessoNegadoException("Só um administrador pode realizar esta operação.");
        }
        Usuario real = usuarios.buscarPorId(admin.getId()).orElse(null);
        if (!(real instanceof Administrador)) {
            throw new AcessoNegadoException("Só um administrador pode realizar esta operação.");
        }
    }
}