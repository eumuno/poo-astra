package astra.servico;
import astra.excecoes.PersistenciaException;
import astra.modelo.Administrador;
import astra.modelo.Produto;
import astra.persistencia.ProdutoRepositorio;
import java.util.List;

/**
 * Casos de uso da bomboniere: cadastrar, editar, repor estoque e listar produtos.
 *
 * O estoque tem dois caminhos separados e propositais: a venda o diminui (dentro da transação do
 * pedido) e {@link #reporEstoque} o aumenta. Editar os dados de um produto nunca mexe nele.
 */
public class ProdutoService {

    private final ProdutoRepositorio repositorio;

    /**
     * Cria o serviço sobre o repositório de produtos recebido.
     *
     * @param repositorio onde os produtos são lidos e gravados
     */
    public ProdutoService(ProdutoRepositorio repositorio){
        this.repositorio = repositorio;
    }

    /**
     * Cadastra um produto novo.
     *
     * @param admin     administrador que está cadastrando (obrigatório)
     * @param nome      nome do produto (obrigatório e único, sem diferenciar maiúsculas)
     * @param descricao descrição do produto (opcional)
     * @param preco     preço unitário (zero ou mais)
     * @param estoque   quantidade inicial em estoque (zero ou mais)
     * @param categoria categoria do produto (obrigatória)
     * @return o produto gravado, com o id gerado pelo banco
     * @throws IllegalArgumentException se algum dado for inválido ou já existir um produto com o mesmo nome
     * @throws PersistenciaException    se o banco falhar
     */
    public Produto cadastrarProduto(Administrador admin, String nome, String descricao, double preco, int estoque,
                                    Produto.CategoriaProduto categoria) throws PersistenciaException {
        validarDados(admin, nome, preco, categoria);
        if (estoque < 0) {
            throw new IllegalArgumentException("O estoque não pode ser negativo.");
        }
        exigirNomeLivre(nome, 0);
        return repositorio.salvar(new Produto(0, nome.trim(), textoOuVazio(descricao), preco, estoque, categoria));
    }

    /**
     * Altera nome, descrição, preço e categoria de um produto. O estoque não é alterado.
     *
     * @param admin     administrador que está alterando (obrigatório)
     * @param produtoId id do produto que será alterado
     * @param nome      novo nome (obrigatório e único, sem diferenciar maiúsculas)
     * @param descricao nova descrição (opcional)
     * @param preco     novo preço unitário (zero ou mais)
     * @param categoria nova categoria (obrigatória)
     * @return o produto como ficou no banco, com o estoque atual
     * @throws IllegalArgumentException se algum dado for inválido ou outro produto já tiver o mesmo nome
     * @throws PersistenciaException    se o produto não existir ou o banco falhar
     */
    public Produto atualizarProduto(Administrador admin, int produtoId, String nome, String descricao, double preco,
                                    Produto.CategoriaProduto categoria) throws PersistenciaException {
        if (produtoId <= 0) {
            throw new IllegalArgumentException("Escolha um produto já cadastrado para editar.");
        }
        validarDados(admin, nome, preco, categoria);
        exigirNomeLivre(nome, produtoId);
        return repositorio.atualizarDados(new Produto(produtoId, nome.trim(), textoOuVazio(descricao), preco, 0, categoria));
    }

    /**
     * Soma unidades ao estoque de um produto existente.
     *
     * @param admin      administrador que está repondo (obrigatório)
     * @param produtoId  id do produto que recebeu as unidades
     * @param quantidade unidades recebidas (maior que zero)
     * @return o produto com o estoque atualizado
     * @throws IllegalArgumentException se a quantidade não for positiva
     * @throws PersistenciaException    se o produto não existir ou o banco falhar
     */
    public Produto reporEstoque(Administrador admin, int produtoId, int quantidade) throws PersistenciaException {
        if (admin == null) {
            throw new IllegalArgumentException("Só um administrador pode repor estoque!");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade a repor deve ser maior que zero.");
        }
        return repositorio.adicionarEstoque(produtoId, quantidade);
    }

    /**
     * Lista os produtos da bomboniere.
     *
     * @return todos os produtos da bomboniere
     * @throws PersistenciaException se o banco falhar
     */
    public List<Produto> listarProdutos() throws PersistenciaException {
        return repositorio.listarTodos();
    }

    private void validarDados(Administrador admin, String nome, double preco, Produto.CategoriaProduto categoria) {
        if (admin == null) {
            throw new IllegalArgumentException("Só um administrador pode gerenciar produtos!");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        if (!Double.isFinite(preco) || preco < 0) {
            throw new IllegalArgumentException("O preço deve ser um valor válido e não negativo.");
        }
        if (categoria == null) {
            throw new IllegalArgumentException("Escolha a categoria do produto.");
        }
    }

    // Dois produtos com o mesmo nome (sem diferenciar maiúsculas) confundem o cliente na bomboniere
    private void exigirNomeLivre(String nome, int idIgnorado) throws PersistenciaException {
        for (Produto existente : repositorio.listarTodos()) {
            if (existente.getId() != idIgnorado && existente.getNome().trim().equalsIgnoreCase(nome.trim())) {
                throw new IllegalArgumentException("Já existe um produto chamado \"" + existente.getNome() + "\".");
            }
        }
    }

    private String textoOuVazio(String texto) {
        return texto == null ? "" : texto.trim();
    }
}