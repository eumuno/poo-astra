package astra.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.time.format.DateTimeFormatter;

public class Sessao {

    // ATRIBUTOS
    private int id;
    private Filme filme;
    private Sala sala;
    private LocalDateTime dataHora;
    private statusSessao status;
    private final double precoBase; // valor do ingresso inteiro nesta sessão (a meia é metade, a cortesia é zero)
    private Set<Integer> assentosOcupados = new HashSet<>();

    // valor usado quando ninguém informa o preço (era o 15 fixo que ficava no Ingresso)
    public static final double PRECO_BASE_PADRAO = 15.0;

    //CONSTRUTORES
    public Sessao(int id, Filme filme, Sala sala, LocalDateTime dataHora, statusSessao status, double precoBase) {
        if (precoBase < 0) {
            throw new IllegalArgumentException("O preço base da sessão não pode ser negativo!");
        }
        this.id = id;
        this.filme = filme;
        this.sala = sala;
        this.dataHora = dataHora;
        this.status = status;
        this.precoBase = precoBase;
    }

    // mantido para o código que ainda não informa o preço: usa o padrão
    public Sessao(int id, Filme filme, Sala sala, LocalDateTime dataHora, statusSessao status) {
        this(id, filme, sala, dataHora, status, PRECO_BASE_PADRAO);
    }

    // ENUM
    public enum statusSessao {
        AGENDADA,
        ENCERRADA,
        CANCELADA
    }

    // MÉTODOS
    public boolean estaDisponivel(){
        return status == statusSessao.AGENDADA;
    }

   public boolean possuiVagas(){
        return assentosOcupados.size() < sala.getAssentos().size();
   }

   public boolean assentoEstaDisponivel(int numero){
        return sala.possuiAssento(numero)
                && !assentosOcupados.contains(numero);
   }

   public void ocuparAssento(int numero){
        if(!assentoEstaDisponivel(numero)){
            throw new IllegalStateException("Assento ocupado ou inexistente na sessão!");
        }
        assentosOcupados.add(numero);
   }

    public void cancelar(){
        status = statusSessao.CANCELADA;
    }

    //GETTERS
    public int getId(){
        return id;
    }

    public Filme getFilme(){
        return filme;
    }

    public Sala getSala(){
        return sala;
    }

    public LocalDateTime getDataHora(){
        return dataHora;
    }

    public statusSessao getStatus(){
        return status;
    }

    public double getPrecoBase(){
        return precoBase;
    }

    @Override
    public String toString(){
        //Exibir no combo de sessões do cliente.
        return filme.getTitulo() + " - " + dataHora.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }
}