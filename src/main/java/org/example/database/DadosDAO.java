package org.example.database;

import org.example.model.*;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

//Todos os comandos SQL aqui
public class DadosDAO {

    // Cache em memória para evitar consultas repetidas ao banco
    // Chave: nome normalizado do órgão -> Valor: ID
    private static final Map<String, Integer> cacheOrgaos = new HashMap<>();
    
    // Cache em memória para cidades
    // Chave: "nomeCidade|UF" normalizado -> Valor: ID
    private static final Map<String, Integer> cacheCidades = new HashMap<>();

    /**
     * Normaliza o nome para evitar duplicatas por diferenças de formatação
     * Remove espaços extras, converte para maiúsculas e trim
     */
    private String normalizarNome(String nome) {
        if (nome == null) return "";
        return nome.trim().toUpperCase().replaceAll("\\s+", " ");
    }

    /**
     * Verifica se um órgão já existe no banco (usando cache ou SQL).
     * Se existir, retorna o ID existente.
     * Se não existir, insere e retorna o novo ID.
     */
    public int salvarOuRecuperarOrgao(Connection conn, Orgao orgao) throws SQLException {
        String nomeNormalizado = normalizarNome(orgao.getNome());
        
        // 1. Verifica no cache primeiro (performance)
        if (cacheOrgaos.containsKey(nomeNormalizado)) {
            return cacheOrgaos.get(nomeNormalizado);
        }
        
        // 2. Consulta no banco de dados
        String sqlSelect = "SELECT id FROM orgao WHERE UPPER(TRIM(nome)) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {
            stmt.setString(1, nomeNormalizado);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int idExistente = rs.getInt("id");
                    // Atualiza o cache
                    cacheOrgaos.put(nomeNormalizado, idExistente);
                    return idExistente;
                }
            }
        }
        
        // 3. Se não encontrou, insere novo registro
        String sqlInsert = "INSERT INTO orgao (nome) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, orgao.getNome().trim()); // Salva o nome original (não normalizado)
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int novoId = rs.getInt(1);
                    // Atualiza o cache
                    cacheOrgaos.put(nomeNormalizado, novoId);
                    return novoId;
                }
            }
        }
        
        throw new SQLException("Erro ao salvar ou recuperar órgão: " + orgao.getNome());
    }

    /**
     * Verifica se uma cidade já existe no banco (usando cache ou SQL).
     * Se existir, retorna o ID existente.
     * Se não existir, insere e retorna o novo ID.
     * Considera nome + UF como chave única.
     */
    public int salvarOuRecuperarCidade(Connection conn, Cidade cidade) throws SQLException {
        String nomeNormalizado = normalizarNome(cidade.getNome());
        String ufNormalizado = normalizarNome(cidade.getUf());
        String chaveCache = nomeNormalizado + "|" + ufNormalizado;
        
        // 1. Verifica no cache primeiro (performance)
        if (cacheCidades.containsKey(chaveCache)) {
            return cacheCidades.get(chaveCache);
        }
        
        // 2. Consulta no banco de dados
        String sqlSelect = "SELECT id FROM cidade WHERE UPPER(TRIM(nome)) = ? AND UPPER(TRIM(uf)) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {
            stmt.setString(1, nomeNormalizado);
            stmt.setString(2, ufNormalizado);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int idExistente = rs.getInt("id");
                    // Atualiza o cache
                    cacheCidades.put(chaveCache, idExistente);
                    return idExistente;
                }
            }
        }
        
        // 3. Se não encontrou, insere novo registro
        String sqlInsert = "INSERT INTO cidade (nome, uf) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, cidade.getNome().trim()); // Salva o nome original
            stmt.setString(2, cidade.getUf().trim().toUpperCase()); // UF sempre em maiúsculas
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int novoId = rs.getInt(1);
                    // Atualiza o cache
                    cacheCidades.put(chaveCache, novoId);
                    return novoId;
                }
            }
        }
        
        throw new SQLException("Erro ao salvar ou recuperar cidade: " + cidade.getNome() + "/" + cidade.getUf());
    }

    public void inserirViagem(Connection conn, Viagem viagem) {

    }

    /**
     * Limpa o cache de órgãos (útil para testes ou reinicialização)
     */
    public static void limparCacheOrgaos() {
        cacheOrgaos.clear();
    }

    /**
     * Limpa o cache de cidades (útil para testes ou reinicialização)
     */
    public static void limparCacheCidades() {
        cacheCidades.clear();
    }

    /**
     * Limpa todos os caches
     */
    public static void limparTodosCaches() {
        cacheOrgaos.clear();
        cacheCidades.clear();
    }

    /**
     * Retorna o tamanho atual do cache de órgãos (útil para monitoramento)
     */
    public static int tamanhoCacheOrgaos() {
        return cacheOrgaos.size();
    }

    /**
     * Retorna o tamanho atual do cache de cidades (útil para monitoramento)
     */
    public static int tamanhoCacheCidades() {
        return cacheCidades.size();
    }
}
