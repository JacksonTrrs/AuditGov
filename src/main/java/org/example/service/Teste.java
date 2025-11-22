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

    /**
     * Testa a gestão de entidades únicas (sinônimos)
     * Demonstra que órgãos e cidades duplicados não são inseridos novamente
     */
    public static void testeGestaoEntidadesUnicas() throws SQLException {
        System.out.println("\n=== TESTE: Gestão de Entidades Únicas (Sinônimos) ===\n");
        
        DadosDAO dao = new DadosDAO();
        
        try (Connection conn = ConexaoFactory.getConexao()) {
            conn.setAutoCommit(false);
            
            // Teste 1: Inserir órgão pela primeira vez
            System.out.println("Teste 1: Inserindo 'MINISTERIO DA SAUDE' pela primeira vez...");
            Orgao orgao1 = new Orgao("MINISTERIO DA SAUDE");
            int id1 = dao.salvarOuRecuperarOrgao(conn, orgao1);
            System.out.println("  -> ID retornado: " + id1);
            System.out.println("  -> Cache de órgãos: " + DadosDAO.tamanhoCacheOrgaos() + " entidade(s)");
            
            // Teste 2: Tentar inserir o mesmo órgão com formatação diferente
            System.out.println("\nTeste 2: Tentando inserir 'ministerio da saude' (minúsculas)...");
            Orgao orgao2 = new Orgao("ministerio da saude");
            int id2 = dao.salvarOuRecuperarOrgao(conn, orgao2);
            System.out.println("  -> ID retornado: " + id2);
            System.out.println("  -> Cache de órgãos: " + DadosDAO.tamanhoCacheOrgaos() + " entidade(s)");
            
            if (id1 == id2) {
                System.out.println("  ✓ SUCESSO: Mesmo ID retornado! Duplicata evitada.");
            } else {
                System.out.println("  ✗ ERRO: IDs diferentes! Duplicata foi criada.");
            }
            
            // Teste 3: Tentar inserir com espaços extras
            System.out.println("\nTeste 3: Tentando inserir '  MINISTERIO DA SAUDE  ' (com espaços)...");
            Orgao orgao3 = new Orgao("  MINISTERIO DA SAUDE  ");
            int id3 = dao.salvarOuRecuperarOrgao(conn, orgao3);
            System.out.println("  -> ID retornado: " + id3);
            
            if (id1 == id3) {
                System.out.println("  ✓ SUCESSO: Mesmo ID retornado! Espaços ignorados.");
            } else {
                System.out.println("  ✗ ERRO: IDs diferentes!");
            }
            
            // Teste 4: Testar com cidade
            System.out.println("\nTeste 4: Inserindo cidade 'BRASILIA/DF' pela primeira vez...");
            Cidade cidade1 = new Cidade("BRASILIA", "DF");
            int idCidade1 = dao.salvarOuRecuperarCidade(conn, cidade1);
            System.out.println("  -> ID retornado: " + idCidade1);
            System.out.println("  -> Cache de cidades: " + DadosDAO.tamanhoCacheCidades() + " entidade(s)");
            
            System.out.println("\nTeste 5: Tentando inserir 'brasilia/df' (minúsculas)...");
            Cidade cidade2 = new Cidade("brasilia", "df");
            int idCidade2 = dao.salvarOuRecuperarCidade(conn, cidade2);
            System.out.println("  -> ID retornado: " + idCidade2);
            
            if (idCidade1 == idCidade2) {
                System.out.println("  ✓ SUCESSO: Mesmo ID retornado! Duplicata evitada.");
            } else {
                System.out.println("  ✗ ERRO: IDs diferentes!");
            }
            
            // Rollback para não persistir os dados de teste
            conn.rollback();
            System.out.println("\n=== Rollback executado (dados de teste não foram salvos) ===");
            
            // Limpar cache após teste
            DadosDAO.limparTodosCaches();
            System.out.println("Cache limpo.\n");
            
        } catch (SQLException e) {
            System.err.println("Erro no teste: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}