package org.example.database;

import org.example.model.Cidade;
import org.example.model.Orgao;
import org.example.model.Viagem;

import java.sql.*;

/**
 * Data Access Object (DAO) - Responsável apenas por operações SQL puras.
 * Não contém lógica de negócio, cache ou normalização.
 * <p>
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

    /**
     * Insere a viagem na tabela fato.
     * Converte a data do Java para SQL e extrai os IDs dos objetos relacionados.
     */
    public void inserirViagem(Connection conn, Viagem viagem) throws SQLException {
        String sql = "INSERT INTO viagem (id_processo, data_inicio, valor_total, fk_orgao, fk_destino) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            // 1. ID do Processo (Texto)
            stmt.setString(1, viagem.getIdProcesso());

            // 2. Data (Conversão Obrigatória: LocalDate -> java.sql.Date)
            stmt.setDate(2, java.sql.Date.valueOf(viagem.getDataInicio()));

            // 3. Valor Total
            stmt.setDouble(3, viagem.getValorTotal());

            // 4. FK Órgão (Pega o ID de dentro do objeto Orgao)
            stmt.setInt(4, viagem.getOrgao().getId());

            // 5. FK Destino (Pega o ID de dentro do objeto Cidade)
            stmt.setInt(5, viagem.getDestino().getId());

            // Executa
            stmt.executeUpdate();
        }
    }
}

