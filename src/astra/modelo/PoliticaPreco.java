package astra.modelo;

/**
 * Regra de cálculo do preço final de um ingresso (padrão Strategy).
 *
 * <p>Cada tipo de ingresso ({@link Ingresso.TipoIngresso}) usa uma implementação
 * própria, como {@link PrecoInteira}, {@link PrecoMeia} e {@link PrecoCortesia}.
 * Assim, {@link Ingresso} calcula o preço pedindo à política, sem usar
 * {@code if} ou {@code switch} por tipo. Para criar um tipo novo, basta
 * implementar esta interface, sem alterar o código existente (princípio aberto/fechado).</p>
 */
public interface PoliticaPreco {

    /**
     * Calcula o preço final do ingresso a partir do valor de referência.
     *
     * @param valorReferencia preço do ingresso inteiro na sessão, em reais
     * @return o valor a cobrar, em reais
     */
    double calcular(double valorReferencia);
}