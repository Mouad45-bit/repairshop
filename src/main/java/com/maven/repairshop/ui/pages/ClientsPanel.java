package com.maven.repairshop.ui.pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.dialogs.ClientDialog;
import com.maven.repairshop.ui.session.SessionContext;

public class ClientsPanel extends JPanel {

    private final SessionContext session;

    // --- DESIGN CONSTANTS ---
    private final Color MAIN_COLOR = new Color(44, 185, 152);
    private final Color BG_APP = new Color(240, 242, 245);
    private final Color CARD_SHADOW = new Color(200, 200, 200, 80);

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    public ClientsPanel(SessionContext session) {
        this.session = session;
        initUi();
        fillMockData(); 
    }

    private void initUi() {
        setLayout(new BorderLayout(25, 25));
        setBackground(BG_APP);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // 1. HEADER
        JPanel headerPanel = new JPanel(new BorderLayout(0, 15));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Base Clients (Design Test)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(40, 40, 40));
        headerPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        toolbar.setOpaque(false);

        txtSearch = new JTextField(20);
        styleInput(txtSearch);

        JButton btnSearch = createButton("🔍 Rechercher", MAIN_COLOR);
        JButton btnRefresh = createButton("Actualiser", new Color(149, 165, 166));
        
        // --- BOUTON AJOUTER (Remis à sa place !) ---
        JButton btnAdd = createButton("➕ Nouveau Client", new Color(41, 128, 185)); // Bleu

        toolbar.add(new JLabel("Recherche:"));
        toolbar.add(txtSearch);
        toolbar.add(btnSearch);
        toolbar.add(btnRefresh);
        toolbar.add(new JLabel("     ")); // Espace
        toolbar.add(btnAdd);            // Le bouton est bien là !

        headerPanel.add(toolbar, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // 2. TABLEAU
        model = new DefaultTableModel(new Object[] { "ID", "NOM COMPLET", "TÉLÉPHONE", "EMAIL", "ADRESSE", "VILLE" }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        styleTable(table);

        // Cacher ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        ShadowPanel tableContainer = new ShadowPanel();
        tableContainer.setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tableContainer.add(scroll, BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);

        // 3. ACTIONS BAS DE PAGE
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomBar.setOpaque(false);

        JButton btnDetail = createButton("Voir Fiche", new Color(52, 73, 94));
        JButton btnEdit = createButton("Modifier", new Color(243, 156, 18));
        JButton btnDelete = createButton("Supprimer", new Color(231, 76, 60));

        bottomBar.add(btnDetail);
        bottomBar.add(btnEdit);
        bottomBar.add(btnDelete);
        add(bottomBar, BorderLayout.SOUTH);
        
        // --- EVENTS ---

        btnAdd.addActionListener(e -> {
            Window parent = SwingUtilities.getWindowAncestor(this);
            ClientDialog dlg = new ClientDialog(parent);
            dlg.setModeCreate();
            dlg.setVisible(true);
            
            if (dlg.isSaved()) {
                 ClientDialog.ClientFormData data = dlg.getFormData();
                 model.addRow(new Object[]{ 99L, data.nom, data.telephone, data.email, data.adresse, data.ville });
                 UiDialogs.info(this, "Client ajouté avec succès !");
            }
        });

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row < 0) { UiDialogs.warn(this, "Sélectionnez un client."); return; }
            
            String nom = model.getValueAt(row, 1).toString();
            String tel = model.getValueAt(row, 2).toString();
            
            Window parent = SwingUtilities.getWindowAncestor(this);
            ClientDialog dlg = new ClientDialog(parent);
            dlg.setModeEdit(1L, nom, tel, "email@test.com", "Adresse", "Ville");
            dlg.setVisible(true);
        });

        btnDetail.addActionListener(e -> UiDialogs.info(this, "Détail : Fonctionne avec le nouveau UiDialogs !"));
        btnDelete.addActionListener(e -> UiDialogs.error(this, "Suppression impossible (Test Design)."));
    }

    private void fillMockData() {
        model.setRowCount(0);
        model.addRow(new Object[]{ 1L, "Ahmed Benali", "0661123456", "ahmed@gmail.com", "12 Rue des Fleurs", "Casablanca" });
        model.addRow(new Object[]{ 2L, "Sarah Idrissi", "0662987654", "sarah.id@outlook.fr", "Apt 4, Imm 2", "Rabat" });
        model.addRow(new Object[]{ 3L, "Karim Tazi", "0655443322", "k.tazi@company.ma", "Zone Ind. 3", "Tanger" });
    }

    private void styleTable(JTable table) {
        table.setRowHeight(45);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(240, 240, 240));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(235, 248, 245));
        table.setSelectionForeground(Color.BLACK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBackground(Color.WHITE);
                l.setForeground(new Color(150, 150, 150));
                l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(240, 240, 240)));
                l.setHorizontalAlignment(JLabel.CENTER);
                return l;
            }
        });
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i=1; i<table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private void styleInput(JTextField txt) {
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)), 
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
    }

    class ShadowPanel extends JPanel {
        public ShadowPanel() { setOpaque(false); setBorder(new EmptyBorder(5, 5, 10, 5)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(5, 5, getWidth()-10, getHeight()-15, 15, 15);
            g2.setColor(CARD_SHADOW);
            g2.drawRoundRect(5, 5, getWidth()-10, getHeight()-15, 15, 15);
            super.paintChildren(g);
        }
    }
}