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
 * carrega o pedido atualizado, pede a mudança e grava o novo status. Ver todos os pedidos (que mostra as
 * compras de todos os clientes) e mudar o status exigem um administrador, conferido no banco por
 * {@link ControleDeAcesso}; cada mudança entra na trilha de auditoria.</p>
 */
public class PedidoService {

    private final PedidoRepositorio repositorio;
    private final ControleDeAcesso acesso;
    private final TrilhaDeAuditoria trilha;

    /**
     * Cria o serviço sobre o repositório de pedidos recebido.
     *
     * @param repositorio onde os pedidos são lidos e o status é gravado
     * @param acesso      confere se quem acompanha todos os pedidos é administrador
     * @param trilha      onde as mudanças de status são registradas
     */
    public PedidoService(PedidoRepositorio repositorio, ControleDeAcesso acesso, TrilhaDeAuditoria trilha) {
        if (repositorio == null) {
            throw new IllegalArgumentException("O repositório de pedidos é obrigatório.");
        }
        this.repositorio = repositorio;
        this.acesso = acesso;
        this.trilha = trilha;
    }

    /**
     * Lista todos os pedidos, do mais recente para o mais antigo (visão do administrador).
     *
     * @param admin administrador que está consultando (obrigatório)
     * @return todos os pedidos
     * @throws astra.excecoes.AcessoNegadoException se quem consulta não for administrador
     * @throws PersistenciaException                se o banco falhar
     */
    public List<Pedido> listarTodos(Administrador admin) throws PersistenciaException {
        acesso.exigirAdministrador(admin);
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
     * @throws astra.excecoes.AcessoNegadoException se quem atualiza não for administrador
     * @throws IllegalArgumentException             se o pedido não existir
     * @throws IllegalStateException                se o pedido não estiver pago
     * @throws PersistenciaException                se o banco falhar
     */
    public Pedido marcarComoPronto(Administrador admin, int pedidoId) throws PersistenciaException {
        Pedido pedido = carregar(admin, pedidoId);
        pedido.marcarComoPronto();
        repositorio.atualizarStatus(pedido.getId(), pedido.getStatus());
        trilha.registrar(admin.getId(), TrilhaDeAuditoria.PEDIDO_PRONTO, "pedido=" + pedido.getId());
        return pedido;
    }

    /**
     * Marca um pedido pronto como retirado pelo cliente.
     *
     * @param admin    administrador que está atualizando (obrigatório)
     * @param pedidoId id do pedido
     * @return o pedido já com o novo status
     * @throws astra.excecoes.AcessoNegadoException se quem atualiza não for administrador
     * @throws IllegalArgumentException             se o pedido não existir
     * @throws IllegalStateException                se o pedido não estiver pronto
     * @throws PersistenciaException                se o banco falhar
     */
    public Pedido marcarComoRetirado(Administrador admin, int pedidoId) throws PersistenciaException {
        Pedido pedido = carregar(admin, pedidoId);
        pedido.retirar();
        repositorio.atualizarStatus(pedido.getId(), pedido.getStatus());
        trilha.registrar(admin.getId(), TrilhaDeAuditoria.PEDIDO_RETIRADO, "pedido=" + pedido.getId());
        return pedido;
    }

    // Sempre parte do pedido como está no banco agora, e não de uma cópia que a tela guardou há tempos
    private Pedido carregar(Administrador admin, int pedidoId) throws PersistenciaException {
        acesso.exigirAdministrador(admin);
        return repositorio.buscarPorId(pedidoId).orElseThrow(() ->
                new IllegalArgumentException("O pedido escolhido não existe mais."));
    }
}