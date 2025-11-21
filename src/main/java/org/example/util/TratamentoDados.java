package org.example.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

//Métodos estáticos de limpeza

public class TratamentoDados {

    public static String limparTexto(String texto) {
        if (texto == null) return "";

        //1. Remove aspas duplas que vêm do CSV
        String textoLimpo = texto.replace("\"", "");

        //2. Remove espaços do começo e fim e deixa maiúsculo (Padronização)
        return textoLimpo.trim().toUpperCase();
    }

    public static Double converterValor(String valorBruto) {
        // Se vier nulo ou vazio, retorna zero
        if (valorBruto == null || valorBruto.trim().isEmpty()) { //limpando os espaços e verificando se está vazio ou nulo
            return 0.0;
        }
        try {
            //1. Limpa aspas e espaços
            String valorLimpo = valorBruto.replace("\"", "").trim();

            //2. Remove o ponto de milhar (Ex.: 1.000 -> 1000)
            valorLimpo = valorLimpo.replace(".", "");

            //3. Troca a vírgula decimal por ponto (Ex.: 50,99 -> 50.99)
            valorLimpo = valorLimpo.replace(",", ".");

            //4. Tenta converter para número
            return Double.parseDouble(valorLimpo);

        } catch (NumberFormatException e) {
            // BLINDAGEM: Se o valor vier "sujo" (ex: erro de quebra de linha do CSV),
            // em vez de travar o programa com erro vermelho, retornamos 0.0.
            // Isso garante que a importação continue rodando.
            return 0.0;
        }
    }

//PRÓXIMA MELHORIA REMOVER ACENTOS
    public static String[] separarCidadeUF(String destinoBruto) {
        //Valores padrão caso venha vazio
        String cidade = "INDEFINIDO";
        String uf = "XX";

        if (destinoBruto != null && !destinoBruto.isEmpty()) {
            //Limpa aspas antes de processar
            String limpo = limparTexto(destinoBruto);

            if (limpo.contains("/")) {
                String[] partes = limpo.split("/");

                cidade = partes[0].trim();

                // Verifica se tem a parte da UF depois da barra
                if (partes.length > 1) {
                    uf = partes[1].trim();
                }
            } else {
                // Se não tiver barra, assume que tudo é o nome da cidade
                cidade = limpo;
            }
        }
        return new String[]{cidade,uf};
    }

    public static LocalDate converterData(String dataBruta) {
        if (dataBruta == null || dataBruta.trim().isEmpty()) {
            return LocalDate.now(); // Se não tiver data, usa a de hoje como "tapa-buraco"
        }

        try {
            String limpa = limparTexto(dataBruta); // Tira aspas e espaços

            // Define o formato brasileiro
            DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            return LocalDate.parse(limpa, formatador);
        } catch (DateTimeParseException e) {
            System.err.println("Erro ao converter data: " + dataBruta + ". Usando data atual.");
            return LocalDate.now();
        }

    }
}
