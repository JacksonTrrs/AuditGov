package org.example.service;

import org.example.database.*;
import org.example.model.*;
import org.example.util.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;


public class ServicoImportacao {
    private static final int LIMITE_LINHAS = 20000; //Freio de mão para travar o pc com excesso de informações

    public void excetuarImportacao(String caminhoArquivo) throws SQLException {
        DadosDAO dao = new DadosDAO();
        GestaoEntidadesUnicas gestaoEntidades = new GestaoEntidadesUnicas();

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
                int totalColunas = dados.length;

                //Validação básica para não quebrar
                if (dados.length < 15) continue;

                // --- 1. LIMPEZA (Usando a classe utilitária que faremos a seguir) ---
                // Ajustando os índices

                /*
                Os valores variam bastante a coluna, a melhor maneira para localizá-los é buscando de trás para frente.
                O array começa no 0 e o tamanho é N.
                Se baseando no cabeçalho: ...;Diárias;Passagens;Devolução;Outros
                Outros = índice [total - 1]
                Devolução = índice [total - 2]
                Passagens = índice [total - 3]
                Diárias = índice [total - 4]
                */

                String idProcesso = TratamentoDados.limparTexto(dados[0]);
                String nomeOrgao = TratamentoDados.limparTexto(dados[6]);
                String destinoBruto = TratamentoDados.limparTexto(dados[totalColunas - 6]);

                String strData = TratamentoDados.limparTexto(dados[14]);
                LocalDate dataInicio = TratamentoDados.converterData(strData);

                //LOCALIZANDO, CONVERTENDO E SOMANDO OS VALORES

                String strValorDiarias = TratamentoDados.limparTexto(dados[totalColunas-4]);
                String strValorPassagens = TratamentoDados.limparTexto(dados[totalColunas-3]);
                String strValorDevolucao = TratamentoDados.limparTexto(dados[totalColunas-2]);
                String strValorOutrosGastos = TratamentoDados.limparTexto(dados[totalColunas-1]);

                Double valorDiarias = TratamentoDados.converterValor(strValorDiarias);
                Double valorPassagem = TratamentoDados.converterValor(strValorPassagens);
                Double valorDevolucao = TratamentoDados.converterValor(strValorDevolucao);
                Double valorOutrosGastos = TratamentoDados.converterValor(strValorOutrosGastos);

                Double valorTotal = valorDiarias + valorPassagem + valorOutrosGastos - valorDevolucao;

                // --- 2. PADRONIZAÇÃO ---
                String[] localizacao = TratamentoDados.separarCidadeUF(destinoBruto);
                String nomeCidade = localizacao[0];
                String uf = localizacao[1];

                // --- 3. PERSISTÊNCIA (usando GestaoEntidadesUnicas) ---
                try {
                    //Cria Objetos temporários
                    Orgao orgao = new Orgao(nomeOrgao);
                    Cidade cidade = new Cidade(nomeCidade, uf);

                    //Recupera IDs do banco (usando gestão de entidades únicas)
                    int idOrgao = gestaoEntidades.salvarOuRecuperarOrgao(conn, orgao);
                    int idCidade = gestaoEntidades.salvarOuRecuperarCidade(conn, cidade);

                    // Atualiza os objetos com o ID correto
                    orgao.setId(idOrgao);
                    cidade.setId(idCidade);

                    //Salva a viagem
                    Viagem viagem = new Viagem(idProcesso, dataInicio, valorTotal, orgao, cidade);
                    dao.inserirViagem(conn, viagem);

                    contador++;
                    if (contador % 1000 == 0) System.out.print("."); // Barra de Progresso simples
                } catch (Exception e) {
                    System.err.println("Erro na linha " + contador + ": " + e.getMessage());
                }
            }

            conn.commit(); //Salva tudo de uma vez
            System.out.println("\nSUCESSO! " + contador + " viagens importadas.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
