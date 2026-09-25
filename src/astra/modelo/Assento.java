package astra.modelo;

public class Assento{

    // ATRIBUTOS
    private int numero;
    private tipoAssento tipo;
    private boolean ocupado;

    // Construtor
    public Assento(int numero,tipoAssento tipo){
        this.numero = numero;
        this.tipo = tipo;
        this.ocupado = false;
    }

    // MÉTODOS
    public void ocupar(){

    }
    public void liberar(){

    }
    private boolean ocupado() {

    }

    public boolean estaDisponivel()
    public boolean ehPreferencial()

    // ENUM
    public enum tipoAssento {
        COMUM,
        PREFERENCIAL
    }
}
