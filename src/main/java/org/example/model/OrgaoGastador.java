package org.example.model;

/**
 * DTO (Data Transfer Object) para representar um órgão com seu valor total gasto.
 * Usado para relatórios de maiores gastadores.
 */
public class OrgaoGastador {
    private String nomeOrgao;
    private double valorTotal;

    public OrgaoGastador(String nomeOrgao, double valorTotal) {
        this.nomeOrgao = nomeOrgao;
        this.valorTotal = valorTotal;
    }

    public String getNomeOrgao() {
        return nomeOrgao;
    }

    public void setNomeOrgao(String nomeOrgao) {
        this.nomeOrgao = nomeOrgao;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }
}

