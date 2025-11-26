package org.example.dto;

/**
 * DTO (Data Transfer Object) para representar um órgão com seu valor total gasto.
 * Usado para relatórios de maiores gastadores.
 */
public class OrgaoGastadorDTO {
    private String nomeOrgao;
    private double valorTotal;

    public OrgaoGastadorDTO(String nomeOrgao, double valorTotal) {
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


