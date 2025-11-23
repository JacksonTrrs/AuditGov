public class CidadeVisitaDTO {
    private String cidade;
    private String uf;
    private long total;

    public CidadeVisitaDTO(String cidade, String uf, long total) {
        this.cidade = cidade;
        this.uf = uf;
        this.total = total;
    }

    public String getCidade() { 
        return cidade; 
    }
    public String getUf() { 
        return uf; 
    }
    public long getTotal() { 
        return total; 
    }

    @Override
    public String toString() {
        return cidade + "/" + uf + " - " + total + " viagens";
    }
}
