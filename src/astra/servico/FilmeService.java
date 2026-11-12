package astra.servico;

import astra.excecoes.PersistenciaException;
import astra.modelo.Administrador;
import astra.modelo.Filme;
import astra.persistencia.FilmeRepositorio;
import java.util.List;

public class FilmeService {

    private final FilmeRepositorio repositorio; //interface

    public FilmeService(FilmeRepositorio repositorio){
        this.repositorio = repositorio;
    }

    public Filme cadastrarFilme(Administrador admin, String titulo, int duracaoMinutos, Filme.ClassificacaoIndicativa classificacao, String genero, String diretor, boolean ativo, boolean dublado, String sinopse) throws PersistenciaException{
        if(admin == null){
            throw new IllegalArgumentException("Só um administrador pode cadastrar filmes!");
        }
        if(titulo == null || titulo.isBlank()){
            throw new IllegalArgumentException("O título do filme é obrigatório.");
        }
        if(duracaoMinutos <= 0){
            throw new IllegalArgumentException("Duração do filme deve ser maior que zero.");
        }
        Filme novo = new Filme(0, titulo, duracaoMinutos, classificacao, genero, diretor, ativo, dublado, sinopse);
        return repositorio.salvar(novo);
    }

    public List<Filme> listarFilmes() throws PersistenciaException{
        return repositorio.listarTodos();
    }
}
