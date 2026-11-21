package astra.servico;
import astra.excecoes.PersistenciaException;
import astra.modelo.Administrador;
import astra.modelo.Produto;
import astra.persistencia.ProdutoRepositorio;
import java.util.List;

/**
 * Casos de uso da bomboniere: cadastrar, editar, repor estoque e listar produtos.
 *
 * <p>O estoque tem dois caminhos separados e propositais: a venda o diminui (dentro da transação do
 * pedido) e {@link #reporEstoque} o aumenta. Editar os dados de um produto nunca mexe nele. Só administradores
 * alteram a bomboniere (conferido no banco por {@link ControleDeAcesso}) e cada alteração entra na trilha de
 * auditoria.</p>
 */
public class ProdutoService {

    /** Tamanho máximo do nome do produto, em caracteres. */
    public static final int LIMITE_NOME = 100;
    /** Tamanho máximo da descrição do produto, em caracteres. */
    public static final int LIMITE_DESCRICAO = 300;
    /** Estoque inicial máximo de um produto novo. */
    public static final int LIMITE_ESTOQUE_INICIAL = 100_000;
    /** Máximo de unidades que podem ser repostas de uma vez. */
    public static final int LIMITE_DE_REPOSICAO = 100_000;

    private final ProdutoRepositorio repositorio;
    private final ControleDeAcesso acesso;
    private final TrilhaDeAuditoria trilha;

    /**
     * Cria o serviço sobre o repositório de produtos recebido.
     *
     * @param repositorio onde os produtos são lidos e gravados
     * @param acesso      confere se quem altera é administrador
     * @param trilha      onde as alterações são registradas
     */
    public ProdutoService(ProdutoRepositorio repositorio, ControleDeAcesso acesso, TrilhaDeAuditoria trilha){
        this.repositorio = repositorio;
        this.acesso = acesso;
        this.trilha = trilha;
    }

    /**
     * Cadastra um produto novo.
     *
     * @param admin     administrador que está cadastrando (obrigatório)
     * @param nome      nome do produto (obrigatório e único, sem diferenciar maiúsculas)
     * @param descricao descrição do produto (opcional)
     * @param preco     preço unitário (zero ou mais)
     * @param estoque   quantidade inicial em estoque (de zero ao limite)
     * @param categoria categoria do produto (obrigatória)
     * @return o produto gravado, com o id gerado pelo banco
     * @throws astra.excecoes.AcessoNegadoException se quem cadastra não for administrador
     * @throws IllegalArgumentException             se algum dado for inválido ou já existir um produto com o mesmo nome
     * @throws PersistenciaException                se o banco falhar
     */
    public Produto cadastrarProduto(Administrador admin, String nome, String descricao, double preco, int estoque,
                                    Produto.CategoriaProduto categoria) throws PersistenciaException {
        acesso.exigirAdministrador(admin);
        validarDados(nome, descricao, preco, categoria);
        if (estoque < 0) {
            throw new IllegalArgumentException("O estoque não pode ser negativo.");
        }
        if (estoque > LIMITE_ESTOQUE_INICIAL) {
            throw new IllegalArgumentException("O estoque inicial deve ser de no máximo " + LIMITE_ESTOQUE_INICIAL + " unidades.");
        }
        exigirNomeLivre(nome, 0);
        Produto salvo = repositorio.salvar(new Produto(0, nome.trim(), textoOuVazio(descricao), preco, estoque, categoria));
        trilha.registrar(admin.getId(), TrilhaDeAuditoria.PRODUTO_CRIADO,
                "id=" + salvo.getId() + " nome=\"" + salvo.getNome() + "\" estoque=" + salvo.getEstoque());
        return salvo;
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
     * @throws astra.excecoes.AcessoNegadoException se quem altera não for administrador
     * @throws IllegalArgumentException             se algum dado for inválido ou outro produto já tiver o mesmo nome
     * @throws PersistenciaException                se o produto não existir ou o banco falhar
     */
    public Produto atualizarProduto(Administrador admin, int produtoId, String nome, String descricao, double preco,
                                    Produto.CategoriaProduto categoria) throws PersistenciaException {
        acesso.exigirAdministrador(admin);
        if (produtoId <= 0) {
            throw new IllegalArgumentException("Escolha um produto já cadastrado para editar.");
        }
        validarDados(nome, descricao, preco, categoria);
        exigirNomeLivre(nome, produtoId);
        Produto salvo = repositorio.atualizarDados(new Produto(produtoId, nome.trim(), textoOuVazio(descricao), preco, 0, categoria));
        trilha.registrar(admin.getId(), TrilhaDeAuditoria.PRODUTO_EDITADO,
                "id=" + salvo.getId() + " nome=\"" + salvo.getNome() + "\" preço=" + salvo.getPreco());
        return salvo;
    }

    /**
     * Soma unidades ao estoque de um produto existente.
     *
     * @param admin      administrador que está repondo (obrigatório)
     * @param produtoId  id do produto que recebeu as unidades
     * @param quantidade unidades recebidas (de 1 ao limite de reposição)
     * @return o produto com o estoque atualizado
     * @throws astra.excecoes.AcessoNegadoException se quem repõe não for administrador
     * @throws IllegalArgumentException             se a quantidade não estiver entre 1 e o limite
     * @throws PersistenciaException                se o produto não existir ou o banco falhar
     */
    public Produto reporEstoque(Administrador admin, int produtoId, int quantidade) throws PersistenciaException {
        acesso.exigirAdministrador(admin);
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade a repor deve ser maior que zero.");
        }
        if (quantidade > LIMITE_DE_REPOSICAO) {
            throw new IllegalArgumentException("A reposição máxima por vez é de " + LIMITE_DE_REPOSICAO + " unidades.");
        }
        Produto salvo = repositorio.adicionarEstoque(produtoId, quantidade);
        trilha.registrar(admin.getId(), TrilhaDeAuditoria.ESTOQUE_REPOSTO,
                "id=" + salvo.getId() + " +" + quantidade + " unidades, estoque agora " + salvo.getEstoque());
        return salvo;
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

    private void validarDados(String nome, String descricao, double preco, Produto.CategoriaProduto categoria) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        if (!Double.isFinite(preco) || preco < 0) {
            throw new IllegalArgumentException("O preço deve ser um valor válido e não negativo.");
        }
        if (categoria == null) {
            throw new IllegalArgumentException("Escolha a categoria do produto.");
        }
        Validacao.exigirNoMaximo(nome, LIMITE_NOME, "O nome do produto");
        Validacao.exigirNoMaximo(descricao, LIMITE_DESCRICAO, "A descrição");
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