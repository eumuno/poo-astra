package astra.modelo;

/**
 * Implementa a política de preço para ingressos de cortesia,
 * permitindo a entrada gratuita, sem cobrança de valor.
 */
public class PrecoCortesia implements PoliticaPreco{

    /**
     * Calcula o preço de um ingresso de cortesia.
     *
     * @param valorReferencia valor original do ingresso
     * @return zero, pois o ingresso de cortesia é gratuito
     */
    @Override
    public double calcular(double valorReferencia) {
        return 0;
    }
}
