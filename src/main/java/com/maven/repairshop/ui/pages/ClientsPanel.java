package com.maven.repairshop.ui.pages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.maven.repairshop.model.Client;
import com.maven.repairshop.ui.controllers.ClientController;
import com.maven.repairshop.ui.controllers.ControllerRegistry;
import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.session.SessionContext;

public class ClientsPanel extends JPanel {

    private final SessionContext session;

    //
    private final ClientController ctrl = ControllerRegistry.get().clients();

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    public ClientsPanel(SessionContext session) {
        this.session = session;
        initUi();

        // Charge DB → JTable
        refresh();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        // Top
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

        // Table
        model = new DefaultTableModel(new Object[] { "ID", "Nom", "Téléphone", "Email", "Ville" }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom actions
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnEdit = new JButton("Modifier");
        JButton btnDelete = new JButton("Supprimer");
        JButton btnDetail = new JButton("Détail");
        bottom.add(btnEdit);
        bottom.add(btnDelete);
        bottom.add(btnDetail);
        add(bottom, BorderLayout.SOUTH);

        // Events (branchés controller)
        btnSearch.addActionListener(e -> refresh());
        txtSearch.addActionListener(e -> refresh());

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            refresh();
        });

        // Ajout (pour l’instant via inputs simples)
        btnAdd.addActionListener(e -> createClient());

        // Modif (inputs simples)
        btnEdit.addActionListener(e -> editClient());

        // Suppression (controller)
        btnDelete.addActionListener(e -> deleteClient());

        // Détail (simple affichage pour l’instant)
        btnDetail.addActionListener(e -> showDetail());
    }

    private void refresh() {
        Long reparateurId = session.getReparateurId();
        if (reparateurId == null) {
            UiDialogs.warn(this, "Cette page est réservée au réparateur (session invalide).");
            return;
        }

        String q = txtSearch.getText() == null ? "" : txtSearch.getText().trim();

        if (q.isEmpty()) {
            ctrl.lister(this, reparateurId, this::fillTable);
        } else {
            ctrl.rechercher(this, reparateurId, q, this::fillTable);
        }
    }

    private void fillTable(java.util.List<Client> list) {
        model.setRowCount(0);
        for (Client c : list) {
            model.addRow(new Object[] {
                    c.getId(),
                    c.getNom(),
                    c.getTelephone(),
                    c.getEmail(),
                    c.getVille()
            });
        }
    }

    private void createClient() {
        Long reparateurId = session.getReparateurId();
        if (reparateurId == null) {
            UiDialogs.warn(this, "Cette page est réservée au réparateur.");
            return;
        }

        // Saisie rapide (tu peux remplacer par ClientDialog plus tard)
        String nom = ask("Nom*", "");
        if (nom == null) return;

        String tel = ask("Téléphone*", "");
        if (tel == null) return;

        String email = ask("Email (optionnel)", "");
        if (email == null) return;

        String adresse = ask("Adresse (optionnel)", "");
        if (adresse == null) return;

        String ville = ask("Ville (optionnel)", "");
        if (ville == null) return;

        String imagePath = ""; // pas obligatoire (plus tard)

        ctrl.creer(this, reparateurId, nom, tel, email, adresse, ville, imagePath, created -> {
            UiDialogs.info(this, "Client ajouté.");
            refresh();
        });
    }

    private void editClient() {
        Long id = getSelectedId();
        if (id == null) return;

        // Pré-remplissage depuis la ligne JTable (simple)
        int row = table.getSelectedRow();
        String oldNom = safe(model.getValueAt(row, 1));
        String oldTel = safe(model.getValueAt(row, 2));
        String oldEmail = safe(model.getValueAt(row, 3));
        String oldVille = safe(model.getValueAt(row, 4));

        String nom = ask("Nom*", oldNom);
        if (nom == null) return;

        String tel = ask("Téléphone*", oldTel);
        if (tel == null) return;

        String email = ask("Email (optionnel)", oldEmail);
        if (email == null) return;

        // Adresse non visible dans table → on laisse vide (ou tu ajoutes une colonne)
        String adresse = ask("Adresse (optionnel)", "");
        if (adresse == null) return;

        String ville = ask("Ville (optionnel)", oldVille);
        if (ville == null) return;

        String imagePath = ""; // plus tard

        ctrl.modifier(this, id, nom, tel, email, adresse, ville, imagePath, () -> {
            UiDialogs.info(this, "Client modifié.");
            refresh();
        });
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

        ctrl.supprimer(this, id, () -> {
            UiDialogs.info(this, "Client supprimé.");
            refresh();
        });
    }

    private void showDetail() {
        Long id = getSelectedId();
        if (id == null) return;

        int row = table.getSelectedRow();
        String nom = safe(model.getValueAt(row, 1));
        String tel = safe(model.getValueAt(row, 2));
        String email = safe(model.getValueAt(row, 3));
        String ville = safe(model.getValueAt(row, 4));

        JOptionPane.showMessageDialog(
                this,
                "Client #" + id + "\n" +
                "Nom: " + nom + "\n" +
                "Téléphone: " + tel + "\n" +
                "Email: " + email + "\n" +
                "Ville: " + ville + "\n\n" +
                "(Plus tard: ouvrir une vraie page/detail + historique réparations.)",
                "Détail client",
                JOptionPane.INFORMATION_MESSAGE
        );
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

    private String ask(String label, String initial) {
        return (String) JOptionPane.showInputDialog(
                this,
                label,
                "Saisie",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                initial
        );
    }

    private String safe(Object o) {
        return o == null ? "" : o.toString();
    }
}