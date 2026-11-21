package astra.servico;

import astra.excecoes.PersistenciaException;
import astra.modelo.Administrador;
import astra.modelo.RegistroDeAuditoria;
import astra.persistencia.AuditoriaRepositorio;

import java.util.List;

/**
 * Registra as ações importantes do sistema (logins, cadastros, alterações administrativas e compras) e
 * permite que um administrador as consulte. É o que impede alguém de negar depois que fez algo (não repúdio).
 *
 * <p>Duas decisões de projeto:</p>
 * <ul>
 *   <li><b>Gravar nunca derruba a operação.</b> Se o registro falhar, o erro vai para o console e a ação
 *       segue. É um equilíbrio entre disponibilidade e rastreabilidade; num sistema mais crítico, a falha de
 *       auditoria deveria cancelar a operação.</li>
 *   <li><b>O texto é limpo antes de gravar.</b> Quebras de linha e outros caracteres de controle viram espaço
 *       e o texto é cortado em {@value #TAMANHO_MAXIMO_DO_DETALHE} caracteres, para que um valor digitado pelo
 *       usuário não consiga forjar linhas no registro nem encher o banco.</li>
 * </ul>
 */
public class TrilhaDeAuditoria {

    /** Login bem-sucedido. */
    public static final String LOGIN_OK = "LOGIN_OK";
    /** Tentativa de login com e-mail ou senha incorretos. */
    public static final String LOGIN_FALHA = "LOGIN_FALHA";
    /** Tentativa de login recusada por excesso de tentativas. */
    public static final String LOGIN_BLOQUEADO = "LOGIN_BLOQUEADO";
    /** Conta de cliente criada. */
    public static final String CONTA_CRIADA = "CONTA_CRIADA";
    /** Filme cadastrado. */
    public static final String FILME_CRIADO = "FILME_CRIADO";
    /** Filme alterado (inclusive ativado ou desativado). */
    public static final String FILME_EDITADO = "FILME_EDITADO";
    /** Produto cadastrado. */
    public static final String PRODUTO_CRIADO = "PRODUTO_CRIADO";
    /** Dados de um produto alterados. */
    public static final String PRODUTO_EDITADO = "PRODUTO_EDITADO";
    /** Estoque de um produto reposto. */
    public static final String ESTOQUE_REPOSTO = "ESTOQUE_REPOSTO";
    /** Sessão cadastrada. */
    public static final String SESSAO_CRIADA = "SESSAO_CRIADA";
    /** Sessão alterada. */
    public static final String SESSAO_EDITADA = "SESSAO_EDITADA";
    /** Sessão cancelada. */
    public static final String SESSAO_CANCELADA = "SESSAO_CANCELADA";
    /** Pedido marcado como pronto para retirada. */
    public static final String PEDIDO_PRONTO = "PEDIDO_PRONTO";
    /** Pedido marcado como retirado. */
    public static final String PEDIDO_RETIRADO = "PEDIDO_RETIRADO";
    /** Compra paga e gravada. */
    public static final String COMPRA_CONCLUIDA = "COMPRA_CONCLUIDA";
    /** Pagamento aprovado cuja gravação falhou e foi desfeito (estornado). */
    public static final String VENDA_DESFEITA = "VENDA_DESFEITA";

    /** Tamanho máximo do texto de detalhe gravado. */
    public static final int TAMANHO_MAXIMO_DO_DETALHE = 500;

    private final AuditoriaRepositorio repositorio;
    private final ControleDeAcesso acesso;

    /**
     * Cria a trilha sobre o repositório recebido.
     *
     * @param repositorio onde os registros são gravados
     * @param acesso      usado para permitir a consulta só a administradores
     * @throws IllegalArgumentException se algum argumento for nulo
     */
    public TrilhaDeAuditoria(AuditoriaRepositorio repositorio, ControleDeAcesso acesso) {
        if (repositorio == null || acesso == null) {
            throw new IllegalArgumentException("O repositório de auditoria e o controle de acesso são obrigatórios.");
        }
        this.repositorio = repositorio;
        this.acesso = acesso;
    }

    /**
     * Registra uma ação. Nunca lança exceção: se o registro falhar, a falha vai para o console.
     *
     * @param usuarioId id de quem fez a ação, ou {@code null} se não há usuário identificado
     * @param acao      o tipo da ação (use as constantes desta classe)
     * @param detalhe   o que foi feito; nunca inclua senhas
     */
    public void registrar(Integer usuarioId, String acao, String detalhe) {
        try {
            repositorio.registrar(usuarioId, acao, limpar(detalhe));
        } catch (PersistenciaException | RuntimeException e) {
            System.err.println("Falha ao gravar a auditoria (" + acao + "): " + e.getMessage());
        }
    }

    /**
     * Lista os registros mais recentes. Só administradores podem consultar a trilha.
     *
     * @param admin  quem está consultando
     * @param limite quantos registros devolver (de 1 a 1000)
     * @return os registros, do mais novo para o mais antigo
     * @throws astra.excecoes.AcessoNegadoException se quem consulta não for administrador
     * @throws PersistenciaException                se o banco falhar
     */
    public List<RegistroDeAuditoria> listarRecentes(Administrador admin, int limite) throws PersistenciaException {
        acesso.exigirAdministrador(admin);
        return repositorio.listarRecentes(limite);
    }

    // Caracteres de controle (quebra de linha, tabulação...) viram espaço, e o texto é cortado no limite
    private String limpar(String detalhe) {
        String texto = detalhe == null ? "" : detalhe.replaceAll("\\p{Cntrl}", " ").strip();
        if (texto.length() > TAMANHO_MAXIMO_DO_DETALHE) {
            texto = texto.substring(0, TAMANHO_MAXIMO_DO_DETALHE - 1) + "…";
        }
        return texto.isEmpty() ? "-" : texto;
    }
}