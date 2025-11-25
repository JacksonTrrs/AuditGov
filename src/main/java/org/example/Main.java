package org.example;

import org.example.ui.TelaPrincipal;

import javax.swing.*;

/**
 * Classe principal da aplicação AuditGov.
 * Inicia a interface gráfica do sistema.
 */
public class Main {

    public static void main(String[] args) {
        // Inicia a interface gráfica
        SwingUtilities.invokeLater(() -> {
            try {
                new TelaPrincipal().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Erro ao iniciar aplicação: " + e.getMessage(), 
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}