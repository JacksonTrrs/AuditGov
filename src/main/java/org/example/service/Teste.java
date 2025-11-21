package org.example.service;

import org.example.database.*;
import org.example.model.*;
import org.example.util.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;


public class Teste {
    private static final int LIMITE_LINHAS = 2; //Freio de mão para travar o pc com excesso de informações

    public void excetuarImportacao(String caminhoArquivo) throws SQLException {
        DadosDAO dao = new DadosDAO();

        System.out.println("Iniciando leitura de arquivo: " + caminhoArquivo);

        try (Connection conn = ConexaoFactory.getConexao(); BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            conn.setAutoCommit(false); // Performance: Desliga salvamento automático

            String linha;
            br.readLine(); //Executa uma vez para pular o cabeçalho
            int contador = 0;

            while ((linha = br.readLine()) != null) {
                if (contador >= LIMITE_LINHAS) {
                    System.out.println("!!! Limite de " + LIMITE_LINHAS + " atingido. Parando.");
                    break;
                }

                String[] dados = linha.split(";");

                for (int x = 0; x < dados.length; x++) {
                    System.out.print(x + ": " + dados[x] + "|");
                }
                System.out.println();
                contador++;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void testeTratamentoDados() {
        System.out.println(TratamentoDados.converterValor("1.500,00"));
    }
}