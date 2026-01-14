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
import com.maven.repairshop.ui.dialogs.ReparationDetailDialog; // <--- Import Important
import com.maven.repairshop.ui.dialogs.ReparationFormDialog;
import com.maven.repairshop.ui.session.SessionContext;

public class ReparationsPanel extends JPanel {

    private final SessionContext session;

    // --- DESIGN CONSTANTS ---
    private final Color MAIN_COLOR = new Color(44, 185, 152);
    private final Color BG_APP = new Color(240, 242, 245);
    private final Color CARD_SHADOW = new Color(200, 200, 200, 80);

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    public ReparationsPanel(SessionContext session) {
        this.session = session;
        initUi();
        fillMockData(); // Données de test
    }

    private void initUi() {
        setLayout(new BorderLayout(25, 25));
        setBackground(BG_APP);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // 1. HEADER
        JPanel headerPanel = new JPanel(new BorderLayout(0, 15));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Gestion des Réparations (Design)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(40, 40, 40));
        headerPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        toolbar.setOpaque(false);

        txtSearch = new JTextField(20);
        styleInput(txtSearch);

        JButton btnSearch = createButton("🔍 Rechercher", MAIN_COLOR);
        JButton btnRefresh = createButton("Actualiser", new Color(149, 165, 166));
        JButton btnAdd = createButton("➕ Nouvelle Réparation", new Color(230, 126, 34)); // Orange

        toolbar.add(new JLabel("Recherche:"));
        toolbar.add(txtSearch);
        toolbar.add(btnSearch);
        toolbar.add(btnRefresh);
        toolbar.add(new JLabel("     "));
        toolbar.add(btnAdd);

        headerPanel.add(toolbar, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // 2. TABLEAU
        String[] cols = { "TICKET", "CLIENT", "APPAREIL", "PANNE", "STATUT", "DATE" };
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        styleTable(table);

        // Custom Renderer pour le Statut (Colonne 4)
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());

        ShadowPanel tableContainer = new ShadowPanel();
        tableContainer.setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tableContainer.add(scroll, BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);

        // 3. ACTIONS
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomBar.setOpaque(false);
        
        JButton btnDetail = createButton("Voir Détail", new Color(52, 73, 94));
        JButton btnEtat = createButton("Changer Statut", new Color(41, 128, 185));

        bottomBar.add(btnDetail);
        bottomBar.add(btnEtat);
        add(bottomBar, BorderLayout.SOUTH);

        // --- EVENTS ---
        
        // Clic sur NOUVELLE RÉPARATION
        btnAdd.addActionListener(e -> {
            Window parent = SwingUtilities.getWindowAncestor(this);
            ReparationFormDialog dlg = new ReparationFormDialog(parent, session);
            dlg.setVisible(true);

            if (dlg.isSaved()) {
                UiDialogs.info(this, "Ticket créé (Simulation Design) !");
                model.insertRow(0, new Object[]{"NEW-001", "Nouveau Client", "iPhone Test", "Écran", "EN_ATTENTE", "A l'instant"});
            }
        });

        // Clic sur VOIR DÉTAIL (Connecté !)
        btnDetail.addActionListener(e -> {
            Window parent = SwingUtilities.getWindowAncestor(this);
            // On ouvre la fiche technique (ID 1L par défaut pour le test)
            ReparationDetailDialog dlg = new ReparationDetailDialog(parent, session, 1L);
            dlg.setVisible(true);
        });
        
        btnEtat.addActionListener(e -> UiDialogs.info(this, "Utilisez 'Voir Détail' pour modifier le statut."));
    }

    private void fillMockData() {
        model.setRowCount(0);
        model.addRow(new Object[]{ "REP-2401", "Ahmed Benali", "Samsung S21", "Écran cassé", "EN_COURS", "12/01/2026" });
        model.addRow(new Object[]{ "REP-2402", "Sarah Idrissi", "MacBook Pro", "Batterie HS", "TERMINE", "10/01/2026" });
        model.addRow(new Object[]{ "REP-2403", "Karim Tazi", "HP Pavilion", "Chauffe", "EN_ATTENTE", "13/01/2026" });
    }

    // --- HELPERS DESIGN ---

    class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            l.setHorizontalAlignment(JLabel.CENTER);
            String status = (value != null) ? value.toString() : "";
            
            // Couleurs des badges
            if (status.contains("TERMINE")) {
                l.setForeground(new Color(39, 174, 96));
                l.setText("✔ TERMINÉ");
            } else if (status.contains("COURS")) {
                l.setForeground(new Color(230, 126, 34));
                l.setText("⚡ EN COURS");
            } else {
                l.setForeground(Color.GRAY);
                l.setText("⏳ " + status);
            }
            if(isSelected) l.setForeground(Color.BLACK);
            return l;
        }
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
        for(int i=0; i<table.getColumnCount(); i++) {
            if(i != 4) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer); // Sauf statut
        }
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