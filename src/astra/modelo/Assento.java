package astra.modelo;

public class Assento{

    // ATRIBUTOS
    private int numero;
    private TipoAssento tipo;
    private boolean ocupado;

    // Construtor
    public Assento(int numero,TipoAssento tipo){
        this.numero = numero;
        this.tipo = tipo;
        this.ocupado = false;
    }

    // MÉTODOS
    public void ocupar(){
        this.ocupado = true;
    }
    public void liberar(){
        this.ocupado = false;
    }

    public boolean estaDisponivel(){
        return !this.ocupado;
    }
    public boolean ehPreferencial(){
        return this.tipo == TipoAssento.PREFERENCIAL;
    }

    //Getter
    public int getNumero(){
        return numero;
    }
    public TipoAssento getTipo() {
        return tipo;
    }
    public boolean isOcupado() {
        return ocupado;
    }

    // ENUM
    public enum tipoAssento {
        COMUM,
        PREFERENCIAL
    }
}
