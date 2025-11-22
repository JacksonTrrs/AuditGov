package org.example.database;

import org.example.model.*;

import java.sql.*;

//Todos os comandos SQL aqui
public class DadosDAO {

    //FALTA CONFIGURAR OS MÉTODOS

    public int salvarOuRecuperarOrgao(Connection conn, Orgao orgao) throws SQLException {

        // VERIFICA SE JÁ EXISTE
        String selectSQL = "SELECT * FROM orgao WHERE nome = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectSQL)) {
            ps.setString(1, orgao.getNome());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }

        //INSERE SE NÃO EXISTIR
        String insertSQL = "INSERT INTO orgao (nome) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
            ps.setString(1, orgao.getNome());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }else{
                throw new SQLException("Falha ao inserir orgao.");
            }
        }
    }
    //RETORNA O ID DA CIDADE E INSERE SE NÃO EXISTIR
    public int salvarOuRecuperarCidade(Connection conn, Cidade cidade) throws SQLException {
        //BUSCA
        String selectSQL = "SELECT id FROM cidade WHERE nome = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectSQL)){
            ps.setString(1, cidade.getNome());
            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                return rs.getInt("id");
            }
        }

        //INSERE
        String insertSQL = "INSERT INTO cidade (nome, uf) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, cidade.getNome());
            ps.setString(2, cidade.getUf());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }else {
                throw new SQLException("Falha ao inserir cidade.");
            }
        }
    }

    //INSERE AS VIAGENS JÁ COM AS FOREIGN KEYS RESOLVIDAS
    public void inserirViagem(Connection conn, Viagem viagem) throws SQLException {
        String sql = "INSERT INTO viagem (id_processo, data_inicio, valor_total, id_orgao, id_cidade) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, viagem.getIdProcesso());
            ps.setDate(2, Date.valueOf(viagem.getDataInicio()));
            ps.setDouble(3, viagem.getValorTotal());

            //FKS RESOLVIDAS
            ps.setInt(4, viagem.getOrgao().getId());
            ps.setInt(5, viagem.getDestino().getId());

            ps.executeUpdate();
        }
    }
}
