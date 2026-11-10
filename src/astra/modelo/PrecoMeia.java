package astra.modelo;

//Meia: paga metade do valor da inteira
public class PrecoMeia implements PoliticaPreco {
    @Override
    public double calcular(double valorReferencia) {
        return valorReferencia / 2;
    }
}
