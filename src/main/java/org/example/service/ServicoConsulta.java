package org.example.service;

import org.example.database.ConexaoFactory;
import org.example.database.DadosDAO;
import org.example.dto.DestinoFrequente;
import org.example.dto.OrgaoGastador;
import org.example.model.Viagem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço responsável por consultas ao banco de dados.
 * Responsável por:
 * - Construir objetos de domínio a partir dos dados brutos do DAO
 * - Aplicar regras de negócio (ordenação, formatação)
 * - Definir estrutura de dados de retorno
 */
public class ServicoConsulta {

    private final DadosDAO dao;

    public ServicoConsulta() {
        this.dao = new DadosDAO();
    }

    /**
     * Busca viagens com paginação.
     * Aplica regra de negócio: ordena por data de início (mais recente primeiro).
     * A decisão de ordenação é uma regra de negócio, não do DAO.
     */
    public List<Viagem> buscarViagens(int limite, int offset) throws SQLException {
        try (Connection conn = ConexaoFactory.getConexao()) {
            // Regra de negócio: ordenação por data mais recente
            String orderBy = "v.data_inicio DESC";
            return dao.buscarViagens(conn, orderBy, limite, offset);
        }
    }

    /**
     * Conta o total de viagens.
     */
    public int contarTotalViagens() throws SQLException {
        try (Connection conn = ConexaoFactory.getConexao()) {
            return dao.contarViagens(conn);
        }
    }

    /**
     * Busca estatísticas gerais das viagens.
     * Define a estrutura de dados retornada (nomes das chaves).
     * A estrutura de retorno é uma convenção de negócio, não do DAO.
     */
    public Map<String, Object> buscarEstatisticas() throws SQLException {
        try (Connection conn = ConexaoFactory.getConexao()) {
            Object[] dados = dao.buscarDadosEstatisticas(conn);
            
            Map<String, Object> stats = new HashMap<>();
            // Estrutura de dados definida pela camada de serviço (regra de negócio)
            // Ordem: [total_viagens, valor_total, valor_medio, valor_minimo, valor_maximo]
            stats.put("totalViagens", dados[0]);
            stats.put("valorTotal", dados[1]);
            stats.put("valorMedio", dados[2]);
            stats.put("valorMinimo", dados[3]);
            stats.put("valorMaximo", dados[4]);
            
            return stats;
        }
    }

    /**
     * Busca os N órgãos com maior soma de valor total.
     * Regra de negócio: retorna os 5 maiores gastadores por padrão.
     * 
     * @param quantidade Quantidade de órgãos a retornar (padrão: 5)
     * @return Lista de OrgaoGastador ordenada por valor total (maior para menor)
     */
    public List<OrgaoGastador> buscarMaioresGastadores(int quantidade) throws SQLException {
        try (Connection conn = ConexaoFactory.getConexao()) {
            // Regra de negócio: busca os N maiores gastadores
            List<Object[]> dadosBrutos = dao.buscarDadosOrgaosMaioresGastadores(conn, quantidade);
            
            // Transforma dados brutos em objetos de domínio
            List<OrgaoGastador> gastadores = new ArrayList<>();
            for (Object[] dados : dadosBrutos) {
                String nomeOrgao = (String) dados[0];
                double valorTotal = ((Number) dados[1]).doubleValue();
                gastadores.add(new OrgaoGastador(nomeOrgao, valorTotal));
            }
            
            return gastadores;
        }
    }

    /**
     * Busca os 5 órgãos com maior soma de valor total (método de conveniência).
     */
    public List<OrgaoGastador> buscarTop5MaioresGastadores() throws SQLException {
        return buscarMaioresGastadores(5);
    }

    /**
     * Busca os N destinos mais frequentes (cidades mais visitadas).
     * Agrupa por cidade e UF para garantir que cidades com mesmo nome em UFs diferentes sejam tratadas separadamente.
     * 
     * @param quantidade Quantidade de destinos a retornar
     * @return Lista de DestinoFrequente ordenada por quantidade de viagens (maior para menor)
     */
    public List<DestinoFrequente> buscarDestinosFrequentes(int quantidade) throws SQLException {
        try (Connection conn = ConexaoFactory.getConexao()) {
            // Busca os N destinos mais frequentes
            List<Object[]> dadosBrutos = dao.buscarDadosDestinosFrequentes(conn, quantidade);
            
            // Transforma dados brutos em objetos de domínio
            List<DestinoFrequente> destinos = new ArrayList<>();
            for (Object[] dados : dadosBrutos) {
                String nomeCidade = (String) dados[0];
                String uf = (String) dados[1];
                int quantidadeViagens = ((Number) dados[2]).intValue();
                destinos.add(new DestinoFrequente(nomeCidade, uf, quantidadeViagens));
            }
            
            return destinos;
        }
    }

    /**
     * Busca os 10 destinos mais frequentes (método de conveniência).
     */
    public List<DestinoFrequente> buscarTop10DestinosFrequentes() throws SQLException {
        return buscarDestinosFrequentes(10);
    }
}

