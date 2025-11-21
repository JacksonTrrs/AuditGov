package org.example;

import org.example.service.Teste;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        Teste teste = new Teste();

        teste.excetuarImportacao("");
    }
}
