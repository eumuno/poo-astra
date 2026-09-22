public class Assento {

    // ATRIBUTOS
    private int numero;
    private tipoAssento tipo;
    private boolean ocupado;

    // ENUM
    public enum tipoAssento {
        COMUM,
        PREFERENCIAL
    }

    // MÉTODOS
    public void ocupar()
    public void liberar()
    public boolean estaDisponivel()
    public boolean ehPreferencial()

    // REGRA DE NEGÓCIO SE JÁ ESTIVER OCUPADO
    private class assentoIndisponivelException extends Exception {
    }
}
