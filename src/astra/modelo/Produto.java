package astra.modelo;

public class Produto {

    // ATRIBUTOS
    private int id;
    private String nome;
    private String descricao;
    private double preco;
    private int estoque;
    private categoriaProduto categoria;

    // ENUM
    public enum categoriaProduto {
        PIPOCA,
        BEBIDA,
        DOCE,
        COMBO
    }

    // MÉTODOS
    public void adicionarEstoque(int quantidade)
    public void retirarEstoque(int quantidade)
    public boolean possuiEstoque(int quantidade)

}
