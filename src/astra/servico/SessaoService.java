package astra.servico;

import astra.excecoes.PersistenciaException;
import astra.modelo.Sessao;
import astra.persistencia.SessaoRepositorio;
import java.util.ArrayList;
import java.util.List;

// Consulta as sessões, cadastro da sessão passa pelo admin e depois entra aqui
public class SessaoService {

    private final SessaoRepositorio repositorio;

    public SessaoService(SessaoRepositorio repositorio){
        this.repositorio = repositorio;
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
