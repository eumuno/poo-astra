package astra.modelo;

public class ItemPedido {

    // Encapsulamento: atributos privados impedem alteracoes externas descontroladas
    // O modificador final garante a imutabilidade do item apos ser registrado na compra
    private final Produto produto;
    private final int quantidade;
    private final double precoUnitario;

    // Construtor: protege os invariantes para impedir a criacao de objetos inconsistentes
    public ItemPedido(Produto produto, int quantidade) {
        // Validacao de regras de negocio antes de instanciar o objeto
        if (produto == null) {
            throw new IllegalArgumentException("O item deve estar associado a um produto valido.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser de no minimo 1 unidade.");
        }

        // 'this' diferencia o atributo interno da classe do parametro homonimo recebido
        this.produto = produto;
        this.quantidade = quantidade;

        // Delegacao: o preco e obtido diretamente da entidade responsavel (Produto)
        this.precoUnitario = produto.getPreco();
    }

    // Comportamento proprio da classe: calcula o subtotal evitando classes anemicas
    public double calcularSubtotal() {
        return this.quantidade * this.precoUnitario;
    }

    // Metodos de acesso (Getters): expoem apenas a leitura segura dos dados essenciais
    public Produto getProduto() {
        return this.produto;
    }

    public int getQuantidade() {
        return this.quantidade;
    }

    public double getPrecoUnitario() {
        return this.precoUnitario;
}