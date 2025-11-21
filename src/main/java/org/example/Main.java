package org.example;

import org.example.service.Teste;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        Teste teste = new Teste();
        Teste.testeTratamentoDados();
        //teste.excetuarImportacao("D:\\Programação\\Período IV (2025.2) () - ADS IFPB\\Banco de Dados 2\\Projeto 2\\Base Dados\\2025_Viagem.csv");
    }
}
