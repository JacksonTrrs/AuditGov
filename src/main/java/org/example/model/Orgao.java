package org.example.model;

// Esta classe representa a tabela "orgao" do MariaDB
public class Orgao {

    private int id;
    private String nome;

    public Orgao() {
    }

    public Orgao(String nome) {
        this.nome = nome;
    }

    // --- Getters e Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return "Orgao [id=" + id + ", nome=" + nome + "]";
    }
}