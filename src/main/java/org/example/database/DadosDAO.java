package org.example.database;

import org.example.model.*;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Data Access Object (DAO) - Responsável apenas por operações SQL puras.
 * Não contém lógica de negócio, cache ou normalização.
 * 
 * Para gestão de entidades únicas (sinônimos), use a classe GestaoEntidadesUnicas.
 */
public class DadosDAO {

    /**
     * Busca o ID de um órgão pelo nome normalizado.
     * Retorna null se não encontrar.
     */
    public Integer buscarIdOrgaoPorNome(Connection conn, String nomeNormalizado) throws SQLException {
        String sql = "SELECT id FROM orgao WHERE UPPER(TRIM(nome)) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeNormalizado);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return null;
    }
    
    /**
     * Insere um novo órgão no banco de dados.
     * Retorna o ID gerado.
     */
    public int inserirOrgao(Connection conn, Orgao orgao) throws SQLException {
        String sql = "INSERT INTO orgao (nome) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, orgao.getNome().trim());
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Erro ao inserir órgão: " + orgao.getNome());
    }
    
    /**
     * Busca o ID de uma cidade pelo nome e UF normalizados.
     * Retorna null se não encontrar.
     */
    public Integer buscarIdCidadePorNomeEUf(Connection conn, String nomeNormalizado, String ufNormalizado) throws SQLException {
        String sql = "SELECT id FROM cidade WHERE UPPER(TRIM(nome)) = ? AND UPPER(TRIM(uf)) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeNormalizado);
            stmt.setString(2, ufNormalizado);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return null;
    }
    
    /**
     * Insere uma nova cidade no banco de dados.
     * Retorna o ID gerado.
     */
    public int inserirCidade(Connection conn, Cidade cidade) throws SQLException {
        String sql = "INSERT INTO cidade (nome, uf) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, cidade.getNome().trim());
            stmt.setString(2, cidade.getUf().trim().toUpperCase());
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Erro ao inserir cidade: " + cidade.getNome() + "/" + cidade.getUf());
    }

    /**
     * @deprecated Use GestaoEntidadesUnicas.salvarOuRecuperarOrgao() em vez disso.
     * Mantido apenas para compatibilidade.
     */
    @Deprecated
    public int salvarOuRecuperarOrgao(Connection conn, Orgao orgao) throws SQLException {
        // Delega para o serviço de gestão de entidades únicas
        org.example.service.GestaoEntidadesUnicas gestao = new org.example.service.GestaoEntidadesUnicas();
        return gestao.salvarOuRecuperarOrgao(conn, orgao);
    }

    /**
     * @deprecated Use GestaoEntidadesUnicas.salvarOuRecuperarCidade() em vez disso.
     * Mantido apenas para compatibilidade.
     */
    @Deprecated
    public int salvarOuRecuperarCidade(Connection conn, Cidade cidade) throws SQLException {
        // Delega para o serviço de gestão de entidades únicas
        org.example.service.GestaoEntidadesUnicas gestao = new org.example.service.GestaoEntidadesUnicas();
        return gestao.salvarOuRecuperarCidade(conn, cidade);
    }

    public void inserirViagem(Connection conn, Viagem viagem) {

    }

    //LISTA AS CIDADES MAIS VISITADAS POR UF
    public List<String> listarCidadesMaisVisitadasPorUF(Connection conn) throws SQLException {
        String sql = """
            SELECT 
                c.uf,
                c.nome AS cidade,
                COUNT(*) AS total
            FROM viagem v
            JOIN cidade c ON v.id_cidade = c.id
            GROUP BY c.uf, c.nome
            ORDER BY c.uf, total DESC;
        """;

        List<String> resultado = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String linha = String.format(
                    "%s - %s (%d viagens)",
                    rs.getString("uf"),
                    rs.getString("cidade"),
                    rs.getInt("total")
                );
                resultado.add(linha);
            }
        }

        return resultado;
    }


    
}

