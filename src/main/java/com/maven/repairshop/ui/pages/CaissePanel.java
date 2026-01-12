package com.maven.repairshop.ui.pages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.maven.repairshop.ui.session.SessionContext;

public class CaissePanel extends JPanel {

    private final SessionContext session;

    private JTextField txtDateDebut;
    private JTextField txtDateFin;

    private JTable table;
    private DefaultTableModel model;

    private JLabel lblEntrees;
    private JLabel lblSorties;
    private JLabel lblSolde;

    public CaissePanel(SessionContext session) {
        this.session = session;
        initUi();
        // plus tard: refresh();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        // ===== TOP : filtres =====
        JPanel top = new JPanel(new BorderLayout());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtDateDebut = new JTextField(10); // YYYY-MM-DD
        txtDateFin = new JTextField(10);

        JButton btnApply = new JButton("Appliquer");
        JButton btnReset = new JButton("Réinitialiser");

        left.add(new JLabel("Date début:"));
        left.add(txtDateDebut);
        left.add(new JLabel("Date fin:"));
        left.add(txtDateFin);
        left.add(btnApply);
        left.add(btnReset);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExportPdf = new JButton("Exporter PDF");
        JButton btnExportCsv = new JButton("Exporter CSV");
        right.add(btnExportPdf);
        right.add(btnExportCsv);

        top.add(left, BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // ===== TABLE : mouvements =====
        model = new DefaultTableModel(
                new Object[] { "Date", "Type", "Catégorie", "Description", "Montant" }, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== BOTTOM : totaux =====
        JPanel bottom = new JPanel(new BorderLayout());

        JPanel totals = new JPanel(new GridLayout(1, 3, 12, 12));
        lblEntrees = new JLabel("Total entrées: —");
        lblSorties = new JLabel("Total sorties: —");
        lblSolde = new JLabel("Solde: —");

        totals.add(lblEntrees);
        totals.add(lblSorties);
        totals.add(lblSolde);

        bottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottom.add(totals, BorderLayout.CENTER);

        add(bottom, BorderLayout.SOUTH);

        // ===== EVENTS (UI only pour l’instant) =====
        btnApply.addActionListener(e -> {
            // plus tard: caisseService.getMouvements(from,to,session)
            JOptionPane.showMessageDialog(this, "Filtre période (à brancher service)");
        });

        btnReset.addActionListener(e -> {
            txtDateDebut.setText("");
            txtDateFin.setText("");
            // plus tard: refresh()
        });

        btnExportPdf.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Export PDF (à implémenter plus tard)")
        );
        btnExportCsv.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Export CSV (à implémenter plus tard)")
        );

        // Données fake pour voir l’UI directement
        seedFakeRows();
    }

    private void seedFakeRows() {
        model.setRowCount(0);

        // Type: ENTREE/SORTIE, Catégorie: REPARATION/EMPRUNT/PRET/AUTRE
        model.addRow(new Object[] { "2026-01-12", "ENTREE", "REPARATION", "Avance réparation R-8F2A1", "+100" });
        model.addRow(new Object[] { "2026-01-10", "ENTREE", "REPARATION", "Solde réparation R-91BC0", "+150" });
        model.addRow(new Object[] { "2026-01-09", "SORTIE", "AUTRE", "Achat pièces", "-60" });

        // Totaux fake
        lblEntrees.setText("Total entrées: 250 DH");
        lblSorties.setText("Total sorties: 60 DH");
        lblSolde.setText("Solde: 190 DH");
    }
}