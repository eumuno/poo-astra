package astra.persistencia;

import astra.excecoes.PersistenciaException;
import astra.modelo.Ingresso;

import java.util.List;

/**
 * Contrato de consulta aos ingressos vendidos.
 *
 * Quem GRAVA os ingressos é o PedidoRepositorio, porque o ingresso só existe dentro de um
 * pedido e os dois precisam ser gravados juntos, na mesma transação (ou grava tudo, ou nada).
 * Este repositório é o lado de leitura: "esse assento já foi vendido?" e "quais ingressos
 * tem esse pedido?".
 */
public interface IngressoRepositorio {

    /**
     * Já existe ingresso vendido para esse assento nessa sessão?
     * A tabela ingresso é a fonte da verdade (UNIQUE sessao_id + assento_id).
     *
     * @param numeroAssento o número do assento na sala (o mesmo que Assento.getNumero())
     */
    boolean assentoEstaOcupado(int sessaoId, int numeroAssento) throws PersistenciaException;

    /** Os ingressos de um pedido, com sessão, filme, assento e cliente carregados. */
    List<Ingresso> listarPorPedido(int pedidoId) throws PersistenciaException;
}