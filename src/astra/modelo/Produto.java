package astra.modelo;

public class Produto {

    // ATRIBUTOS
    private int id;
    private String nome;
    private String descricao;
    private double preco;
    private int estoque;
    private categoriaProduto categoria;

    //Construtor
    public Produto(int id, String nome, String descricao, double preco, int estoque, categoriaProduto produto){
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.estoque = estoque;
    }

    // MÉTODOS
    public void adicionarEstoque(int quantidade){
        if (quantidade > 0){
            estoque =+quantidade;
        }
    }
    public void retirarEstoque(int quantidade){
        if (possuiEstoque(quantidade)) {
            estoque -= quantidade;
    }
    public boolean possuiEstoque(int quantidade){
            return estoque >= quantidade;
        }
    }
    // Inicializar o preço
    public Produto(double preco) {
        this.preco = preco;
    }

    // Metodo necessário para o ItemPedido ler o valor
    public double getPreco() {
        return this.preco;
    }

    public boolean possuiEstoque(int quantidade) {
    }

    public String getNome() {
    }

    // ENUM
    public enum categoriaProduto {
        PIPOCA,
        BEBIDA,
        DOCE,
        COMBO
    }
}
