
package astra.modelo;

/**
 * Representa um produto vendido na bomboniere do cinema.
 * Armazena suas informações e permite consultar e alterar o estoque.
 */
public class Produto {

    // Atributos
    private int id;
    private String nome;
    private String descricao;
    private double preco;
    private int estoque;
    private CategoriaProduto categoria;

    /**
     * Categorias utilizadas para classificar os produtos da bomboniere.
     */
    public enum CategoriaProduto {
        /** Produtos de pipoca. */
        PIPOCA,

        /** Produtos de bebida. */
        BEBIDA,

        /** Produtos de doce. */
        DOCE,
    }

    // CONSTRUTORES

    /**
     * Cria um produto com todas as suas informações.
     *
     * @param id identificador do produto
     * @param nome nome do produto
     * @param descricao descrição do produto
     * @param preco preço do produto
     * @param estoque quantidade disponível em estoque
     * @param categoria categoria do produto
     */
    public Produto(int id, String nome, String descricao, double preco,
                   int estoque, CategoriaProduto categoria) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.estoque = estoque;
        this.categoria = categoria;
    }

    /**
     * Cria um produto informando apenas o preço.
     *
     * @param preco preço inicial do produto
     */
    public Produto(double preco) {
        this.preco = preco;
    }

    // Métodos de ação (Estoque)

    /**
     * Adiciona unidades ao estoque do produto.
     * A quantidade só é adicionada quando for maior que zero.
     *
     * @param quantidade quantidade de unidades a adicionar
     */
    public void adicionarEstoque(int quantidade) {
        if (quantidade > 0) {
            this.estoque += quantidade;
        }
    }

    /**
     * Verifica se o estoque possui a quantidade informada.
     *
     * @param quantidade quantidade a ser consultada
     * @return true se o estoque for suficiente; false caso contrário
     */
    public boolean possuiEstoque(int quantidade) {
        return this.estoque >= quantidade;
    }

    /**
     * Retira a quantidade informada do estoque do produto.
     *
     * @param quantidade quantidade de unidades a retirar
     * @throws IllegalStateException se não houver estoque suficiente
     */
    public void retirarEstoque(int quantidade) {
        if (!possuiEstoque(quantidade)) {
            throw new IllegalStateException("Estoque insuficiente de \"" + this.nome
                    + "\": restam " + this.estoque + " e foram pedidas " + quantidade + ".");
        }
        this.estoque -= quantidade;
    }

    // GETTERS E SETTERS

    /**
     * Obtém o identificador do produto.
     *
     * @return identificador do produto
     */
    public int getId() {
        return id;
    }

    /**
     * Define o identificador do produto.
     *
     * @param id novo identificador do produto
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtém o nome do produto.
     *
     * @return nome do produto
     */
    public String getNome() {
        return nome;
    }

    /**
     * Define o nome do produto.
     *
     * @param nome novo nome do produto
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Obtém a descrição do produto.
     *
     * @return descrição do produto
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Define a descrição do produto.
     *
     * @param descricao nova descrição do produto
     */
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Obtém o preço do produto.
     *
     * @return preço atual do produto
     */
    public double getPreco() {
        return preco;
    }

    /**
     * Atualiza o preço do produto quando o valor não é negativo.
     *
     * @param preco novo preço do produto
     */
    public void setPreco(double preco) {
        if (preco >= 0) {
            this.preco = preco;
        }
    }

    /**
     * Obtém a quantidade disponível em estoque.
     *
     * @return quantidade atual em estoque
     */
    public int getEstoque() {
        return estoque;
    }

    /**
     * Atualiza o estoque quando a quantidade não é negativa.
     *
     * @param estoque nova quantidade em estoque
     */
    public void setEstoque(int estoque) {
        if (estoque >= 0)
            this.estoque = estoque;
    }

    /**
     * Obtém a categoria do produto.
     *
     * @return categoria do produto
     */
    public CategoriaProduto getCategoria() {
        return categoria;
    }

    /**
     * Define a categoria do produto.
     *
     * @param categoria nova categoria do produto
     */
    public void setCategoria(CategoriaProduto categoria) {
        this.categoria = categoria;
    }

    /**
     * Retorna uma representação textual com o nome e o preço
     * do produto para exibição na interface gráfica.
     *
     * @return nome e preço do produto
     */
    @Override
    public String toString() {
        // P EXIBIR O NOME E O PREÇO NA INTERFACE GRÁFICA
        return nome + " - R$ " + preco;
    }
}
