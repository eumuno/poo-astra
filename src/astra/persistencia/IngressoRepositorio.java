package astra.persistencia;

import astra.excecoes.PersistenciaException;
import astra.modelo.Ingresso;

import java.util.List;

/**
 * Contrato de consulta aos ingressos vendidos.
 *
 * <p>Quem GRAVA os ingressos é o PedidoRepositorio, porque o ingresso só existe dentro de um
 * pedido e os dois precisam ser gravados juntos, na mesma transação (ou grava tudo, ou nada).
 * Este repositório é o lado de leitura: "esse assento já foi vendido?" e "quais ingressos
 * tem esse pedido?".</p>
 */
public interface IngressoRepositorio {

    /**
     * Informa se já existe ingresso vendido para esse assento nessa sessão.
     * A tabela ingresso é a fonte da verdade (UNIQUE sessao_id + assento_id).
     *
     * @param sessaoId      identificador da sessão
     * @param numeroAssento o número do assento na sala (o mesmo que Assento.getNumero())
     * @return {@code true} se o assento já foi vendido
     * @throws PersistenciaException se o banco falhar
     */
    boolean assentoEstaOcupado(int sessaoId, int numeroAssento) throws PersistenciaException;

    /**
     * Lista os ingressos de um pedido, com sessão, filme, assento e cliente carregados.
     *
     * @param pedidoId identificador do pedido
     * @return os ingressos do pedido (vazio se não houver)
     * @throws PersistenciaException se o banco falhar
     */
    List<Ingresso> listarPorPedido(int pedidoId) throws PersistenciaException;
}