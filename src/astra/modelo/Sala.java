package astra.modelo;

import java.util.List;

public class Sala {

    // ATRIBUTOS
    private int numero;
    private List<Assento> assentos;

    //CONSTRUTOR
    public Sala(int numero, List<Assento> assentos) {
        this.numero = numero;
        this.assentos = assentos;
    }

    // MÉTODOS
    public boolean possuiAssento(int numero){
        return buscarAssento(numero) != null;
    }

    public Assento buscarAssento(int numero){
        for(Assento assento : assentos){
            if(assento.getNumero() == numero){
                return assento;
            }
        }
        return null;
    }

    public int quantidadeAssentosDisponiveis(){
        int quantidade = 0;

        for(Assento assento : assentos){
            if(assento.estaDisponivel()){
                quantidade++;
            }
        }
        return quantidade;
    }

    //GETTER
    public int getNumero() {
        return numero;
    }

    public List<Assento> getAssentos() {
        return assentos;
    }

}
