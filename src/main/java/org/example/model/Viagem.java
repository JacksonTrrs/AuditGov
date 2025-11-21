package org.example.model;

import java.time.LocalDate;

//Espelho da tabela Viagem
public class Viagem {
    private String idProcesso;
    private LocalDate dataInicio;
    private double valorTotal;
    private Orgao orgao;
    private Cidade destino;

    public Viagem() {
    }

    public Viagem(String idProcesso, LocalDate dataInicio, double valorTotal, Orgao orgao, Cidade destino) {
        this.idProcesso = idProcesso;
        this.dataInicio = dataInicio;
        this.valorTotal = valorTotal;
        this.orgao = orgao;
        this.destino = destino;
    }

    // --- Getters e Setters ---

    public String getIdProcesso() {
        return idProcesso;
    }

    public void setIdProcesso(String idProcesso) {
        this.idProcesso = idProcesso;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Orgao getOrgao() {
        return orgao;
    }

    public void setOrgao(Orgao orgao) {
        this.orgao = orgao;
    }

    public Cidade getDestino() {
        return destino;
    }

    public void setDestino(Cidade destino) {
        this.destino = destino;
    }

    @Override
    public String toString() {
        return "Viagem [Processo=" + idProcesso + ", Valor=R$ " + valorTotal + ", Destino=" + destino.getNome() + "]";
    }
}
