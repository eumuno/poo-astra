package astra.modelo;

public class Ingresso {

    // ATRIBUTOS
    private int id;
    private Filme filme;
    private Sessao sessao;
    private Assento assento;
    private Cliente cliente;
    private double valorReferencia = 15; //É uma simplificação professor :)
    private  TipoIngresso tipoIngresso;

    //construtor com os atributos aqui
    // ENUM
    public enum TipoIngresso {
        INTEIRA,
        MEIA,
        CORTESIA
    }

    // MÉTODOS


}
