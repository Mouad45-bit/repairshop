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

import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.dialogs.EmpruntDialog; // Import Correct
import com.maven.repairshop.ui.session.SessionContext;

public class EmpruntsPanel extends JPanel {

    private final SessionContext session;

    // --- DESIGN CONSTANTS ---
    private final Color MAIN_COLOR = new Color(44, 185, 152);
    private final Color BG_APP = new Color(240, 242, 245);
    private final Color CARD_SHADOW = new Color(200, 200, 200, 80);

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    public EmpruntsPanel(SessionContext session) {
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

        JLabel lblTitle = new JLabel("Gestion des Prêts & Emprunts (Design)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(40, 40, 40));
        headerPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        toolbar.setOpaque(false);

        txtSearch = new JTextField(20);
        styleInput(txtSearch);

        JButton btnSearch = createButton("🔍 Rechercher", MAIN_COLOR);
        JButton btnAdd = createButton("➕ Nouveau Prêt/Avance", new Color(155, 89, 182)); // Violet

        toolbar.add(new JLabel("Recherche:"));
        toolbar.add(txtSearch);
        toolbar.add(btnSearch);
        toolbar.add(new JLabel("     "));
        toolbar.add(btnAdd);

        headerPanel.add(toolbar, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // 2. TABLEAU
        String[] cols = { "DATE", "TYPE", "PERSONNE", "MONTANT", "STATUT" };
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        styleTable(table);

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
        JButton btnRembourser = createButton("Marquer Remboursé", new Color(39, 174, 96));
        bottomBar.add(btnRembourser);
        add(bottomBar, BorderLayout.SOUTH);

        // --- EVENTS ---
        btnAdd.addActionListener(e -> {
            Window parent = SwingUtilities.getWindowAncestor(this);
            // On ouvre le Dialog
            EmpruntDialog dlg = new EmpruntDialog(parent, session);
            dlg.setVisible(true);
            
            if(dlg.isSaved()) {
                 UiDialogs.info(this, "Enregistré (Simulation) !");
                 model.insertRow(0, new Object[]{"Auj.", dlg.getFormData().type, dlg.getFormData().personne, dlg.getFormData().montantStr + " DH", "NON_REGLE"});
            }
        });

        btnRembourser.addActionListener(e -> UiDialogs.info(this, "Action Remboursement (Simulation)"));
    }

    private void fillMockData() {
        model.setRowCount(0);
        model.addRow(new Object[]{ "14/01/2026", "PRET_EMPOYE", "Technicien Ali", "500.00 DH", "NON_REGLE" });
        model.addRow(new Object[]{ "10/01/2026", "EMPRUNT_EXT", "Fournisseur Info", "2000.00 DH", "REGLE" });
    }

    class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            l.setHorizontalAlignment(JLabel.CENTER);
            String status = (value != null) ? value.toString() : "";
            
            if (status.contains("NON")) {
                l.setForeground(new Color(192, 57, 43));
                l.setText("✖ NON RÉGLÉ");
            } else {
                l.setForeground(new Color(39, 174, 96));
                l.setText("✔ RÉGLÉ");
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
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i<table.getColumnCount(); i++) {
            if(i != 4) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
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