package com.maven.repairshop.ui.pages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.maven.repairshop.ui.dialogs.EmpruntDialog;
import com.maven.repairshop.ui.session.SessionContext;

public class EmpruntsPanel extends JPanel {

    private final SessionContext session;

    private JTable table;
    private DefaultTableModel model;

    private JTextField txtSearch;
    private JComboBox<String> cbType;
    private JComboBox<String> cbStatut;

    private JLabel lblTotalEmprunts;
    private JLabel lblTotalPrets;

    public EmpruntsPanel(SessionContext session) {
        this.session = session;
        initUi();
        // plus tard: refresh();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        // ===== TOP (filtres + ajouter) =====
        JPanel top = new JPanel(new BorderLayout());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(16);

        cbType = new JComboBox<>(new String[] { "Tous", "EMPRUNT", "PRET" });
        cbStatut = new JComboBox<>(new String[] { "Tous", "EN_COURS", "REMBOURSE", "PARTIEL" });

        JButton btnSearch = new JButton("Rechercher");
        JButton btnRefresh = new JButton("Actualiser");

        left.add(new JLabel("Personne:"));
        left.add(txtSearch);
        left.add(new JLabel("Type:"));
        left.add(cbType);
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

        // ===== TABLE =====
        model = new DefaultTableModel(
                new Object[] { "ID", "Type", "Personne", "Montant", "Date", "Statut", "Remarque" }, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== BOTTOM (totaux + actions) =====
        JPanel bottom = new JPanel(new BorderLayout());

        JPanel totals = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblTotalEmprunts = new JLabel("Total emprunts en cours: —");
        lblTotalPrets = new JLabel("Total prêts en cours: —");
        totals.add(lblTotalEmprunts);
        totals.add(new JLabel(" | "));
        totals.add(lblTotalPrets);

        bottom.add(totals, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnEdit = new JButton("Modifier");
        JButton btnMarkPaid = new JButton("Marquer remboursé");
        JButton btnDetail = new JButton("Détail");
        actions.add(btnEdit);
        actions.add(btnMarkPaid);
        actions.add(btnDetail);

        bottom.add(actions, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        // ===== EVENTS (UI only pour l’instant) =====
        btnAdd.addActionListener(e -> openCreate());
        btnEdit.addActionListener(e -> openEditSelected());
        btnDetail.addActionListener(e -> showDetailSelected());

        btnMarkPaid.addActionListener(e -> {
            Long id = getSelectedId();
            if (id == null) return;

            int ok = JOptionPane.showConfirmDialog(this,
                    "Marquer cet emprunt/prêt comme REMBOURSÉ ?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION);

            if (ok == JOptionPane.YES_OPTION) {
                // plus tard: empruntService.marquerRembourse(id, session)
                JOptionPane.showMessageDialog(this, "Marqué remboursé (à brancher service)");
            }
        });

        btnSearch.addActionListener(e -> {
            // plus tard: refresh avec filtres
            JOptionPane.showMessageDialog(this, "Recherche (à brancher service)");
        });

        btnRefresh.addActionListener(e -> {
            // plus tard: refresh()
            JOptionPane.showMessageDialog(this, "Actualiser (à brancher service)");
        });

        // Données fake pour voir l’UI tout de suite
        seedFakeRows();
    }

    private void openCreate() {
        EmpruntDialog dlg = new EmpruntDialog(SwingUtilities.getWindowAncestor(this), session);
        dlg.setVisible(true);
        // plus tard: if (dlg.isSaved()) refresh();
    }

    private void openEditSelected() {
        Long id = getSelectedId();
        if (id == null) return;

        EmpruntDialog dlg = new EmpruntDialog(SwingUtilities.getWindowAncestor(this), session);
        dlg.setModeEdit(id); // UI-only
        dlg.setVisible(true);
        // plus tard: refresh();
    }

    private void showDetailSelected() {
        Long id = getSelectedId();
        if (id == null) return;

        JOptionPane.showMessageDialog(this,
                "Détail emprunt/prêt #" + id + "\n(À remplacer par un dialog détail si tu veux)",
                "Détail",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private Long getSelectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionne une ligne d'abord.");
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
        model.addRow(new Object[] { 1, "EMPRUNT", "Youssef", "500", "2026-01-05", "EN_COURS", "A rendre fin du mois" });
        model.addRow(new Object[] { 2, "PRET", "Hamza", "300", "2026-01-07", "PARTIEL", "Déjà reçu 100" });
        model.addRow(new Object[] { 3, "EMPRUNT", "Khadija", "200", "2026-01-02", "REMBOURSE", "" });

        // Totaux fake
        lblTotalEmprunts.setText("Total emprunts en cours: 500 DH");
        lblTotalPrets.setText("Total prêts en cours: 200 DH");
    }
}