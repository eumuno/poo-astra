package astra.modelo;

/**
 * Implementa a política de preço para ingressos de inteira,
 * mantendo o valor integral do ingresso, sem descontos.
 */
public class PrecoInteira implements PoliticaPreco{

    /**
     * Calcula o preço da entrada inteira.
     *
     * @param valorReferencia valor original do ingresso
     * @return valor integral do ingresso, sem descontos
     */

    @Override
    public double calcular(double valorReferencia) {
        return valorReferencia;
    }
}
