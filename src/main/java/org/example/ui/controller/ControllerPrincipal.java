package org.example.ui.controller;

import org.example.service.ServicoConsulta;
import org.example.service.ServicoImportacao;
import org.example.dto.DestinoFrequente;
import org.example.dto.OrgaoGastador;
import org.example.model.Viagem;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controlador principal que gerencia as ações da interface.
 * Separa a lógica de negócio da interface gráfica.
 */
public class ControllerPrincipal {

    private final ServicoConsulta servicoConsulta;

    public ControllerPrincipal() {
        this.servicoConsulta = new ServicoConsulta();
    }

    /**
     * Executa a importação de forma assíncrona e atualiza o progresso.
     */
    public CompletableFuture<String> importarArquivo(String caminhoArquivo, 
                                                      JProgressBar progressBar, 
                                                      JLabel statusLabel) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Atualiza status
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Importando...");
                    progressBar.setIndeterminate(true);
                });

                // Executa importação
                ServicoImportacao.excetuarImportacao(caminhoArquivo);

                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                });

                return "Importação concluída com sucesso!";
            } catch (SQLException e) {
                return "Erro na importação: " + e.getMessage();
            } catch (Exception e) {
                return "Erro inesperado: " + e.getMessage();
            }
        });
    }

    /**
     * Busca viagens de forma assíncrona.
     */
    public CompletableFuture<List<Viagem>> buscarViagens(int limite, int offset) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return servicoConsulta.buscarViagens(limite, offset);
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao buscar viagens: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Busca estatísticas de forma assíncrona.
     */
    public CompletableFuture<Map<String, Object>> buscarEstatisticas() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return servicoConsulta.buscarEstatisticas();
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao buscar estatísticas: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Conta total de viagens de forma assíncrona.
     */
    public CompletableFuture<Integer> contarTotalViagens() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return servicoConsulta.contarTotalViagens();
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao contar viagens: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Busca maiores gastadores de forma assíncrona.
     */
    public CompletableFuture<List<OrgaoGastador>> buscarMaioresGastadores(int quantidade) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return servicoConsulta.buscarMaioresGastadores(quantidade);
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao buscar maiores gastadores: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Busca top 5 maiores gastadores de forma assíncrona.
     */
    public CompletableFuture<List<OrgaoGastador>> buscarTop5MaioresGastadores() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return servicoConsulta.buscarTop5MaioresGastadores();
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao buscar maiores gastadores: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Busca destinos frequentes de forma assíncrona.
     */
    public CompletableFuture<List<DestinoFrequente>> buscarDestinosFrequentes(int quantidade) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return servicoConsulta.buscarDestinosFrequentes(quantidade);
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao buscar destinos frequentes: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Busca top 10 destinos frequentes de forma assíncrona.
     */
    public CompletableFuture<List<DestinoFrequente>> buscarTop10DestinosFrequentes() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return servicoConsulta.buscarTop10DestinosFrequentes();
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao buscar destinos frequentes: " + e.getMessage(), e);
            }
        });
    }
}

