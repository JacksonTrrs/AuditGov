package org.example.service;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.example.database.ConexaoFactory;
import org.example.database.DadosDAO;
import org.example.model.Cidade;
import org.example.model.Orgao;
import org.example.model.Viagem;
import org.example.util.TratamentoDados;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;


public class ServicoImportacao {
    private static final int LIMITE_LINHAS = 20000; //Freio de mão para travar o pc com excesso de informações

    public static void excetuarImportacao(String caminhoArquivo) throws SQLException {
        DadosDAO dao = new DadosDAO();
        GestaoEntidadesUnicas gestaoEntidades = new GestaoEntidadesUnicas();

        System.out.println("Iniciando leitura de arquivo: " + caminhoArquivo);

        try (Connection conn = ConexaoFactory.getConexao();
             InputStreamReader reader = new InputStreamReader(new FileInputStream(caminhoArquivo), StandardCharsets.ISO_8859_1);

             // 2. Configuramos o OpenCSV para entender que o separador é PONTO E VÍRGULA (;)
             // Se não fizer isso, ele acha que é vírgula e quebra tudo!
             CSVReader csvReader = new CSVReaderBuilder(reader)
                     .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                     .build()
        ) {
            conn.setAutoCommit(false); // Performance: Desliga salvamento automático
            String[] dados; //O OpenCSV vai preencher isso aqui pra gente

            //Pula o cabeçalho
            csvReader.readNext();

            int contador = 0;


            while ((dados = csvReader.readNext()) != null) {

                if (contador >= LIMITE_LINHAS) {
                    System.out.println("!!! Limite de " + LIMITE_LINHAS + " atingido.");
                    break;
                }

                int totalColunas = dados.length;

                //Validação básica para não quebrar
                if (totalColunas < 15) continue;

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
                String destinoBruto = TratamentoDados.limparTexto(dados[16]);

                String strData = TratamentoDados.limparTexto(dados[14]);
                LocalDate dataInicio = TratamentoDados.converterData(strData);

                //LOCALIZANDO, CONVERTENDO E SOMANDO OS VALORES

                String strValorDiarias = TratamentoDados.limparTexto(dados[totalColunas - 4]);
                String strValorPassagens = TratamentoDados.limparTexto(dados[totalColunas - 3]);
                String strValorDevolucao = TratamentoDados.limparTexto(dados[totalColunas - 2]);
                String strValorOutrosGastos = TratamentoDados.limparTexto(dados[totalColunas - 1]);

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