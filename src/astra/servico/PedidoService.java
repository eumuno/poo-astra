package astra.servico;

import astra.excecoes.PersistenciaException;
import astra.modelo.Administrador;
import astra.modelo.Cliente;
import astra.modelo.Pedido;
import astra.persistencia.PedidoRepositorio;

import java.util.List;

/**
 * Casos de uso de acompanhamento dos pedidos: o cliente consulta as próprias compras e o
 * administrador lista tudo e leva cada pedido de pago a pronto e a retirado.
 *
 * <p>Quem decide se uma mudança de status é válida é o próprio {@link Pedido}; este serviço
 * carrega o pedido atualizado, pede a mudança e grava o novo status.</p>
 */
public class PedidoService {

    private final PedidoRepositorio repositorio;

    /**
     * Cria o serviço sobre o repositório de pedidos recebido.
     *
     * @param repositorio onde os pedidos são lidos e o status é gravado
     */
    public PedidoService(PedidoRepositorio repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("O repositório de pedidos é obrigatório.");
        }
        this.repositorio = repositorio;
    }

    /**
     * Lista todos os pedidos, do mais recente para o mais antigo (visão do administrador).
     *
     * @return todos os pedidos
     * @throws PersistenciaException se o banco falhar
     */
    public List<Pedido> listarTodos() throws PersistenciaException {
        return repositorio.listarTodos();
    }

    /**
     * Lista as compras de um cliente, da mais recente para a mais antiga.
     *
     * @param cliente dono dos pedidos (precisa já estar gravado)
     * @return os pedidos do cliente
     * @throws IllegalArgumentException se não houver cliente
     * @throws PersistenciaException    se o banco falhar
     */
    public List<Pedido> listarPorCliente(Cliente cliente) throws PersistenciaException {
        if (cliente == null) {
            throw new IllegalArgumentException("Informe o cliente.");
        }
        return repositorio.listarPorCliente(cliente.getId());
    }

    /**
     * Marca um pedido pago como pronto para retirada.
     *
     * @param admin    administrador que está atualizando (obrigatório)
     * @param pedidoId id do pedido
     * @return o pedido já com o novo status
     * @throws IllegalArgumentException se não houver administrador ou o pedido não existir
     * @throws IllegalStateException    se o pedido não estiver pago
     * @throws PersistenciaException    se o banco falhar
     */
    public Pedido marcarComoPronto(Administrador admin, int pedidoId) throws PersistenciaException {
        Pedido pedido = carregar(admin, pedidoId);
        pedido.marcarComoPronto();
        repositorio.atualizarStatus(pedido.getId(), pedido.getStatus());
        return pedido;
    }

    /**
     * Marca um pedido pronto como retirado pelo cliente.
     *
     * @param admin    administrador que está atualizando (obrigatório)
     * @param pedidoId id do pedido
     * @return o pedido já com o novo status
     * @throws IllegalArgumentException se não houver administrador ou o pedido não existir
     * @throws IllegalStateException    se o pedido não estiver pronto
     * @throws PersistenciaException    se o banco falhar
     */
    public Pedido marcarComoRetirado(Administrador admin, int pedidoId) throws PersistenciaException {
        Pedido pedido = carregar(admin, pedidoId);
        pedido.retirar();
        repositorio.atualizarStatus(pedido.getId(), pedido.getStatus());
        return pedido;
    }

    // Sempre parte do pedido como está no banco agora, e não de uma cópia que a tela guardou há tempos
    private Pedido carregar(Administrador admin, int pedidoId) throws PersistenciaException {
        if (admin == null) {
            throw new IllegalArgumentException("Só um administrador pode atualizar pedidos!");
        }
        return repositorio.buscarPorId(pedidoId).orElseThrow(() ->
                new IllegalArgumentException("O pedido escolhido não existe mais."));
    }
}