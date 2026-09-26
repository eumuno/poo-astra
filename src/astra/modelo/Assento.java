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
        this.ocupado = true;
    }
    public void liberar(){
        this.ocupado = false;
    }
     //private boolean ocupado() {//?? precisaria de 2 ocupados?

    public boolean estaDisponivel(){
        return !this.ocupado;
    }
    public boolean ehPreferencial(){
        return this.tipo == tipoAssento.PREFERENCIAL;
    }

    //Getter
    public int getNumero(){
        return numero;
    }
    public tipoAssento getTipo() {
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
