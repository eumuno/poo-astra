package astra.servico;

import astra.excecoes.PersistenciaException;
import astra.modelo.Administrador;
import astra.modelo.Filme;
import astra.modelo.Sala;
import astra.modelo.Sessao;
import astra.persistencia.SessaoRepositorio;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Consulta as sessões, cadastro da sessão passa pelo admin e depois entra aqui
public class SessaoService {

    private final SessaoRepositorio repositorio;

    public SessaoService(SessaoRepositorio repositorio){
        this.repositorio = repositorio;
    }

    public Sessao cadastrarSessao(Administrador admin, Filme filme, int numeroSala, LocalDateTime dataHora,
                                  double precoBase) throws PersistenciaException {
        if (admin == null) {
            throw new IllegalArgumentException("Só um administrador pode cadastrar sessões!");
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
        // o repositório só usa o número da sala para achá-la no banco; os assentos vêm de lá
        Sala sala = new Sala(numeroSala, new ArrayList<>());
        return repositorio.salvar(new Sessao(0, filme, sala, dataHora, Sessao.statusSessao.AGENDADA, precoBase));
    }

    public List<Sessao> listarSessoesDisponiveis() throws PersistenciaException{
        List<Sessao> disponiveis = new ArrayList<>();
        for(Sessao s : repositorio.listarTodos()){
            if(s.estaDisponivel()){
                disponiveis.add(s);
            }
        }
        return disponiveis;
    }
}
