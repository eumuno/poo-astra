package astra.modelo;

/**
 * Representa uma linha de produto da bomboniere dentro de um {@link Pedido}:
 * qual produto foi escolhido, quantas unidades e por quanto cada uma foi vendida.
 * O preço unitário fica guardado no item, então mudar o preço do produto depois
 * não altera pedidos antigos.
 */
public class ItemPedido {

    // ATRIBUTOS
    private final Produto produto;
    private final int quantidade;
    private final double precoUnitario;

    // CONSTRUTOR
    /**
     * Cria um item novo, usando o preço atual do produto.
     *
     * @param produto    produto escolhido; não pode ser nulo
     * @param quantidade número de unidades; deve ser no mínimo 1
     * @throws IllegalArgumentException se o produto for nulo ou a quantidade for menor que 1
     */
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

    /**
     * Reconstrói um item que veio do banco. Usa o preço da hora da compra,
     * e não o preço atual do produto (que pode ter mudado depois).
     *
     * @param produto       produto do item; não pode ser nulo
     * @param quantidade    número de unidades; deve ser no mínimo 1
     * @param precoUnitario preço de cada unidade na hora da compra; não pode ser negativo
     * @throws IllegalArgumentException se o produto for nulo, a quantidade for menor que 1
     *                                  ou o preço for negativo
     */
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

    /**
     * Calcula o subtotal do item.
     *
     * @return quantidade multiplicada pelo preço unitário
     */
    public double calcularSubtotal() {
        return this.quantidade * this.precoUnitario;
    }

    // GETTERS
    /** @return o produto deste item */
    public Produto getProduto() {
        return this.produto;
    }

    /** @return o número de unidades do item */
    public int getQuantidade() {
        return this.quantidade;
    }

    /** @return o preço de cada unidade, fixado no momento da compra */
    public double getPrecoUnitario() {
        return this.precoUnitario;
    }
}