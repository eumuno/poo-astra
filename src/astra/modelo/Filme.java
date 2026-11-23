package astra.modelo;

/**
 * Filme do catálogo do cinema Astra: guarda os dados de apresentação (título, duração,
 * gênero, diretor, sinopse) e a classificação indicativa. Um filme inativo continua
 * cadastrado, mas deixa de ser oferecido.
 */
public class Filme {

    // ATRIBUTOS
    private int id;
    private String titulo;
    private int duracaoMinutos;
    private ClassificacaoIndicativa classificacaoIndicativa;
    private String genero;
    private String diretor;
    private boolean ativo;
    private boolean dublado;
    private String sinopse;

    //ENUM
    /** Faixa etária indicada para assistir ao filme. */
    public enum ClassificacaoIndicativa {
        /** Livre para todos os públicos. */
        LIVRE("Livre."),
        /** Não recomendado para menores de 10 anos. */
        DEZ("10 anos."),
        /** Não recomendado para menores de 12 anos. */
        DOZE("12 anos."),
        /** Não recomendado para menores de 14 anos. */
        QUATORZE("14 anos."),
        /** Não recomendado para menores de 16 anos. */
        DEZESSEIS("16 anos."),
        /** Não recomendado para menores de 18 anos. */
        DEZOITO("18 anos.");

        private String descricao;

        /**
         * Cria uma classificação com o texto que aparece para o usuário.
         *
         * @param descricao texto da faixa etária, como "12 anos."
         */
        ClassificacaoIndicativa(String descricao){
            this.descricao = descricao;
        }

        /**
         * Retorna o texto da faixa etária.
         *
         * @return a descrição da classificação, como "12 anos."
         */
        public String getDescricao(){
            return descricao;
        }
    }

    //CONSTRUTOR
    /**
     * Cria um filme com todos os seus dados.
     *
     * @param id                      identificador do filme (0 enquanto não foi gravado no banco)
     * @param titulo                  título do filme
     * @param duracaoMinutos          duração total, em minutos
     * @param classificacaoIndicativa faixa etária indicada
     * @param genero                  gênero do filme, como "Drama" ou "Comédia"
     * @param diretor                 nome do diretor
     * @param ativo                   {@code true} se o filme está disponível no catálogo
     * @param dublado                 {@code true} se a sessão é dublada
     * @param sinopse                 breve resumo da história
     */
    public Filme(int id, String titulo, int duracaoMinutos, ClassificacaoIndicativa classificacaoIndicativa, String genero, String diretor, boolean ativo, boolean dublado, String sinopse) {
        this.id = id;
        this.titulo = titulo;
        this.duracaoMinutos = duracaoMinutos;
        this.classificacaoIndicativa = classificacaoIndicativa;
        this.genero = genero;
        this.diretor = diretor;
        this.ativo = ativo;
        this.dublado = dublado;
        this.sinopse = sinopse;
    }

    // MÉTODOS
    /**
     * Retorna o identificador do filme.
     *
     * @return o identificador do filme (0 se ainda não foi gravado no banco)
     */
    public int getId() {
        return id;
    }

    /**
     * Retorna o título do filme.
     *
     * @return o título do filme
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Retorna a duração do filme.
     *
     * @return a duração, em minutos
     */
    public int getDuracaoMinutos() {
        return duracaoMinutos;
    }

    /**
     * Retorna a classificação indicativa do filme.
     *
     * @return a faixa etária indicada
     */
    public ClassificacaoIndicativa getClassificacaoIndicativa() {
        return classificacaoIndicativa;
    }

    /**
     * Retorna o gênero do filme.
     *
     * @return o gênero do filme
     */
    public String getGenero() {
        return genero;
    }

    /**
     * Retorna o diretor do filme.
     *
     * @return o nome do diretor
     */
    public String getDiretor() {
        return diretor;
    }

    /**
     * Informa se o filme está ativo no catálogo.
     *
     * @return {@code true} se o filme está ativo
     */
    public boolean isAtivo() {
        return ativo;
    }

    /**
     * Informa se o filme é dublado.
     *
     * @return {@code true} se o filme é dublado
     */
    public boolean isDublado() {
        return dublado;
    }

    /**
     * Retorna a sinopse do filme.
     *
     * @return o resumo da história
     */
    public String getSinopse() {
        return sinopse;
    }

    /**
     * Monta um texto com todos os dados do filme, um por linha.
     *
     * @return o resumo do filme (id, título, duração, gênero, classificação, diretor, se é
     *         dublado, se está ativo e a sinopse)
     */
    public String gerarResumo(){
        String textoDublado;
        if(dublado){
            textoDublado = "Sim.";
        } else{
            textoDublado="Não.";
        }

        String textoAtivo;
        if(ativo){
            textoAtivo = "Ativo.";
        }
        else{
            textoAtivo = "Inativo.";
        }

        return "ID: " + id +
        "\nTítulo: " + titulo +
        "\nDuracao minutos: " + duracaoMinutos + " minutos" +
        "\nGênero: " + genero +
        "\nClassificação indicativa: " + classificacaoIndicativa.getDescricao() +
        "\nDiretor: " + diretor +
        "\nDublado: " + textoDublado +
        "\nAtivo: " + textoAtivo +
        "\nSinopse: " + sinopse;
    }

    /**
     * Texto curto usado para exibir o filme em listas e caixas de seleção.
     *
     * @return o título seguido da duração, como "O Auto da Compadecida (104 min)"
     */
    @Override
    public String toString() {
        return titulo + " (" + duracaoMinutos + " min)";
    }

}
