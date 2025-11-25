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

    /**
     * Busca viagens com informações de órgão e cidade.
     * O DAO aceita ordenação como parâmetro, mas não decide qual ordenação usar.
     * A decisão de ordenação é responsabilidade da camada de serviço.
     * 
     * @param orderBy Cláusula ORDER BY (ex: "v.data_inicio DESC"). Pode ser null para sem ordenação.
     */
    public java.util.List<Viagem> buscarViagens(Connection conn, String orderBy, int limite, int offset) throws SQLException {
        java.util.List<Viagem> viagens = new java.util.ArrayList<>();
        String sql = "SELECT v.id_processo, v.data_inicio, v.valor_total, " +
                     "o.id as orgao_id, o.nome as orgao_nome, " +
                     "c.id as cidade_id, c.nome as cidade_nome, c.uf " +
                     "FROM viagem v " +
                     "INNER JOIN orgao o ON v.fk_orgao = o.id " +
                     "INNER JOIN cidade c ON v.fk_destino = c.id " +
                     (orderBy != null && !orderBy.isEmpty() ? "ORDER BY " + orderBy + " " : "") +
                     "LIMIT ? OFFSET ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limite);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Construção de objetos de domínio - responsabilidade do DAO
                    // (padrão comum em DAOs, desde que não contenha lógica de negócio)
                    Orgao orgao = new Orgao(rs.getString("orgao_nome"));
                    orgao.setId(rs.getInt("orgao_id"));

                    Cidade cidade = new Cidade(rs.getString("cidade_nome"), rs.getString("uf"));
                    cidade.setId(rs.getInt("cidade_id"));

                    Viagem viagem = new Viagem(
                            rs.getString("id_processo"),
                            rs.getDate("data_inicio").toLocalDate(),
                            rs.getDouble("valor_total"),
                            orgao,
                            cidade
                    );
                    viagens.add(viagem);
                }
            }
        }
        return viagens;
    }

    /**
     * Conta o total de viagens no banco de dados.
     */
    public int contarViagens(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM viagem";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    /**
     * Busca dados brutos de estatísticas das viagens.
     * Retorna um array com os valores calculados pelo banco na ordem:
     * [total_viagens, valor_total, valor_medio, valor_minimo, valor_maximo]
     * A estrutura e nomes dos campos retornados são definidos pela camada de serviço.
     */
    public Object[] buscarDadosEstatisticas(Connection conn) throws SQLException {
        String sql = "SELECT " +
                     "COUNT(*) as total_viagens, " +
                     "SUM(valor_total) as valor_total, " +
                     "AVG(valor_total) as valor_medio, " +
                     "MIN(valor_total) as valor_minimo, " +
                     "MAX(valor_total) as valor_maximo " +
                     "FROM viagem";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return new Object[]{
                    rs.getLong("total_viagens"),
                    rs.getDouble("valor_total"),
                    rs.getDouble("valor_medio"),
                    rs.getDouble("valor_minimo"),
                    rs.getDouble("valor_maximo")
                };
            }
        }
        // Retorna valores zerados se não houver dados
        return new Object[]{0L, 0.0, 0.0, 0.0, 0.0};
    }

    /**
     * Busca dados brutos dos órgãos com maior soma de valor total.
     * Retorna uma lista de arrays [nome_orgao, valor_total] ordenados por valor_total DESC.
     * O DAO não decide quantos retornar - isso é responsabilidade da camada de serviço.
     * 
     * @param limite Número máximo de órgãos a retornar
     */
    public java.util.List<Object[]> buscarDadosOrgaosMaioresGastadores(Connection conn, int limite) throws SQLException {
        java.util.List<Object[]> resultados = new java.util.ArrayList<>();
        String sql = "SELECT o.nome as nome_orgao, SUM(v.valor_total) as valor_total " +
                     "FROM viagem v " +
                     "INNER JOIN orgao o ON v.fk_orgao = o.id " +
                     "GROUP BY o.id, o.nome " +
                     "ORDER BY valor_total DESC " +
                     "LIMIT ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Retorna dados brutos: [nome_orgao, valor_total]
                    resultados.add(new Object[]{
                        rs.getString("nome_orgao"),
                        rs.getDouble("valor_total")
                    });
                }
            }
        }
        return resultados;
    }

    /**
     * Busca dados brutos dos destinos mais frequentes agrupados por cidade e UF.
     * Retorna uma lista de arrays [nome_cidade, uf, quantidade] ordenados por quantidade DESC.
     * O DAO não decide quantos retornar - isso é responsabilidade da camada de serviço.
     * 
     * @param limite Número máximo de destinos a retornar
     */
    public java.util.List<Object[]> buscarDadosDestinosFrequentes(Connection conn, int limite) throws SQLException {
        java.util.List<Object[]> resultados = new java.util.ArrayList<>();
        String sql = "SELECT c.nome as nome_cidade, c.uf, COUNT(*) as quantidade " +
                     "FROM viagem v " +
                     "INNER JOIN cidade c ON v.fk_destino = c.id " +
                     "GROUP BY c.id, c.nome, c.uf " +
                     "ORDER BY quantidade DESC " +
                     "LIMIT ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Retorna dados brutos: [nome_cidade, uf, quantidade]
                    resultados.add(new Object[]{
                        rs.getString("nome_cidade"),
                        rs.getString("uf"),
                        rs.getInt("quantidade")
                    });
                }
            }
        }
        return resultados;
    }
}

