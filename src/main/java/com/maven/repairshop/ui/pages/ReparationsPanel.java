package com.maven.repairshop.ui.pages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.maven.repairshop.ui.dialogs.ReparationDetailDialog;
import com.maven.repairshop.ui.dialogs.ReparationFormDialog;
import com.maven.repairshop.ui.session.SessionContext;

public class ReparationsPanel extends JPanel {

    private final SessionContext session;

    private JTable table;
    private DefaultTableModel model;

    private JTextField txtSearch;
    private JComboBox<String> cbStatut;

    public ReparationsPanel(SessionContext session) {
        this.session = session;
        initUi();
        // plus tard: refresh();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        // ===== Top bar (recherche + filtre + ajouter) =====
        JPanel top = new JPanel(new BorderLayout());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(18);
        cbStatut = new JComboBox<>(new String[] {
                "Tous", "ENREGISTREE", "EN_COURS", "EN_ATTENTE_PIECES", "TERMINEE", "LIVREE", "ANNULEE"
        });
        JButton btnSearch = new JButton("Rechercher");
        JButton btnRefresh = new JButton("Actualiser");

        left.add(new JLabel("Recherche:"));
        left.add(txtSearch);
        left.add(new JLabel("Statut:"));
        left.add(cbStatut);
        left.add(btnSearch);
        left.add(btnRefresh);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("+ Ajouter");
        right.add(btnAdd);

        top.add(left, BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // ===== Table =====
        model = new DefaultTableModel(
                new Object[] { "ID", "Code", "Date", "Client", "Statut", "Avance", "Reste" }, 0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Double-clic => détail
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openDetailSelected();
                }
            }
        });

        // ===== Actions rapides =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnEdit = new JButton("Modifier");
        JButton btnCancel = new JButton("Annuler réparation");
        JButton btnDetail = new JButton("Voir détail");
        bottom.add(btnEdit);
        bottom.add(btnCancel);
        bottom.add(btnDetail);

        add(bottom, BorderLayout.SOUTH);

        // ===== Events (UI-only pour l’instant) =====
        btnAdd.addActionListener(e -> openFormCreate());
        btnEdit.addActionListener(e -> openFormEditSelected());
        btnDetail.addActionListener(e -> openDetailSelected());

        btnCancel.addActionListener(e -> {
            Long id = getSelectedId();
            if (id == null) return;

            int ok = JOptionPane.showConfirmDialog(this,
                    "Confirmer l'annulation de la réparation sélectionnée ?",
                    "Annuler réparation",
                    JOptionPane.YES_NO_OPTION);

            if (ok == JOptionPane.YES_OPTION) {
                // plus tard: reparationService.annuler(id, session)
                JOptionPane.showMessageDialog(this, "Annulation (à brancher service)");
            }
        });

        btnSearch.addActionListener(e -> {
            // plus tard: refresh() avec query + statut
            JOptionPane.showMessageDialog(this, "Recherche (à brancher service)");
        });

        btnRefresh.addActionListener(e -> {
            // plus tard: refresh()
            JOptionPane.showMessageDialog(this, "Actualiser (à brancher service)");
        });

        // (Option) données fake pour voir l’UI tout de suite
        seedFakeRows();
    }

    private void openFormCreate() {
        ReparationFormDialog dlg = new ReparationFormDialog(SwingUtilities.getWindowAncestor(this), session);
        dlg.setVisible(true);
        // plus tard: si dlg.isSaved() => refresh()
    }

    private void openFormEditSelected() {
        Long id = getSelectedId();
        if (id == null) return;

        ReparationFormDialog dlg = new ReparationFormDialog(SwingUtilities.getWindowAncestor(this), session);
        dlg.setModeEdit(id); // UI-only
        dlg.setVisible(true);
        // plus tard: refresh()
    }

    private void openDetailSelected() {
        Long id = getSelectedId();
        if (id == null) return;

        ReparationDetailDialog dlg = new ReparationDetailDialog(SwingUtilities.getWindowAncestor(this), session, id);
        dlg.setVisible(true);
    }

    private Long getSelectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionne une réparation d'abord.");
            return null;
        }
        Object v = model.getValueAt(row, 0);
        if (v == null) return null;
        try {
            return Long.valueOf(v.toString());
        } catch (Exception ex) {
            return null;
        }
    }

    private void seedFakeRows() {
        model.setRowCount(0);
        model.addRow(new Object[] { 1, "R-8F2A1", "2026-01-12", "Sara B.", "EN_COURS", "100", "200" });
        model.addRow(new Object[] { 2, "R-91BC0", "2026-01-10", "Amine K.", "TERMINEE", "150", "0" });
        model.addRow(new Object[] { 3, "R-003X9", "2026-01-08", "Nadia L.", "EN_ATTENTE_PIECES", "0", "400" });
    }
}