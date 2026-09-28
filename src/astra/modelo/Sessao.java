package astra.modelo;

import java.time.LocalDateTime;

public class Sessao {

    // ATRIBUTOS
    private int id;
    private Filme filme;
    private Sala sala;
    private LocalDateTime dataHora;
    private double preco;
    private statusSessao status;

    //CONSTRUTOR
    public Sessao(int id, Filme filme, Sala sala, LocalDateTime dataHora, double preco, statusSessao status) {
        this.id = id;
        this.filme = filme;
        this.sala = sala;
        this.dataHora = dataHora;
        this.preco = preco;
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
        //IMPLEMENTAR DEPOIS QUANDO A CLASSE SALA EXISTIR
        return true;
    }

    public void cancelar(){
        status = statusSessao.CANCELADA;
    }

    public double calcularPreco(){
        return preco;
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

    public double getPreco(){
        return preco;
    }

    public statusSessao getStatus(){
        return status;
    }
}