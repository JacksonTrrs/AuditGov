package org.example.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Abre/Fecha conexão (Singleton/Factory)
public class ConexaoFactory {

    // --- CONFIGURAÇÃO DO BANCO ---
    private static final String URL = "jdbc:mariadb://localhost:3306/audit_gov"; //configure a porta
    private static final String USUARIO = "root"; //configure o usuario
    private static final String SENHA = ""; //configure a senha

    public static Connection getConexao() throws SQLException {
        //O DriverManager é quem pega o jar do MariaDB e abre o túnel
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}
