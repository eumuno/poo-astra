package astra.modelo;

import java.util.List;

/**
 * Sala de cinema, identificada por um número e formada por uma lista de {@link Assento}.
 *
 * <p>A sala sabe localizar seus assentos pelo número e contar quantos ainda estão
 * livres. O estado de cada assento (ocupado ou livre) é mantido pelo próprio {@link Assento}.</p>
 */
public class Sala {

    // ATRIBUTOS
    /** Número que identifica a sala no cinema. */
    private int numero;
    /** Assentos que pertencem à sala. */
    private List<Assento> assentos;

    //CONSTRUTOR

    /**
     * Cria uma sala com os assentos informados.
     *
     * @param numero   número que identifica a sala
     * @param assentos assentos da sala
     */
    public Sala(int numero, List<Assento> assentos) {
        this.numero = numero;
        this.assentos = assentos;
    }

    // MÉTODOS

    /**
     * Verifica se a sala tem um assento com o número informado.
     *
     * @param numero número do assento procurado
     * @return {@code true} se o assento existe na sala; {@code false} caso contrário
     */
    public boolean possuiAssento(int numero){
        return buscarAssento(numero) != null;
    }

    /**
     * Procura um assento da sala pelo número.
     *
     * @param numero número do assento procurado
     * @return o assento encontrado, ou {@code null} se a sala não tiver esse número
     */
    public Assento buscarAssento(int numero){
        for(Assento assento : assentos){
            if(assento.getNumero() == numero){
                return assento;
            }
        }
        return null;
    }

    /**
     * Conta os assentos que ainda não estão ocupados.
     *
     * @return a quantidade de assentos disponíveis na sala
     */
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

    /**
     * Devolve o número da sala.
     *
     * @return o número que identifica a sala
     */
    public int getNumero() {
        return numero;
    }

    /**
     * Devolve os assentos da sala.
     *
     * @return a lista de assentos (a própria lista da sala, não uma cópia)
     */
    public List<Assento> getAssentos() {
        return assentos;
    }

}