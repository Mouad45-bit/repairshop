package com.maven.repairshop.ui.pages;

import com.maven.repairshop.model.Client;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.service.ClientService;
import com.maven.repairshop.service.exceptions.BusinessException;
import com.maven.repairshop.ui.dialogs.ClientDialog;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.ServiceRegistry;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientsPanel extends JPanel {

    private final SessionContext session;
    private final ClientService clientService = ServiceRegistry.get().clients();

    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel model;

    public ClientsPanel(SessionContext session) {
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

        JLabel title = new JLabel("Clients");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        txtSearch = new JTextField(24);

        JButton btnSearch = new JButton("Rechercher");
        JButton btnRefresh = new JButton("Actualiser");

        btnSearch.addActionListener(e -> refresh());
        btnRefresh.addActionListener(e -> refresh());

        right.add(new JLabel("Recherche:"));
        right.add(txtSearch);
        right.add(btnSearch);
        right.add(btnRefresh);

        p.add(title, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JComponent buildTable() {
        model = new DefaultTableModel(
                new Object[]{"ID", "Nom", "Téléphone", "Email", "Ville", "Adresse"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // cacher ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        return new JScrollPane(table);
    }

    private JComponent buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        JButton btnAdd = new JButton("Ajouter");
        JButton btnEdit = new JButton("Modifier");
        JButton btnDelete = new JButton("Supprimer");

        btnAdd.addActionListener(e -> addClient());
        btnEdit.addActionListener(e -> editClient());
        btnDelete.addActionListener(e -> deleteClient());

        p.add(btnAdd);
        p.add(btnEdit);
        p.add(btnDelete);

        return p;
    }

    private Long getSelectedClientId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object id = model.getValueAt(row, 0);
        if (id instanceof Long) return (Long) id;
        return Long.valueOf(String.valueOf(id));
    }

    private void refresh() {
        try {
            model.setRowCount(0);

            Long userId = session.getCurrentUser().getId();
            Long reparateurId = (session.getCurrentUser() instanceof Reparateur) ? userId : null;

            String q = txtSearch.getText().trim();
            List<Client> clients = clientService.rechercher(q, reparateurId, userId);

            for (Client c : clients) {
                model.addRow(new Object[]{
                        c.getId(),
                        orDash(c.getNom()),
                        orDash(c.getTelephone()),
                        orDash(c.getEmail()),
                        orDash(c.getVille()),
                        orDash(c.getAdresse())
                });
            }

        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur chargement clients : " + ex.getMessage());
        }
    }

    private void addClient() {
        ClientDialog dlg = new ClientDialog(SwingUtilities.getWindowAncestor(this), session, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) refresh();
    }

    private void editClient() {
        Long id = getSelectedClientId();
        if (id == null) {
            UiDialogs.error(this, "Sélectionnez un client.");
            return;
        }

        ClientDialog dlg = new ClientDialog(SwingUtilities.getWindowAncestor(this), session, id);
        dlg.setVisible(true);
        if (dlg.isSaved()) refresh();
    }

    private void deleteClient() {
        Long id = getSelectedClientId();
        if (id == null) {
            UiDialogs.error(this, "Sélectionnez un client.");
            return;
        }

        if (!UiDialogs.confirm(this, "Supprimer ce client ?")) return;

        try {
            Long userId = session.getCurrentUser().getId();
            clientService.supprimerClient(id, userId);
            UiDialogs.success(this, "Client supprimé.");
            refresh();

        } catch (BusinessException ex) {
            UiDialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur: " + ex.getMessage());
        }
    }

    private static String orDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }
}