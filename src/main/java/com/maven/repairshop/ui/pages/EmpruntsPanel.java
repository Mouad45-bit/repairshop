package com.maven.repairshop.ui.pages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.maven.repairshop.ui.dialogs.EmpruntDialog;
import com.maven.repairshop.ui.session.SessionContext;

import com.maven.repairshop.ui.controllers.ControllerRegistry;
import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.model.enums.TypeEmprunt;

public class EmpruntsPanel extends JPanel {

    private final SessionContext session;

    private final var ctrl = ControllerRegistry.get().emprunts();

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

        // On charge directement depuis la DB via controller
        refresh();
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

        // ===== EVENTS =====

        // Ajouter (UI dialog pour l’instant)
        btnAdd.addActionListener(e -> openCreate());

        // Modifier (UI dialog pour l’instant)
        btnEdit.addActionListener(e -> openEditSelected());

        // Détail (simple message pour l’instant)
        btnDetail.addActionListener(e -> showDetailSelected());

        // Marquer remboursé (branché controller)
        btnMarkPaid.addActionListener(e -> {
            Long id = getSelectedId();
            if (id == null) return;

            int ok = JOptionPane.showConfirmDialog(this,
                    "Marquer cet emprunt/prêt comme REMBOURSÉ ?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION);

            if (ok == JOptionPane.YES_OPTION) {
                ctrl.changerStatut(this, id, "REMBOURSE", () -> {
                    UiDialogs.info(this, "Statut mis à jour.");
                    refresh();
                });
            }
        });

        // Rechercher = refresh (filtre appliqué côté UI)
        btnSearch.addActionListener(e -> refresh());

        // Actualiser = refresh
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cbType.setSelectedIndex(0);
            cbStatut.setSelectedIndex(0);
            refresh();
        });

        // Enter dans recherche => refresh
        txtSearch.addActionListener(e -> refresh());
    }

    private void openCreate() {
        EmpruntDialog dlg = new EmpruntDialog(SwingUtilities.getWindowAncestor(this), session);
        dlg.setVisible(true);

        // Si ton dialog met "saved=true", on refresh
        if (dlg.isSaved()) {
            UiDialogs.info(this, "Ajout OK.");
            refresh();
        }
    }

    private void openEditSelected() {
        Long id = getSelectedId();
        if (id == null) return;

        EmpruntDialog dlg = new EmpruntDialog(SwingUtilities.getWindowAncestor(this), session);
        dlg.setModeEdit(id);
        dlg.setVisible(true);

        if (dlg.isSaved()) {
            UiDialogs.info(this, "Modification OK.");
            refresh();
        }
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

    /**
     * Charge depuis la DB via controller (async),
     * puis applique les filtres côté UI et calcule les totaux.
     */
    private void refresh() {
        // Cette page est faite pour le réparateur.
        Long reparateurId = session.getReparateurId();
        if (reparateurId == null) {
            UiDialogs.warn(this, "Cette page est réservée au réparateur (session invalide).");
            return;
        }

        ctrl.lister(this, reparateurId, list -> {
            // Appliquer filtres UI
            List<Object[]> rows = toRowsFiltered(list);

            // Remplir table
            model.setRowCount(0);
            for (Object[] r : rows) model.addRow(r);

            // Totaux
            updateTotalsFromRows(rows);
        });
    }

    private List<Object[]> toRowsFiltered(List<?> rawList) {
        // On travaille en "var" au runtime, pas besoin de type générique
        String q = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        String typeSel = (String) cbType.getSelectedItem();
        String statutSel = (String) cbStatut.getSelectedItem();

        List<Object[]> out = new ArrayList<>();

        for (var e : rawList) {
            // --- champs attendus selon ton modèle ---
            // e.getType() : TypeEmprunt
            // e.getNomPersonne() : String
            // e.getMontant() : double
            // e.getDateEmprunt() : Date/LocalDate/String
            // e.getStatut() : enum
            // e.getMotif() : String

            String personne = safeStr(call(e, "getNomPersonne"));
            String motif = safeStr(call(e, "getMotif"));

            String type = safeStr(call(e, "getType"));   // ex: EMPRUNT/PRET
            String statut = safeStr(call(e, "getStatut")); // ex: EN_COURS/REMBOURSE/PARTIEL

            // filtre personne
            if (!q.isEmpty()) {
                String hay = (personne + " " + motif).toLowerCase();
                if (!hay.contains(q)) continue;
            }

            // filtre type
            if (!"Tous".equalsIgnoreCase(typeSel)) {
                if (!typeSel.equalsIgnoreCase(type)) continue;
            }

            // filtre statut
            if (!"Tous".equalsIgnoreCase(statutSel)) {
                if (!statutSel.equalsIgnoreCase(statut)) continue;
            }

            Object id = call(e, "getId");
            Object montant = call(e, "getMontant");
            Object date = call(e, "getDateEmprunt");

            out.add(new Object[] {
                    id,
                    type,
                    personne,
                    montant,
                    date,
                    statut,
                    motif
            });
        }

        return out;
    }

    /**
     * Totaux "en cours" :
     * - EN_COURS + PARTIEL (toute autre valeur est ignorée)
     * - EMPRUNT => total emprunts
     * - PRET => total prêts
     */
    private void updateTotalsFromRows(List<Object[]> rows) {
        double totalEmprunts = 0;
        double totalPrets = 0;

        for (Object[] r : rows) {
            String type = r[1] != null ? r[1].toString() : "";
            String statut = r[5] != null ? r[5].toString() : "";

            if (!("EN_COURS".equalsIgnoreCase(statut) || "PARTIEL".equalsIgnoreCase(statut))) {
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

    private String formatDh(double v) {
        // simple: pas de DecimalFormat pour rester léger
        if (v == (long) v) return ((long) v) + " DH";
        return String.format("%.2f DH", v);
    }

    // --------- Helpers (évite de casser si ton modèle n'a pas exactement les mêmes méthodes) ---------
    private Object call(Object obj, String method) {
        try {
            return obj.getClass().getMethod(method).invoke(obj);
        } catch (Exception ex) {
            return null;
        }
    }

    private String safeStr(Object v) {
        return v == null ? "" : v.toString();
    }
}