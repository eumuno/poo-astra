package astra.servico;

import astra.excecoes.PersistenciaException;
import astra.modelo.Administrador;
import astra.modelo.Assento;
import astra.modelo.Filme;
import astra.modelo.Sala;
import astra.modelo.Sessao;
import astra.persistencia.SessaoRepositorio;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Casos de uso das sessões: consultar (para o cliente e para o administrador), cadastrar, alterar e cancelar.
 *
 * Regra central da administração: uma sessão que já vendeu ingresso não é alterada nem cancelada,
 * porque o preço e o horário combinados com quem comprou não podem mudar depois da venda.
 */
public class SessaoService {

    private final SessaoRepositorio repositorio;

    /**
     * Cria o serviço sobre o repositório de sessões recebido.
     *
     * @param repositorio onde as sessões são lidas e gravadas
     */
    public SessaoService(SessaoRepositorio repositorio){
        this.repositorio = repositorio;
    }

    /**
     * Cadastra uma sessão nova, com status agendada.
     *
     * @param admin      administrador que está cadastrando (obrigatório)
     * @param filme      filme exibido (obrigatório e ativo)
     * @param numeroSala número de uma sala já cadastrada
     * @param dataHora   precisa estar no futuro
     * @param precoBase  valor do ingresso inteiro (a meia é metade)
     * @return a sessão gravada, com o id gerado pelo banco
     * @throws IllegalArgumentException se algum dado for inválido ou o filme estiver inativo
     * @throws PersistenciaException    se a sala não existir, se ela já tiver sessão nesse horário ou se o banco falhar
     */
    public Sessao cadastrarSessao(Administrador admin, Filme filme, int numeroSala, LocalDateTime dataHora,
                                  double precoBase) throws PersistenciaException {
        validarDados(admin, filme, dataHora);
        return repositorio.salvar(novaSessao(0, filme, numeroSala, dataHora, precoBase));
    }

    /**
     * Altera filme, sala, data e hora e preço de uma sessão agendada que ainda não vendeu ingresso.
     *
     * @param admin      administrador que está alterando (obrigatório)
     * @param sessaoId   id da sessão que será alterada
     * @param filme      novo filme (obrigatório e ativo)
     * @param numeroSala número da nova sala (já cadastrada)
     * @param dataHora   nova data e hora (precisa estar no futuro)
     * @param precoBase  novo valor do ingresso inteiro
     * @return a sessão com os dados novos
     * @throws IllegalArgumentException se algum dado for inválido, o filme estiver inativo ou a sessão não existir
     * @throws IllegalStateException    se a sessão não estiver agendada ou já tiver ingressos vendidos
     * @throws PersistenciaException    se a sala não existir, se ela já tiver outra sessão nesse horário ou se o banco falhar
     */
    public Sessao atualizarSessao(Administrador admin, int sessaoId, Filme filme, int numeroSala,
                                  LocalDateTime dataHora, double precoBase) throws PersistenciaException {
        validarDados(admin, filme, dataHora);
        Sessao existente = buscarSessaoAlteravel(sessaoId, "alterada");
        return repositorio.atualizar(novaSessao(existente.getId(), filme, numeroSala, dataHora, precoBase));
    }

    /**
     * Cancela uma sessão agendada que ainda não vendeu ingresso. Ela deixa de aparecer para o cliente,
     * mas continua registrada.
     *
     * @param admin    administrador que está cancelando (obrigatório)
     * @param sessaoId id da sessão que será cancelada
     * @return a sessão já cancelada
     * @throws IllegalArgumentException se a sessão não existir
     * @throws IllegalStateException    se a sessão já estiver cancelada ou tiver ingressos vendidos
     * @throws PersistenciaException    se o banco falhar
     */
    public Sessao cancelarSessao(Administrador admin, int sessaoId) throws PersistenciaException {
        if (admin == null) {
            throw new IllegalArgumentException("Só um administrador pode cancelar sessões!");
        }
        Sessao existente = buscarSessaoAlteravel(sessaoId, "cancelada");
        existente.cancelar();
        return repositorio.atualizar(existente);
    }

    /**
     * Lista todas as sessões para a administração.
     *
     * @return todas as sessões, inclusive canceladas e já realizadas (visão do administrador)
     * @throws PersistenciaException se o banco falhar
     */
    public List<Sessao> listarSessoes() throws PersistenciaException {
        return repositorio.listarTodos();
    }

    /**
     * Lista as sessões que o cliente pode comprar.
     *
     * @return as sessões que o cliente pode comprar: agendadas e de um filme ativo
     * @throws PersistenciaException se o banco falhar
     */
    public List<Sessao> listarSessoesDisponiveis() throws PersistenciaException{
        List<Sessao> disponiveis = new ArrayList<>();
        for(Sessao s : repositorio.listarTodos()){
            if(s.estaDisponivel() && s.getFilme().isAtivo()){
                disponiveis.add(s);
            }
        }
        return disponiveis;
    }

    /**
     * Conta os assentos já vendidos de uma sessão.
     *
     * @param sessao sessão carregada do banco (que traz os assentos já vendidos)
     * @return quantos assentos dela já foram vendidos
     */
    public int contarIngressosVendidos(Sessao sessao) {
        int vendidos = 0;
        for (Assento assento : sessao.getSala().getAssentos()) {
            if (!sessao.assentoEstaDisponivel(assento.getNumero())) {
                vendidos++;
            }
        }
        return vendidos;
    }

    private void validarDados(Administrador admin, Filme filme, LocalDateTime dataHora) {
        if (admin == null) {
            throw new IllegalArgumentException("Só um administrador pode gerenciar sessões!");
        }
        if (filme == null) {
            throw new IllegalArgumentException("Escolha o filme da sessão.");
        }
        if (!filme.isAtivo()) {
            throw new IllegalArgumentException("O filme \"" + filme.getTitulo() + "\" não está ativo.");
        }
        if (dataHora == null || !dataHora.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("A sessão precisa ser numa data futura.");
        }
    }

    // O repositório só usa o número da sala para achá-la no banco; os assentos vêm de lá
    private Sessao novaSessao(int id, Filme filme, int numeroSala, LocalDateTime dataHora, double precoBase) {
        Sala sala = new Sala(numeroSala, new ArrayList<>());
        return new Sessao(id, filme, sala, dataHora, Sessao.StatusSessao.AGENDADA, precoBase);
    }

    // Carrega a sessão e confere se ela ainda pode mudar: agendada e sem nenhum ingresso vendido
    private Sessao buscarSessaoAlteravel(int sessaoId, String acao) throws PersistenciaException {
        Sessao existente = repositorio.buscarPorId(sessaoId).orElseThrow(() ->
                new IllegalArgumentException("A sessão escolhida não existe mais."));
        if (existente.getStatus() != Sessao.StatusSessao.AGENDADA) {
            throw new IllegalStateException("Só sessões agendadas podem ser " + acao + ".");
        }
        int vendidos = contarIngressosVendidos(existente);
        if (vendidos > 0) {
            throw new IllegalStateException("Esta sessão já tem " + vendidos
                    + " ingresso(s) vendido(s) e não pode ser " + acao + ".");
        }
        return existente;
    }
}