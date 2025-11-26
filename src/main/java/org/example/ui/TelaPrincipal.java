package org.example.ui;

import org.example.dto.DestinoFrequente;
import org.example.dto.OrgaoGastador;
import org.example.model.Viagem;
import org.example.ui.controller.ControllerPrincipal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Interface gráfica principal da aplicação AuditGov.
 * Interface simples e intuitiva para gerenciar importações e visualizar dados.
 */
public class TelaPrincipal extends JFrame {

    private final ControllerPrincipal controlador;
    private JTable tabelaViagens;
    private DefaultTableModel modeloTabela;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel estatisticasLabel;
    private JButton btnImportar;
    private JButton btnAtualizar;
    private JButton btnEstatisticas;
    private JButton btnRelatorioGastadores;
    private JButton btnRelatorioDestinos;
    private final JFileChooser fileChooser;
    private JTable tabelaGastadores;
    private DefaultTableModel modeloTabelaGastadores;
    private JTable tabelaDestinos;
    private DefaultTableModel modeloTabelaDestinos;
    
    private static final int LIMITE_REGISTROS = 100;
    private int paginaAtual = 0;

    public TelaPrincipal() {
        this.controlador = new ControllerPrincipal();
        this.fileChooser = new JFileChooser();
        
        configurarJanela();
        criarComponentes();
        organizarLayout();
        configurarEventos();
        
        // Carrega dados iniciais
        carregarViagens();
        atualizarEstatisticas();
        carregarRelatorioGastadores();
        carregarRelatorioDestinos();
    }

    private void configurarJanela() {
        setTitle("AuditGov - Sistema de Auditoria Governamental");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
    }

    private void criarComponentes() {
        // Painel superior - Importação
        JPanel painelImportacao = criarPainelImportacao();
        
        // Tabela de viagens
        String[] colunas = {"Processo", "Data Início", "Órgão", "Destino", "Valor Total"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaViagens = new JTable(modeloTabela);
        tabelaViagens.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaViagens.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabelaViagens.getTableHeader().setReorderingAllowed(false);
        
        // Ajusta largura das colunas
        tabelaViagens.getColumnModel().getColumn(0).setPreferredWidth(150);
        tabelaViagens.getColumnModel().getColumn(1).setPreferredWidth(120);
        tabelaViagens.getColumnModel().getColumn(2).setPreferredWidth(300);
        tabelaViagens.getColumnModel().getColumn(3).setPreferredWidth(200);
        tabelaViagens.getColumnModel().getColumn(4).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(tabelaViagens);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Viagens Cadastradas"));
        
        // Painel de relatórios com abas
        JTabbedPane painelRelatorios = criarPainelRelatorios();
        
        // Painel dividido para ter tabela de viagens e relatórios lado a lado
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, painelRelatorios);
        splitPane.setDividerLocation(700);
        splitPane.setResizeWeight(0.6);
        splitPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Painel de navegação
        JPanel painelNavegacao = criarPainelNavegacao();
        
        // Adiciona componentes
        add(painelImportacao, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(painelNavegacao, BorderLayout.SOUTH);
    }

    private JPanel criarPainelImportacao() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        btnImportar = new JButton("Importar CSV");
        btnImportar.setPreferredSize(new Dimension(150, 35));
        btnImportar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        btnAtualizar = new JButton("Atualizar Lista");
        btnAtualizar.setPreferredSize(new Dimension(150, 35));
        
        btnEstatisticas = new JButton("Atualizar Estatísticas");
        btnEstatisticas.setPreferredSize(new Dimension(180, 35));
        
        btnRelatorioGastadores = new JButton("Atualizar Gastadores");
        btnRelatorioGastadores.setPreferredSize(new Dimension(170, 35));
        
        btnRelatorioDestinos = new JButton("Atualizar Destinos");
        btnRelatorioDestinos.setPreferredSize(new Dimension(160, 35));
        
        painelBotoes.add(btnImportar);
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnEstatisticas);
        painelBotoes.add(btnRelatorioGastadores);
        painelBotoes.add(btnRelatorioDestinos);
        
        // Barra de progresso
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Pronto");
        
        // Label de status
        statusLabel = new JLabel("Sistema pronto");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        
        // Label de estatísticas
        estatisticasLabel = new JLabel("Carregando estatísticas...");
        estatisticasLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        estatisticasLabel.setForeground(new Color(0, 100, 0));
        
        JPanel painelInfo = new JPanel(new BorderLayout(5, 5));
        painelInfo.add(statusLabel, BorderLayout.WEST);
        painelInfo.add(estatisticasLabel, BorderLayout.EAST);
        painelInfo.add(progressBar, BorderLayout.SOUTH);
        
        painel.add(painelBotoes, BorderLayout.NORTH);
        painel.add(painelInfo, BorderLayout.CENTER);
        
        return painel;
    }

    private JPanel criarPainelNavegacao() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton btnAnterior = new JButton("◀ Anterior");
        JButton btnProximo = new JButton("Próximo ▶");
        JLabel labelPagina = new JLabel("Página: 1");
        labelPagina.setName("labelPagina");
        
        btnAnterior.addActionListener(e -> {
            if (paginaAtual > 0) {
                paginaAtual--;
                carregarViagens();
            }
        });
        
        btnProximo.addActionListener(e -> {
            paginaAtual++;
            carregarViagens();
        });
        
        painel.add(btnAnterior);
        painel.add(labelPagina);
        painel.add(btnProximo);
        
        return painel;
    }

    private void organizarLayout() {
        // Aplica estilo moderno
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            // Usa look and feel padrão se houver erro
        }
    }

    private void configurarEventos() {
        btnImportar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importarArquivo();
            }
        });
        
        btnAtualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                paginaAtual = 0;
                carregarViagens();
                atualizarEstatisticas();
            }
        });
        
        btnEstatisticas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                atualizarEstatisticas();
            }
        });
        
        btnRelatorioGastadores.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                carregarRelatorioGastadores();
            }
        });
        
        btnRelatorioDestinos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                carregarRelatorioDestinos();
            }
        });
    }

    private void importarArquivo() {
        fileChooser.setDialogTitle("Selecione o arquivo CSV para importar");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }

            @Override
            public String getDescription() {
                return "Arquivos CSV (*.csv)";
            }
        });
        
        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            String caminho = arquivo.getAbsolutePath();
            
            btnImportar.setEnabled(false);
            statusLabel.setText("Importando arquivo: " + arquivo.getName());
            
            controlador.importarArquivo(caminho, progressBar, statusLabel)
                .thenAccept(mensagem -> {
                    SwingUtilities.invokeLater(() -> {
                        btnImportar.setEnabled(true);
                        statusLabel.setText(mensagem);
                        progressBar.setValue(0);
                        progressBar.setString("Concluído");
                        
                        if (mensagem.contains("sucesso")) {
                            JOptionPane.showMessageDialog(this, 
                                mensagem, 
                                "Importação Concluída", 
                                JOptionPane.INFORMATION_MESSAGE);
                            carregarViagens();
                            atualizarEstatisticas();
                        } else {
                            JOptionPane.showMessageDialog(this, 
                                mensagem, 
                                "Erro na Importação", 
                                JOptionPane.ERROR_MESSAGE);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    SwingUtilities.invokeLater(() -> {
                        btnImportar.setEnabled(true);
                        statusLabel.setText("Erro: " + throwable.getMessage());
                        progressBar.setValue(0);
                        JOptionPane.showMessageDialog(this, 
                            "Erro: " + throwable.getMessage(), 
                            "Erro", 
                            JOptionPane.ERROR_MESSAGE);
                    });
                    return null;
                });
        }
    }

    private void carregarViagens() {
        statusLabel.setText("Carregando viagens...");
        modeloTabela.setRowCount(0);
        
        controlador.buscarViagens(LIMITE_REGISTROS, paginaAtual * LIMITE_REGISTROS)
            .thenAccept(viagens -> {
                SwingUtilities.invokeLater(() -> {
                    preencherTabela(viagens);
                    atualizarLabelPagina();
                    statusLabel.setText("Viagens carregadas: " + viagens.size());
                });
            })
            .exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Erro ao carregar: " + throwable.getMessage());
                    JOptionPane.showMessageDialog(this, 
                        "Erro ao carregar viagens: " + throwable.getMessage(), 
                        "Erro", 
                        JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
    }

    private void preencherTabela(List<Viagem> viagens) {
        NumberFormat formatador = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
        DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        for (Viagem viagem : viagens) {
            Object[] linha = {
                viagem.getIdProcesso(),
                viagem.getDataInicio().format(formatadorData),
                viagem.getOrgao().getNome(),
                viagem.getDestino().toString(),
                formatador.format(viagem.getValorTotal())
            };
            modeloTabela.addRow(linha);
        }
    }

    private void atualizarEstatisticas() {
        estatisticasLabel.setText("Carregando...");
        
        controlador.buscarEstatisticas()
            .thenAccept(stats -> {
                SwingUtilities.invokeLater(() -> {
                    NumberFormat formatador = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
                    NumberFormat formatadorNumero = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
                    
                    long totalViagens = ((Number) stats.get("totalViagens")).longValue();
                    double valorTotal = ((Number) stats.get("valorTotal")).doubleValue();
                    double valorMedio = ((Number) stats.get("valorMedio")).doubleValue();
                    
                    String texto = String.format(
                        "Total: %s viagens | Valor Total: %s | Média: %s",
                        formatadorNumero.format(totalViagens),
                        formatador.format(valorTotal),
                        formatador.format(valorMedio)
                    );
                    
                    estatisticasLabel.setText(texto);
                });
            })
            .exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    estatisticasLabel.setText("Erro ao carregar estatísticas");
                });
                return null;
            });
    }

    private JTabbedPane criarPainelRelatorios() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(450, 0));
        
        // Aba 1: Maiores Gastadores
        JPanel painelGastadores = criarPainelRelatorioGastadores();
        tabbedPane.addTab("Maiores Gastadores", painelGastadores);
        
        // Aba 2: Destinos Frequentes
        JPanel painelDestinos = criarPainelRelatorioDestinos();
        tabbedPane.addTab("Destinos Frequentes", painelDestinos);
        
        return tabbedPane;
    }

    private JPanel criarPainelRelatorioGastadores() {
        JPanel painel = new JPanel(new BorderLayout(5, 5));
        painel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Tabela de maiores gastadores
        String[] colunasGastadores = {"#", "Órgão", "Valor Total"};
        modeloTabelaGastadores = new DefaultTableModel(colunasGastadores, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Integer.class;
                if (column == 2) return Double.class;
                return String.class;
            }
        };
        
        tabelaGastadores = new JTable(modeloTabelaGastadores);
        tabelaGastadores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaGastadores.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabelaGastadores.getTableHeader().setReorderingAllowed(false);
        
        // Ajusta largura das colunas
        tabelaGastadores.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabelaGastadores.getColumnModel().getColumn(1).setPreferredWidth(300);
        tabelaGastadores.getColumnModel().getColumn(2).setPreferredWidth(150);
        
        // Formatação da coluna de valor
        tabelaGastadores.getColumnModel().getColumn(2).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            NumberFormat formatador = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
            
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Number) {
                    setText(formatador.format(((Number) value).doubleValue()));
                    setHorizontalAlignment(JLabel.RIGHT);
                }
                return c;
            }
        });
        
        JScrollPane scrollPaneGastadores = new JScrollPane(tabelaGastadores);
        painel.add(scrollPaneGastadores, BorderLayout.CENTER);
        
        return painel;
    }

    private JPanel criarPainelRelatorioDestinos() {
        JPanel painel = new JPanel(new BorderLayout(5, 5));
        painel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Tabela de destinos frequentes
        String[] colunasDestinos = {"#", "Cidade", "UF", "Quantidade"};
        modeloTabelaDestinos = new DefaultTableModel(colunasDestinos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0 || column == 3) return Integer.class;
                return String.class;
            }
        };
        
        tabelaDestinos = new JTable(modeloTabelaDestinos);
        tabelaDestinos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaDestinos.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabelaDestinos.getTableHeader().setReorderingAllowed(false);
        
        // Ajusta largura das colunas
        tabelaDestinos.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabelaDestinos.getColumnModel().getColumn(1).setPreferredWidth(250);
        tabelaDestinos.getColumnModel().getColumn(2).setPreferredWidth(50);
        tabelaDestinos.getColumnModel().getColumn(3).setPreferredWidth(100);
        
        // Formatação da coluna de quantidade
        tabelaDestinos.getColumnModel().getColumn(3).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            NumberFormat formatador = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
            
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Number) {
                    setText(formatador.format(((Number) value).intValue()));
                    setHorizontalAlignment(JLabel.RIGHT);
                }
                return c;
            }
        });
        
        JScrollPane scrollPaneDestinos = new JScrollPane(tabelaDestinos);
        painel.add(scrollPaneDestinos, BorderLayout.CENTER);
        
        return painel;
    }

    private void carregarRelatorioGastadores() {
        statusLabel.setText("Carregando relatório de maiores gastadores...");
        modeloTabelaGastadores.setRowCount(0);
        
        controlador.buscarTop5MaioresGastadores()
            .thenAccept(gastadores -> {
                SwingUtilities.invokeLater(() -> {
                    int posicao = 1;
                    for (OrgaoGastador gastador : gastadores) {
                        Object[] linha = {
                            posicao++,
                            gastador.getNomeOrgao(),
                            gastador.getValorTotal()
                        };
                        modeloTabelaGastadores.addRow(linha);
                    }
                    
                    if (gastadores.isEmpty()) {
                        statusLabel.setText("Nenhum dado encontrado para o relatório");
                    } else {
                        statusLabel.setText("Relatório carregado: " + gastadores.size() + " órgãos");
                    }
                });
            })
            .exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Erro ao carregar relatório: " + throwable.getMessage());
                    JOptionPane.showMessageDialog(this, 
                        "Erro ao carregar relatório de maiores gastadores: " + throwable.getMessage(), 
                        "Erro", 
                        JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
    }

    private void carregarRelatorioDestinos() {
        statusLabel.setText("Carregando relatório de destinos frequentes...");
        modeloTabelaDestinos.setRowCount(0);
        
        controlador.buscarTop10DestinosFrequentes()
            .thenAccept(destinos -> {
                SwingUtilities.invokeLater(() -> {
                    int posicao = 1;
                    for (DestinoFrequente destino : destinos) {
                        Object[] linha = {
                            posicao++,
                            destino.getNomeCidade(),
                            destino.getUf(),
                            destino.getQuantidadeViagens()
                        };
                        modeloTabelaDestinos.addRow(linha);
                    }
                    
                    if (destinos.isEmpty()) {
                        statusLabel.setText("Nenhum dado encontrado para o relatório de destinos");
                    } else {
                        statusLabel.setText("Relatório de destinos carregado: " + destinos.size() + " cidades");
                    }
                });
            })
            .exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Erro ao carregar relatório de destinos: " + throwable.getMessage());
                    JOptionPane.showMessageDialog(this, 
                        "Erro ao carregar relatório de destinos frequentes: " + throwable.getMessage(), 
                        "Erro", 
                        JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
    }

    private void atualizarLabelPagina() {
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JPanel) {
                for (Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JLabel && "labelPagina".equals(subComp.getName())) {
                        ((JLabel) subComp).setText("Página: " + (paginaAtual + 1));
                        break;
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
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

