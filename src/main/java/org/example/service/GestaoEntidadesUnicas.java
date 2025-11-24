package org.example.service;

import org.example.database.DadosDAO;
import org.example.model.Cidade;
import org.example.model.Orgao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Serviço responsável pela gestão de entidades únicas (sinônimos).
 * Gerencia cache e normalização para evitar duplicatas de Órgãos e Cidades.
 * <p>
 * Separação de responsabilidades:
 * - Esta classe: lógica de negócio (cache, normalização)
 * - DadosDAO: apenas operações SQL puras
 */
public class GestaoEntidadesUnicas {

    private final DadosDAO dao;

    // Cache em memória para evitar consultas repetidas ao banco
    // Chave: nome normalizado do órgão -> Valor: ID
    private final Map<String, Integer> cacheOrgaos = new HashMap<>();

    // Cache em memória para cidades
    // Chave: "nomeCidade|UF" normalizado -> Valor: ID
    private final Map<String, Integer> cacheCidades = new HashMap<>();

    public GestaoEntidadesUnicas() {
        this.dao = new DadosDAO();
    }

    /**
     * Normaliza o nome para evitar duplicatas por diferenças de formatação.
     * Remove espaços extras, converte para maiúsculas e trim.
     */
    private String normalizarNome(String nome) {
        if (nome == null) return "";
        return nome.trim().toUpperCase().replaceAll("\\s+", " ");
    }

    /**
     * Verifica se um órgão já existe no banco (usando cache ou SQL).
     * Se existir, retorna o ID existente.
     * Se não existir, insere e retorna o novo ID.
     * <p>
     * Exemplo: Se "MINISTERIO DA SAUDE" já existe com ID 50,
     * retornará 50 mesmo se chamado com "ministerio da saude" (minúsculas).
     */
    public int salvarOuRecuperarOrgao(Connection conn, Orgao orgao) throws SQLException {
        String nomeNormalizado = normalizarNome(orgao.getNome());

        // 1. Verifica no cache primeiro (performance)
        if (cacheOrgaos.containsKey(nomeNormalizado)) {
            return cacheOrgaos.get(nomeNormalizado);
        }

        // 2. Consulta no banco de dados (delega para o DAO)
        Integer idExistente = dao.buscarIdOrgaoPorNome(conn, nomeNormalizado);

        if (idExistente != null) {
            // Encontrou no banco, atualiza o cache
            cacheOrgaos.put(nomeNormalizado, idExistente);
            return idExistente;
        }

        // 3. Se não encontrou, insere novo registro (delega para o DAO)
        int novoId = dao.inserirOrgao(conn, orgao);

        // Atualiza o cache
        cacheOrgaos.put(nomeNormalizado, novoId);
        return novoId;
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

        // 2. Consulta no banco de dados (delega para o DAO)
        Integer idExistente = dao.buscarIdCidadePorNomeEUf(conn, nomeNormalizado, ufNormalizado);

        if (idExistente != null) {
            // Encontrou no banco, atualiza o cache
            cacheCidades.put(chaveCache, idExistente);
            return idExistente;
        }

        // 3. Se não encontrou, insere novo registro (delega para o DAO)
        int novoId = dao.inserirCidade(conn, cidade);

        // Atualiza o cache
        cacheCidades.put(chaveCache, novoId);
        return novoId;
    }

    /**
     * Limpa o cache de órgãos (útil para testes ou reinicialização)
     */
    public void limparCacheOrgaos() {
        cacheOrgaos.clear();
    }

    /**
     * Limpa o cache de cidades (útil para testes ou reinicialização)
     */
    public void limparCacheCidades() {
        cacheCidades.clear();
    }

    /**
     * Limpa todos os caches
     */
    public void limparTodosCaches() {
        cacheOrgaos.clear();
        cacheCidades.clear();
    }

    /**
     * Retorna o tamanho atual do cache de órgãos (útil para monitoramento)
     */
    public int tamanhoCacheOrgaos() {
        return cacheOrgaos.size();
    }

    /**
     * Retorna o tamanho atual do cache de cidades (útil para monitoramento)
     */
    public int tamanhoCacheCidades() {
        return cacheCidades.size();
    }
}

