package astra.modelo;

/**
 * Implementa a política de preço para ingressos de meia-entrada,
 * cobrando metade do valor integral do ingresso.
 */
public class PrecoMeia implements PoliticaPreco {

    /**
     * Calcula o preço da meia-entrada.
     *
     * @param valorReferencia valor original do ingresso
     * @return metade do valor original do ingresso
     */
    @Override
    public double calcular(double valorReferencia) {
        return valorReferencia / 2;
    }
}
