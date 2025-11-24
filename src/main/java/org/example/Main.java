package org.example;

import org.example.service.ServicoImportacao;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException {
        ServicoImportacao.excetuarImportacao("D:\\Programação\\Período IV (2025.2) () - ADS IFPB\\Banco de Dados 2\\Projeto 2\\Base Dados\\2025_Viagem.csv");
    }


}