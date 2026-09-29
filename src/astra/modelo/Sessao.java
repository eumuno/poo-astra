package astra.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Sessao {

    // ATRIBUTOS
    private int id;
    private Filme filme;
    private Sala sala;
    private LocalDateTime dataHora;
    private statusSessao status;
    private Set<Integer> assentosOcupados = new HashSet<>();

    //CONSTRUTOR
    public Sessao(int id, Filme filme, Sala sala, LocalDateTime dataHora, statusSessao status) {
        this.id = id;
        this.filme = filme;
        this.sala = sala;
        this.dataHora = dataHora;
        this.status = status;
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

    //GETTER
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
}