package astra.modelo;

//Inteira: pagamento do valor cheio
public class PrecoInteira implements PoliticaPreco{
    @Override
    public double calcular(double valorReferencia) {
        return valorReferencia;
    }
}
