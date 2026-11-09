package astra.modelo;

public class ItemPedido {

    // ATRIBUTOS
    private final Produto produto;
    private final int quantidade;
    private final double precoUnitario;

    // CONSTRUTOR
    public ItemPedido(Produto produto, int quantidade) {
        // Validacao de regras de negócio antes de instanciar o objeto
        if (produto == null) {
            throw new IllegalArgumentException("O item deve estar associado a um produto valido.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser de no minimo 1 unidade.");
        }

        this.produto = produto;
        this.quantidade = quantidade;

        // Delegacao: o preco e obtido diretamente da entidade responsavel (Produto)
        this.precoUnitario = produto.getPreco();
    }

    // Construtor para reconstruir um item que veio do banco: o preço é o da hora da compra,
    // não o preço atual do produto (que pode ter mudado depois).
    public ItemPedido(Produto produto, int quantidade, double precoUnitario) {
        if (produto == null) {
            throw new IllegalArgumentException("O item deve estar associado a um produto valido.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser de no minimo 1 unidade.");
        }
        if (precoUnitario < 0) {
            throw new IllegalArgumentException("O preco unitario nao pode ser negativo.");
        }
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    // Comportamento proprio da classe: calcula o subtotal evitando classes anemicas
    public double calcularSubtotal() {
        return this.quantidade * this.precoUnitario;
    }

    // GETTERS
    public Produto getProduto() {
        return this.produto;
    }

    public int getQuantidade() {
        return this.quantidade;
    }

    public double getPrecoUnitario() {
        return this.precoUnitario;
    }
}