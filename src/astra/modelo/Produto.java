package astra.modelo;

public class Produto {
    // Atributos
    private int id;
    private String nome;
    private String descricao;
    private double preco;
    private int estoque;
    private CategoriaProduto categoria;

    // ENUM
    public enum CategoriaProduto {
        PIPOCA,
        BEBIDA,
        DOCE,
    }

    // CONSTRUTORES
    public Produto(int id, String nome, String descricao, double preco, int estoque, CategoriaProduto categoria) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.estoque = estoque;
        this.categoria = categoria;
    }

    public Produto(double preco) {
        this.preco = preco;
    }

    // Metodos de açao (Estoque)

    public void adicionarEstoque(int quantidade) {
        if (quantidade > 0) {
            this.estoque += quantidade;
        }
    }

    // Verifica se tem estoque (DEVE vir como metodo da classe)
    public boolean possuiEstoque(int quantidade) {
        return this.estoque >= quantidade;
    }

    // Retira do estoque chamando o possuiEstoque
    public void retirarEstoque(int quantidade) {
        if (possuiEstoque(quantidade)) {
            this.estoque -= quantidade;
        }
    }
    // GETTERS E SETTERS

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        if(preco >= 0){
            this.preco = preco;
        }
    }

    public int getEstoque() {
        return estoque;
    }

    public void setEstoque(int estoque) {
        if(estoque >= 0)
        this.estoque = estoque;
    }
    public CategoriaProduto getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaProduto categoria) {
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        // P EXIBIR O NOME E O PREÇO NA INTERFACE GRÁFICA
        return nome + " - R$ " + preco;
    }

}