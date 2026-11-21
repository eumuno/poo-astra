package astra.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.time.format.DateTimeFormatter;

/**
 * Uma exibição de um filme, numa sala, em um dia e horário, com um preço base de ingresso.
 *
 * <p>A sessão sabe quais poltronas já foram vendidas e decide se ainda está à venda: ela precisa
 * estar agendada <b>e</b> ainda não ter começado.</p>
 */
public class Sessao {

    // ATRIBUTOS
    private int id;
    private Filme filme;
    private Sala sala;
    private LocalDateTime dataHora;
    private StatusSessao status;
    private final double precoBase; // valor do ingresso inteiro nesta sessão (a meia é metade, a cortesia é zero)
    private Set<Integer> assentosOcupados = new HashSet<>();

    /** Preço usado quando ninguém informa o valor do ingresso (era o 15 fixo que ficava no Ingresso). */
    public static final double PRECO_BASE_PADRAO = 15.0;

    //CONSTRUTORES

    /**
     * Cria uma sessão com preço de ingresso informado.
     *
     * @param id        identificador (0 se ainda não foi gravada no banco)
     * @param filme     filme exibido
     * @param sala      sala onde acontece
     * @param dataHora  dia e horário de início
     * @param status    situação da sessão (agendada, encerrada ou cancelada)
     * @param precoBase valor do ingresso inteiro
     * @throws IllegalArgumentException se o preço for negativo
     */
    public Sessao(int id, Filme filme, Sala sala, LocalDateTime dataHora, StatusSessao status, double precoBase) {
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

    /**
     * Cria uma sessão com o preço padrão ({@link #PRECO_BASE_PADRAO}).
     *
     * @param id       identificador (0 se ainda não foi gravada no banco)
     * @param filme    filme exibido
     * @param sala     sala onde acontece
     * @param dataHora dia e horário de início
     * @param status   situação da sessão
     */
    public Sessao(int id, Filme filme, Sala sala, LocalDateTime dataHora, StatusSessao status) {
        this(id, filme, sala, dataHora, status, PRECO_BASE_PADRAO);
    }

    // ENUM

    /** Situação da sessão. */
    public enum StatusSessao {
        /** Marcada e à venda (enquanto não começar). */
        AGENDADA,
        /** Já aconteceu. */
        ENCERRADA,
        /** Cancelada pela administração. */
        CANCELADA
    }

    // MÉTODOS

    /**
     * Informa se a sessão está à venda agora.
     *
     * @return {@code true} se está agendada e ainda não começou
     */
    public boolean estaDisponivel(){
        return estaDisponivel(LocalDateTime.now());
    }

    /**
     * Informa se a sessão está à venda em um determinado momento. Receber o momento como
     * parâmetro permite testar a regra sem depender do relógio.
     *
     * @param agora o momento da consulta
     * @return {@code true} se está agendada e começa depois de {@code agora}
     */
    public boolean estaDisponivel(LocalDateTime agora){
        return status == StatusSessao.AGENDADA && !jaComecou(agora);
    }

    /**
     * Informa se o horário da sessão já chegou, independentemente do status.
     *
     * @param agora o momento da consulta
     * @return {@code true} se a sessão começa em {@code agora} ou antes
     */
    public boolean jaComecou(LocalDateTime agora){
        return !dataHora.isAfter(agora);
    }

    /**
     * Informa se ainda sobra alguma poltrona.
     *
     * @return {@code true} se há ao menos uma poltrona não vendida
     */
    public boolean possuiVagas(){
        return assentosOcupados.size() < sala.getAssentos().size();
    }

    /**
     * Informa se uma poltrona existe na sala e ainda não foi vendida.
     *
     * @param numero número da poltrona
     * @return {@code true} se a poltrona pode ser vendida
     */
    public boolean assentoEstaDisponivel(int numero){
        return sala.possuiAssento(numero)
                && !assentosOcupados.contains(numero);
    }

    /**
     * Marca uma poltrona como vendida.
     *
     * @param numero número da poltrona
     * @throws IllegalStateException se a poltrona já está ocupada ou não existe na sala
     */
    public void ocuparAssento(int numero){
        if(!assentoEstaDisponivel(numero)){
            throw new IllegalStateException("Assento ocupado ou inexistente na sessão!");
        }
        assentosOcupados.add(numero);
    }

    /** Cancela a sessão (ela deixa de ficar à venda, mas continua registrada). */
    public void cancelar(){
        status = StatusSessao.CANCELADA;
    }

    //GETTERS

    /**
     * Identificador da sessão.
     *
     * @return o id (0 se ainda não foi gravada)
     */
    public int getId(){
        return id;
    }

    /**
     * Filme exibido na sessão.
     *
     * @return o filme
     */
    public Filme getFilme(){
        return filme;
    }

    /**
     * Sala onde a sessão acontece.
     *
     * @return a sala
     */
    public Sala getSala(){
        return sala;
    }

    /**
     * Dia e horário de início.
     *
     * @return a data e hora da sessão
     */
    public LocalDateTime getDataHora(){
        return dataHora;
    }

    /**
     * Situação atual da sessão.
     *
     * @return agendada, encerrada ou cancelada
     */
    public StatusSessao getStatus(){
        return status;
    }

    /**
     * Preço do ingresso inteiro nesta sessão (a meia é metade e a cortesia é zero).
     *
     * @return o valor, em reais
     */
    public double getPrecoBase(){
        return precoBase;
    }

    /**
     * Texto exibido no combo de sessões do cliente.
     *
     * @return título do filme e dia/hora, por exemplo "O Auto da Compadecida - 22/11 19:00"
     */
    @Override
    public String toString(){
        return filme.getTitulo() + " - " + dataHora.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }
}