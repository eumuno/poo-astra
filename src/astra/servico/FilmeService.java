package astra.servico;

import astra.excecoes.PersistenciaException;
import astra.modelo.Administrador;
import astra.modelo.Filme;
import astra.persistencia.FilmeRepositorio;
import java.util.List;

/**
 * Casos de uso do catálogo de filmes: cadastrar, editar (inclusive ativar e desativar) e listar.
 *
 * Concentra as regras de validação, para que valham também fora da interface gráfica.
 * Depende apenas da interface {@link FilmeRepositorio}.
 */
public class FilmeService {

    private final FilmeRepositorio repositorio; //interface

    /**
     * Cria o serviço sobre o repositório de filmes recebido.
     *
     * @param repositorio onde os filmes são lidos e gravados
     */
    public FilmeService(FilmeRepositorio repositorio){
        this.repositorio = repositorio;
    }

    /**
     * Cadastra um filme novo.
     *
     * @param admin         administrador que está cadastrando (obrigatório)
     * @param titulo        título do filme (obrigatório e único, sem diferenciar maiúsculas)
     * @param duracaoMinutos duração em minutos (maior que zero)
     * @param classificacao classificação indicativa (obrigatória)
     * @param genero        gênero do filme (obrigatório)
     * @param diretor       diretor do filme (obrigatório)
     * @param ativo         se o filme aparece para o cliente
     * @param dublado       se a exibição é dublada
     * @param sinopse       resumo do filme (opcional)
     * @return o filme gravado, com o id gerado pelo banco
     * @throws IllegalArgumentException se algum dado for inválido ou já existir um filme com o mesmo título
     * @throws PersistenciaException    se o banco falhar
     */
    public Filme cadastrarFilme(Administrador admin, String titulo, int duracaoMinutos, Filme.ClassificacaoIndicativa classificacao, String genero, String diretor, boolean ativo, boolean dublado, String sinopse) throws PersistenciaException{
        validarDados(admin, titulo, duracaoMinutos, classificacao, genero, diretor);
        exigirTituloLivre(titulo, 0);
        return repositorio.salvar(new Filme(0, titulo.trim(), duracaoMinutos, classificacao, genero.trim(), diretor.trim(), ativo, dublado, textoOuVazio(sinopse)));
    }

    /**
     * Altera os dados de um filme já cadastrado. Para tirar um filme de cartaz sem apagá-lo
     * (preservando as sessões e vendas antigas), basta gravar com {@code ativo = false}.
     *
     * @param admin          administrador que está alterando (obrigatório)
     * @param filmeId        id do filme que será alterado
     * @param titulo         novo título (obrigatório e único, sem diferenciar maiúsculas)
     * @param duracaoMinutos nova duração em minutos (maior que zero)
     * @param classificacao  nova classificação indicativa (obrigatória)
     * @param genero         novo gênero (obrigatório)
     * @param diretor        novo diretor (obrigatório)
     * @param ativo          se o filme continua aparecendo para o cliente
     * @param dublado        se a exibição é dublada
     * @param sinopse        novo resumo (opcional)
     * @return o filme com os dados novos
     * @throws IllegalArgumentException se algum dado for inválido ou outro filme já tiver o mesmo título
     * @throws PersistenciaException    se o filme não existir ou o banco falhar
     */
    public Filme atualizarFilme(Administrador admin, int filmeId, String titulo, int duracaoMinutos, Filme.ClassificacaoIndicativa classificacao, String genero, String diretor, boolean ativo, boolean dublado, String sinopse) throws PersistenciaException{
        if (filmeId <= 0) {
            throw new IllegalArgumentException("Escolha um filme já cadastrado para editar.");
        }
        validarDados(admin, titulo, duracaoMinutos, classificacao, genero, diretor);
        exigirTituloLivre(titulo, filmeId);
        return repositorio.salvar(new Filme(filmeId, titulo.trim(), duracaoMinutos, classificacao, genero.trim(), diretor.trim(), ativo, dublado, textoOuVazio(sinopse)));
    }

    /**
     * Lista o catálogo completo de filmes.
     *
     * @return todos os filmes, ativos e inativos (a tela do administrador precisa ver todos)
     * @throws PersistenciaException se o banco falhar
     */
    public List<Filme> listarFilmes() throws PersistenciaException{
        return repositorio.listarTodos();
    }

    private void validarDados(Administrador admin, String titulo, int duracaoMinutos, Filme.ClassificacaoIndicativa classificacao, String genero, String diretor) {
        if(admin == null){
            throw new IllegalArgumentException("Só um administrador pode gerenciar filmes!");
        }
        if(titulo == null || titulo.isBlank()){
            throw new IllegalArgumentException("O título do filme é obrigatório.");
        }
        if(duracaoMinutos <= 0){
            throw new IllegalArgumentException("Duração do filme deve ser maior que zero.");
        }
        if(classificacao == null){
            throw new IllegalArgumentException("Escolha a classificação indicativa do filme.");
        }
        if(genero == null || genero.isBlank()){
            throw new IllegalArgumentException("O gênero do filme é obrigatório.");
        }
        if(diretor == null || diretor.isBlank()){
            throw new IllegalArgumentException("O diretor do filme é obrigatório.");
        }
    }

    // Dois filmes com o mesmo título (sem diferenciar maiúsculas) confundem o cliente na hora de escolher a sessão
    private void exigirTituloLivre(String titulo, int idIgnorado) throws PersistenciaException {
        for (Filme existente : repositorio.listarTodos()) {
            if (existente.getId() != idIgnorado && existente.getTitulo().trim().equalsIgnoreCase(titulo.trim())) {
                throw new IllegalArgumentException("Já existe um filme com o título \"" + existente.getTitulo() + "\".");
            }
        }
    }

    private String textoOuVazio(String texto) {
        return texto == null ? "" : texto.trim();
    }
}