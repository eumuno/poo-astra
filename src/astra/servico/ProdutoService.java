package astra.servico;
import astra.excecoes.PersistenciaException;
import astra.modelo.Administrador;
import astra.modelo.Produto;
import astra.persistencia.ProdutoRepositorio;
import java.util.List;

//Regras de cadastro e consulta de produtos da bomboniere
public class ProdutoService {

    private final ProdutoRepositorio repositorio;

    public ProdutoService(ProdutoRepositorio repositorio){
        this.repositorio = repositorio;
    }
    public Produto cadastrarProduto(Administrador admin, String nome, String descricao, double preco, int estoque,
                                    Produto.CategoriaProduto categoria) throws PersistenciaException {
        if (admin == null) {
            throw new IllegalArgumentException("Só um administrador pode cadastrar produtos!");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        if (preco < 0) {
            throw new IllegalArgumentException("O preço não pode ser negativo.");
        }
        if (estoque < 0) {
            throw new IllegalArgumentException("O estoque não pode ser negativo.");
        }
        return repositorio.salvar(new Produto(0, nome, descricao, preco, estoque, categoria));
    }

    public List<Produto> listarProdutos() throws PersistenciaException {
        return repositorio.listarTodos();
    }
}
