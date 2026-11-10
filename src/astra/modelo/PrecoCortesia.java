package astra.modelo;

//Cortesia: não paga nada
public class PrecoCortesia implements PoliticaPreco{
    @Override
    public double calcular(double valorReferencia) {
        return 0;
    }
}
