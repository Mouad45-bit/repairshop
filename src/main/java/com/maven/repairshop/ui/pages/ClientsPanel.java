package com.maven.repairshop.ui.pages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.maven.repairshop.model.Client;
import com.maven.repairshop.ui.controllers.ClientController;
import com.maven.repairshop.ui.controllers.ControllerRegistry;
import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.dialogs.ClientDialog;
import com.maven.repairshop.ui.session.SessionContext;

public class ClientsPanel extends JPanel {

    private final SessionContext session;

    // Controller récupéré depuis le registre (branché sur ServiceRegistry -> backend)
    private final ClientController ctrl = ControllerRegistry.get().clients();

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    public ClientsPanel(SessionContext session) {
        this.session = session;
        initUi();
        refresh();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        // ===== TOP =====
        JPanel top = new JPanel(new BorderLayout());

        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(18);
        JButton btnSearch = new JButton("Rechercher");
        JButton btnRefresh = new JButton("Actualiser");

        search.add(new JLabel("Recherche:"));
        search.add(txtSearch);
        search.add(btnSearch);
        search.add(btnRefresh);

        JPanel actionsTop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("+ Ajouter");
        actionsTop.add(btnAdd);

        top.add(search, BorderLayout.CENTER);
        top.add(actionsTop, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(new Object[] { "ID", "Nom", "Téléphone", "Email", "Adresse", "Ville" }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // cacher colonne ID
        TableColumn idCol = table.getColumnModel().getColumn(0);
        idCol.setMinWidth(0);
        idCol.setMaxWidth(0);
        idCol.setPreferredWidth(0);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== BOTTOM =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnEdit = new JButton("Modifier");
        JButton btnDelete = new JButton("Supprimer");
        JButton btnDetail = new JButton("Détail");
        bottom.add(btnEdit);
        bottom.add(btnDelete);
        bottom.add(btnDetail);
        add(bottom, BorderLayout.SOUTH);

        // ===== EVENTS =====
        btnSearch.addActionListener(e -> refresh());
        txtSearch.addActionListener(e -> refresh());

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            refresh();
        });

        btnAdd.addActionListener(e -> createClient());
        btnEdit.addActionListener(e -> editClient());
        btnDelete.addActionListener(e -> deleteClient());
        btnDetail.addActionListener(e -> showDetail());
    }

    private void refresh() {
        Long reparateurId = session.getReparateurId();
        if (reparateurId == null) {
            UiDialogs.warn(this, "Cette page est réservée au réparateur (session invalide).");
            return;
        }

        String q = txtSearch.getText() == null ? "" : txtSearch.getText().trim();

        // Contract backend: rechercher(query, reparateurId)
        try {
            ctrl.rechercher(this, q, reparateurId, this::fillTable);
        } catch (Exception ex) {
            UiDialogs.handle(this, ex);
        }
    }

    private void fillTable(java.util.List<Client> list) {
        model.setRowCount(0);
        for (Client c : list) {
            model.addRow(new Object[] {
                    c.getId(),
                    safe(c.getNom()),
                    safe(c.getTelephone()),
                    safe(c.getEmail()),
                    safe(c.getAdresse()),
                    safe(c.getVille())
            });
        }
    }

    private void createClient() {
        Long reparateurId = session.getReparateurId();
        if (reparateurId == null) {
            UiDialogs.warn(this, "Cette page est réservée au réparateur.");
            return;
        }

        try {
            ClientDialog dlg = new ClientDialog(SwingUtilities.getWindowAncestor(this));
            dlg.setModeCreate();
            dlg.setVisible(true);

            if (!dlg.isSaved()) return;

            ClientDialog.ClientFormData data = dlg.getFormData();
            if (data == null) return;

            ctrl.creer(this,
                    data.nom, data.telephone, data.email, data.adresse, data.ville,
                    reparateurId,
                    created -> {
                        UiDialogs.info(this, "Client ajouté.");
                        refresh();
                    });
        } catch (Exception ex) {
            UiDialogs.handle(this, ex);
        }
    }

    private void editClient() {
        Long id = getSelectedId();
        if (id == null) return;

        int row = table.getSelectedRow();
        String oldNom = safe(model.getValueAt(row, 1));
        String oldTel = safe(model.getValueAt(row, 2));
        String oldEmail = safe(model.getValueAt(row, 3));
        String oldAdresse = safe(model.getValueAt(row, 4));
        String oldVille = safe(model.getValueAt(row, 5));

        try {
            ClientDialog dlg = new ClientDialog(SwingUtilities.getWindowAncestor(this));
            dlg.setModeEdit(id, oldNom, oldTel, oldEmail, oldAdresse, oldVille);
            dlg.setVisible(true);

            if (!dlg.isSaved()) return;

            ClientDialog.ClientFormData data = dlg.getFormData();
            if (data == null) return;

            ctrl.modifier(this, id,
                    data.nom, data.telephone, data.email, data.adresse, data.ville,
                    () -> {
                        UiDialogs.info(this, "Client modifié.");
                        refresh();
                    });
        } catch (Exception ex) {
            UiDialogs.handle(this, ex);
        }
    }

    private void deleteClient() {
        Long id = getSelectedId();
        if (id == null) return;

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Supprimer ce client ?\n(Interdit si le client a des réparations, selon règles métier.)",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            ctrl.supprimer(this, id, () -> {
                UiDialogs.info(this, "Client supprimé.");
                refresh();
            });
        } catch (Exception ex) {
            UiDialogs.handle(this, ex);
        }
    }

    private void showDetail() {
        Long id = getSelectedId();
        if (id == null) return;

        int row = table.getSelectedRow();
        String nom = safe(model.getValueAt(row, 1));
        String tel = safe(model.getValueAt(row, 2));
        String email = safe(model.getValueAt(row, 3));
        String adresse = safe(model.getValueAt(row, 4));
        String ville = safe(model.getValueAt(row, 5));

        UiDialogs.info(this,
                "Client #" + id + "\n" +
                "Nom: " + nom + "\n" +
                "Téléphone: " + tel + "\n" +
                "Email: " + email + "\n" +
                "Adresse: " + adresse + "\n" +
                "Ville: " + ville + "\n\n" +
                "(Plus tard: ouvrir un vrai écran détail + historique réparations.)");
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

    private String safe(Object o) {
        return o == null ? "" : o.toString();
    }
}