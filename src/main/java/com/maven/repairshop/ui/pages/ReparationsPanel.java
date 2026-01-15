package com.maven.repairshop.ui.pages;

import com.maven.repairshop.model.Paiement;
import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.model.enums.StatutReparation;
import com.maven.repairshop.ui.controllers.ReparationController;
import com.maven.repairshop.ui.dialogs.ReparationDialog;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReparationsPanel extends JPanel {

    private final SessionContext session;
    private final ReparationController reparationCtrl = new ReparationController();

    private JTextField txtSearch;
    private JComboBox<Object> cbStatut;

    private JTable table;
    private DefaultTableModel model;

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReparationsPanel(SessionContext session) {
        this.session = session;
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(14, 14, 14, 14));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        refresh();
    }

    private JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout(10, 10));

        JLabel title = new JLabel("Réparations");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        txtSearch = new JTextField(22);

        cbStatut = new JComboBox<>();
        cbStatut.addItem("Tous statuts");
        for (StatutReparation s : StatutReparation.values()) cbStatut.addItem(s);

        JButton btnSearch = new JButton("Rechercher");
        JButton btnRefresh = new JButton("Actualiser");

        btnSearch.addActionListener(e -> refresh());
        btnRefresh.addActionListener(e -> refresh());
        cbStatut.addActionListener(e -> refresh());

        filters.add(new JLabel("Recherche:"));
        filters.add(txtSearch);
        filters.add(new JLabel("Statut:"));
        filters.add(cbStatut);
        filters.add(btnSearch);
        filters.add(btnRefresh);

        p.add(title, BorderLayout.WEST);
        p.add(filters, BorderLayout.EAST);
        return p;
    }

    private JComponent buildTable() {
        model = new DefaultTableModel(
                new Object[]{"ID", "Code", "Client", "Créée le", "Statut", "Appareils", "Total payé"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // cacher la colonne ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        return new JScrollPane(table);
    }

    private JComponent buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        JButton btnNew = new JButton("Nouvelle réparation");
        JButton btnDetails = new JButton("Détails");
        JButton btnView = new JButton("Consulter");

        btnNew.addActionListener(e -> createNew());
        btnDetails.addActionListener(e -> showDetails());
        btnView.addActionListener(e -> showDetails());

        p.add(btnNew);
        p.add(btnDetails);
        p.add(btnView);
        return p;
    }

    private void refresh() {
        try {
            model.setRowCount(0);

            Long userId = session.getCurrentUser().getId();
            Long reparateurId = (session.getCurrentUser() instanceof Reparateur) ? userId : null;

            String q = txtSearch.getText().trim();
            StatutReparation statut = null;
            Object sel = cbStatut.getSelectedItem();
            if (sel instanceof StatutReparation s) statut = s;

            List<Reparation> list = reparationCtrl.rechercher(q, reparateurId, statut, userId);

            for (Reparation r : list) {
                String client = (r.getClient() != null) ? r.getClient().getNom() : "-";
                String date = (r.getDateCreation() != null) ? r.getDateCreation().format(DT) : "-";
                int nbApp = (r.getAppareils() != null) ? r.getAppareils().size() : 0;

                double totalPaye = 0;
                if (r.getPaiements() != null) {
                    for (Paiement p : r.getPaiements()) totalPaye += p.getMontant();
                }

                model.addRow(new Object[]{
                        r.getId(),
                        r.getCodeUnique(),
                        client,
                        date,
                        r.getStatut(),
                        nbApp,
                        String.format("%.2f", totalPaye)
                });
            }

        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur chargement réparations : " + ex.getMessage());
        }
    }

    private Long getSelectedReparationId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object id = model.getValueAt(row, 0);
        return (id instanceof Long) ? (Long) id : Long.valueOf(String.valueOf(id));
    }

    private void createNew() {
        ReparationDialog dlg = new ReparationDialog(SwingUtilities.getWindowAncestor(this), session);
        dlg.setVisible(true);
        if (dlg.isSaved()) refresh();
    }

    private void showDetails() {
        Long id = getSelectedReparationId();
        if (id == null) {
            UiDialogs.error(this, "Sélectionnez une réparation.");
            return;
        }

        // étape suivante (après) : ReparationDetailDialog complet
        // pour l’instant on affiche un résumé propre
        int row = table.getSelectedRow();
        String code = String.valueOf(model.getValueAt(row, 1));
        String client = String.valueOf(model.getValueAt(row, 2));
        String statut = String.valueOf(model.getValueAt(row, 4));

        UiDialogs.success(this,
                "Réparation\n\n" +
                        "Code: " + code + "\n" +
                        "Client: " + client + "\n" +
                        "Statut: " + statut + "\n\n" +
                        "Étape suivante: écran Détail + changement statut + paiements/objets/causes."
        );
    }
}