package astra.modelo;


public class Filme {

    // ATRIBUTOS
    private int id;
    private String titulo;
    private int duracaoMinutos;
    private classificacaoIndicativa classificacaoIndicativa;
    private String genero;
    private String diretor;
    private boolean dublado;
    private boolean ativo;

    //ENUM
    public enum classificacaoIndicativa{
        LIVRE("Livre."),
        DEZ("10 anos."),
        DOZE("12 anos."),
        QUATORZE("14 anos."),
        DEZESSEIS("16 anos."),
        DEZOITO("18 anos.");

        private String descricao;
        classificacaoIndicativa(String descricao){
            this.descricao = descricao;
        }
        public String getDescricao(){
            return descricao;
        }
    }

    //CONSTRUTOR
    public Filme(int id, String titulo, int duracaoMinutos, classificacaoIndicativa classificacaoIndicativa, String genero, String diretor, boolean ativo, boolean dublado) {
        this.id = id;
        this.titulo = titulo;
        this.duracaoMinutos = duracaoMinutos;
        this.classificacaoIndicativa = classificacaoIndicativa;
        this.genero = genero;
        this.diretor = diretor;
        this.ativo = ativo;
    }

    // MÉTODOS
    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public classificacaoIndicativa getClassificacaoIndicativa() {
        return classificacaoIndicativa;
    }

    public String getGenero() {
        return genero;
    }

    public String getDiretor() {
        return diretor;
    }

    public boolean isDublado() {
        return dublado;
    }

    public boolean isAtivo() {
        return ativo;
    }

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
        "\nDuracao minutos: " + duracaoMinutos +
        "\nGênero: " + genero +
        "\nDiretor: " + diretor +
        "\nAtivo: " + ativo;
    }

}
