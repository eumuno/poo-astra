package astra.persistencia;

import astra.excecoes.AssentoIndisponivelException;
import astra.excecoes.EstoqueInsuficienteException;
import astra.excecoes.PersistenciaException;
import astra.modelo.Pagamento;
import astra.modelo.Pedido;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de acesso aos pedidos. O pedido é a "raiz" da venda: gravar um pedido grava
 * junto os itens da bomboniere (abatendo o estoque), os ingressos e o pagamento, tudo
 * numa transação só.
 */
public interface PedidoRepositorio {

    /**
     * Grava a venda inteira (pedido + itens + baixa de estoque + ingressos + pagamento) de forma
     * atômica: se qualquer parte falhar, nada fica gravado (inclusive a baixa de estoque).
     *
     * @param pedido    pedido novo (id 0), normalmente já PAGO
     * @param pagamento o pagamento que quitou o pedido
     * @return o pedido gravado, já com os ids definitivos do pedido e dos ingressos
     * @throws AssentoIndisponivelException se algum assento já foi vendido para essa sessão
     *                                      (por exemplo, por outra janela aberta ao mesmo tempo)
     * @throws EstoqueInsuficienteException se algum produto não tem unidades suficientes em estoque
     *                                      (a conferência é feita pelo próprio banco, então vale mesmo com duas janelas abertas)
     * @throws PersistenciaException        para qualquer outro erro (cliente inválido, produto não gravado, banco fora...)
     */
    Pedido salvar(Pedido pedido, Pagamento pagamento)
            throws PersistenciaException, AssentoIndisponivelException, EstoqueInsuficienteException;

    /**
     * Busca um pedido pelo id.
     *
     * @param id identificador do pedido
     * @return o pedido (itens e ingressos carregados), ou vazio se não existir
     * @throws PersistenciaException se o banco falhar
     */
    Optional<Pedido> buscarPorId(int id) throws PersistenciaException;

    /**
     * Lista os pedidos de um cliente, do mais recente para o mais antigo.
     *
     * @param clienteId identificador do cliente
     * @return os pedidos do cliente
     * @throws PersistenciaException se o banco falhar
     */
    List<Pedido> listarPorCliente(int clienteId) throws PersistenciaException;

    /**
     * Lista todos os pedidos, do mais recente para o mais antigo (tela do administrador).
     *
     * @return todos os pedidos
     * @throws PersistenciaException se o banco falhar
     */
    List<Pedido> listarTodos() throws PersistenciaException;

    /**
     * Grava o novo status de um pedido que já existe (PAGO, PRONTO, RETIRADO...).
     * A regra de quais transições são válidas continua no Pedido (marcarComoPronto, retirar...):
     * quem chama usa o método do Pedido e depois manda gravar o status resultante.
     *
     * @param pedidoId   identificador do pedido
     * @param novoStatus o status a gravar
     * @throws PersistenciaException se o pedido não existir ou o banco falhar
     */
    void atualizarStatus(int pedidoId, Pedido.StatusPedido novoStatus) throws PersistenciaException;
}