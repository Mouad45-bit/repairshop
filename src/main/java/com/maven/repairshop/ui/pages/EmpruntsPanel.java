package com.maven.repairshop.ui.pages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.maven.repairshop.model.Emprunt;
import com.maven.repairshop.model.enums.StatutEmprunt;
import com.maven.repairshop.model.enums.TypeEmprunt;
import com.maven.repairshop.ui.controllers.EmpruntController;
import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.dialogs.EmpruntDialog;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.UiServices;

public class EmpruntsPanel extends JPanel {

    private final SessionContext session;

    private final EmpruntController ctrl = new EmpruntController(UiServices.get().emprunts());

    private JTable table;
    private DefaultTableModel model;

    private JTextField txtSearch;
    private JComboBox<Object> cbType;
    private JComboBox<Object> cbStatut;

    private JLabel lblTotalEmprunts;
    private JLabel lblTotalPrets;

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EmpruntsPanel(SessionContext session) {
        this.session = session;
        initUi();
        refresh();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        // ===== TOP (filtres + ajouter) =====
        JPanel top = new JPanel(new BorderLayout());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(16);

        cbType = new JComboBox<>();
        cbType.addItem("Tous");
        for (TypeEmprunt t : TypeEmprunt.values()) cbType.addItem(t);

        cbStatut = new JComboBox<>();
        cbStatut.addItem("Tous");
        for (StatutEmprunt s : StatutEmprunt.values()) cbStatut.addItem(s);

        JButton btnSearch = new JButton("Rechercher");
        JButton btnRefresh = new JButton("Actualiser");

        left.add(new JLabel("Personne / Motif:"));
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
                new Object[] { "ID", "Type", "Personne", "Montant", "Date", "Statut", "Motif" }, 0
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
        JButton btnMarkPaid = new JButton("Marquer remboursé");
        JButton btnDelete = new JButton("Supprimer");
        JButton btnDetail = new JButton("Détail");
        actions.add(btnMarkPaid);
        actions.add(btnDelete);
        actions.add(btnDetail);

        bottom.add(actions, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        // ===== EVENTS =====
        btnAdd.addActionListener(e -> openCreate());
        btnDetail.addActionListener(e -> showDetailSelected());

        btnMarkPaid.addActionListener(e -> {
            Long id = getSelectedId();
            if (id == null) return;

            int ok = JOptionPane.showConfirmDialog(this,
                    "Marquer comme REMBOURSÉ ?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION);

            if (ok == JOptionPane.YES_OPTION) {
                ctrl.changerStatut(this, id, StatutEmprunt.REMBOURSE.name(), () -> {
                    UiDialogs.info(this, "Statut mis à jour.");
                    refresh();
                });
            }
        });

        btnDelete.addActionListener(e -> {
            Long id = getSelectedId();
            if (id == null) return;

            int ok = JOptionPane.showConfirmDialog(this,
                    "Supprimer cet emprunt/prêt ?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;

            ctrl.supprimer(this, id, () -> {
                UiDialogs.info(this, "Supprimé.");
                refresh();
            });
        });

        btnSearch.addActionListener(e -> refresh());
        txtSearch.addActionListener(e -> refresh());
        cbType.addActionListener(e -> refresh());
        cbStatut.addActionListener(e -> refresh());

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cbType.setSelectedIndex(0);
            cbStatut.setSelectedIndex(0);
            refresh();
        });
    }

    private void openCreate() {
        Long reparateurId = session.getReparateurId();
        if (reparateurId == null) {
            UiDialogs.warn(this, "Cette page est réservée au réparateur.");
            return;
        }

        EmpruntDialog dlg = new EmpruntDialog(SwingUtilities.getWindowAncestor(this));
        dlg.setModeCreate();
        dlg.setVisible(true);

        if (!dlg.isSaved()) return;

        EmpruntDialog.EmpruntFormData data = dlg.getFormData();
        if (data == null) return;

        TypeEmprunt type = "PRET".equalsIgnoreCase(data.type) ? TypeEmprunt.PRET : TypeEmprunt.EMPRUNT;

        // create() n'accepte pas date/statut => statut = EN_COURS par défaut côté service
        ctrl.creer(this, reparateurId, type, data.personne, data.montantStr, data.remarque, created -> {
            UiDialogs.info(this, "Ajout OK.");
            refresh();
        });
    }

    private void refresh() {
        Long reparateurId = session.getReparateurId();
        if (reparateurId == null) {
            UiDialogs.warn(this, "Cette page est réservée au réparateur (session invalide).");
            return;
        }

        ctrl.lister(this, reparateurId, list -> {
            List<Object[]> rows = toRowsFiltered(list);

            model.setRowCount(0);
            for (Object[] r : rows) model.addRow(r);

            updateTotalsFromRows(rows);
        });
    }

    private List<Object[]> toRowsFiltered(List<Emprunt> list) {
        String q = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        Object typeSel = cbType.getSelectedItem();
        Object statutSel = cbStatut.getSelectedItem();

        TypeEmprunt typeFilter = (typeSel instanceof TypeEmprunt) ? (TypeEmprunt) typeSel : null;
        StatutEmprunt statutFilter = (statutSel instanceof StatutEmprunt) ? (StatutEmprunt) statutSel : null;

        List<Object[]> out = new ArrayList<>();

        for (Emprunt e : list) {
            String personne = safe(e.getNomPersonne());
            String motif = safe(e.getMotif());

            if (!q.isEmpty()) {
                String hay = (personne + " " + motif).toLowerCase();
                if (!hay.contains(q)) continue;
            }

            if (typeFilter != null && e.getType() != typeFilter) continue;
            if (statutFilter != null && e.getStatut() != statutFilter) continue;

            String dt = e.getDateEmprunt() != null ? e.getDateEmprunt().format(DT) : "";

            out.add(new Object[] {
                    e.getId(),
                    e.getType(),
                    personne,
                    e.getMontant(),
                    dt,
                    e.getStatut(),
                    motif
            });
        }

        return out;
    }

    private void updateTotalsFromRows(List<Object[]> rows) {
        double totalEmprunts = 0;
        double totalPrets = 0;

        for (Object[] r : rows) {
            String type = r[1] != null ? r[1].toString() : "";
            String statut = r[5] != null ? r[5].toString() : "";

            // "en cours" = EN_COURS ou PARTIELLEMENT_REMBOURSE
            if (!("EN_COURS".equalsIgnoreCase(statut) || "PARTIELLEMENT_REMBOURSE".equalsIgnoreCase(statut))) {
                continue;
            }

            double montant = 0;
            try {
                montant = Double.parseDouble(r[3].toString().replace(",", "."));
            } catch (Exception ignored) {}

            if ("EMPRUNT".equalsIgnoreCase(type)) totalEmprunts += montant;
            if ("PRET".equalsIgnoreCase(type)) totalPrets += montant;
        }

        lblTotalEmprunts.setText("Total emprunts en cours: " + formatDh(totalEmprunts));
        lblTotalPrets.setText("Total prêts en cours: " + formatDh(totalPrets));
    }

    private void showDetailSelected() {
        Long id = getSelectedId();
        if (id == null) return;

        int row = table.getSelectedRow();
        String type = safe(model.getValueAt(row, 1));
        String personne = safe(model.getValueAt(row, 2));
        String montant = safe(model.getValueAt(row, 3));
        String date = safe(model.getValueAt(row, 4));
        String statut = safe(model.getValueAt(row, 5));
        String motif = safe(model.getValueAt(row, 6));

        JOptionPane.showMessageDialog(this,
                "Emprunt/Prêt #" + id + "\n" +
                "Type: " + type + "\n" +
                "Personne: " + personne + "\n" +
                "Montant: " + montant + "\n" +
                "Date: " + date + "\n" +
                "Statut: " + statut + "\n" +
                "Motif: " + motif,
                "Détail",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private Long getSelectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiDialogs.warn(this, "Sélectionne une ligne d'abord.");
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

    private String formatDh(double v) {
        if (v == (long) v) return ((long) v) + " DH";
        return String.format("%.2f DH", v);
    }

    private String safe(Object o) {
        return o == null ? "" : o.toString();
    }
}