package org.example.model;

/**
 * DTO (Data Transfer Object) para representar um destino frequente.
 * Usado para relatórios de cidades mais visitadas.
 */
public class DestinoFrequente {
    private String nomeCidade;
    private String uf;
    private int quantidadeViagens;

    public DestinoFrequente(String nomeCidade, String uf, int quantidadeViagens) {
        this.nomeCidade = nomeCidade;
        this.uf = uf;
        this.quantidadeViagens = quantidadeViagens;
    }

    public String getNomeCidade() {
        return nomeCidade;
    }

    public void setNomeCidade(String nomeCidade) {
        this.nomeCidade = nomeCidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public int getQuantidadeViagens() {
        return quantidadeViagens;
    }

    public void setQuantidadeViagens(int quantidadeViagens) {
        this.quantidadeViagens = quantidadeViagens;
    }
}

