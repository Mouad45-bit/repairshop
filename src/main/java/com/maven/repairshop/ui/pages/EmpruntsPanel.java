package com.maven.repairshop.ui.pages;

import com.maven.repairshop.model.Emprunt;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.model.enums.StatutEmprunt;
import com.maven.repairshop.model.enums.TypeEmprunt;
import com.maven.repairshop.service.EmpruntService;
import com.maven.repairshop.service.exceptions.BusinessException;
import com.maven.repairshop.ui.dialogs.EmpruntDialog;
import com.maven.repairshop.ui.dialogs.EmpruntStatutDialog;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.ServiceRegistry;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmpruntsPanel extends JPanel {

    private final SessionContext session;
    private final EmpruntService empruntService = ServiceRegistry.get().emprunts();

    private JTextField txtSearch;
    private JComboBox<Object> cbType;
    private JComboBox<Object> cbStatut;

    private JTextField txtDateFrom; // YYYY-MM-DD
    private JTextField txtDateTo;   // YYYY-MM-DD

    private JTextField txtReparateurId; // utile si Propriétaire

    private JTable table;
    private DefaultTableModel model;

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EmpruntsPanel(SessionContext session) {
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

        JLabel title = new JLabel("Emprunts");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        // Propriétaire : besoin d’un reparateurId pour lister
        txtReparateurId = new JTextField(8);
        if (session.isReparateur()) {
            txtReparateurId.setText(String.valueOf(session.getCurrentUser().getId()));
            txtReparateurId.setEnabled(false);
        }

        txtSearch = new JTextField(16);

        cbType = new JComboBox<>();
        cbType.addItem("Tous types");
        for (TypeEmprunt t : TypeEmprunt.values()) cbType.addItem(t);

        cbStatut = new JComboBox<>();
        cbStatut.addItem("Tous statuts");
        for (StatutEmprunt s : StatutEmprunt.values()) cbStatut.addItem(s);

        txtDateFrom = new JTextField(10);
        txtDateTo = new JTextField(10);
        txtDateFrom.setToolTipText("YYYY-MM-DD");
        txtDateTo.setToolTipText("YYYY-MM-DD");

        JButton btnSearch = new JButton("Rechercher");
        JButton btnRefresh = new JButton("Actualiser");

        btnSearch.addActionListener(e -> refresh());
        btnRefresh.addActionListener(e -> refresh());
        cbType.addActionListener(e -> refresh());
        cbStatut.addActionListener(e -> refresh());

        if (session.isProprietaire()) {
            filters.add(new JLabel("Réparateur ID:"));
            filters.add(txtReparateurId);
        }

        filters.add(new JLabel("Recherche:"));
        filters.add(txtSearch);

        filters.add(new JLabel("Type:"));
        filters.add(cbType);

        filters.add(new JLabel("Statut:"));
        filters.add(cbStatut);

        filters.add(new JLabel("Du:"));
        filters.add(txtDateFrom);

        filters.add(new JLabel("Au:"));
        filters.add(txtDateTo);

        filters.add(btnSearch);
        filters.add(btnRefresh);

        p.add(title, BorderLayout.WEST);
        p.add(filters, BorderLayout.EAST);

        if (session.isProprietaire()) {
            JLabel hint = new JLabel("Propriétaire: pour lister, saisissez l'ID d’un réparateur de votre boutique.");
            hint.setForeground(new Color(90, 90, 90));
            JPanel south = new JPanel(new BorderLayout());
            south.add(hint, BorderLayout.WEST);

            JPanel wrapper = new JPanel(new BorderLayout(0, 6));
            wrapper.add(p, BorderLayout.CENTER);
            wrapper.add(south, BorderLayout.SOUTH);
            return wrapper;
        }

        return p;
    }

    private JComponent buildTable() {
        model = new DefaultTableModel(
                new Object[]{"ID", "Date", "Type", "Statut", "Personne", "Montant", "Motif"}, 0
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

        JButton btnNew = new JButton("Nouvel emprunt");
        JButton btnStatut = new JButton("Changer statut");
        JButton btnDetails = new JButton("Détails");
        JButton btnDelete = new JButton("Supprimer");

        btnNew.addActionListener(e -> createNew());
        btnStatut.addActionListener(e -> changeStatus());
        btnDetails.addActionListener(e -> showDetails());
        btnDelete.addActionListener(e -> deleteSelected());

        p.add(btnNew);
        p.add(btnStatut);
        p.add(btnDetails);
        p.add(btnDelete);

        return p;
    }

    private void refresh() {
        try {
            model.setRowCount(0);

            Long userId = session.getCurrentUser().getId();
            Long reparateurId = resolveReparateurIdForListing();

            List<Emprunt> list = empruntService.lister(reparateurId, userId);

            // Filtres UI (le backend lister() ne prend pas de query/filtre)
            List<Emprunt> filtered = applyUiFilters(list);

            for (Emprunt e : filtered) {
                String date = e.getDateEmprunt() != null ? e.getDateEmprunt().format(DT) : "-";
                model.addRow(new Object[]{
                        e.getId(),
                        date,
                        e.getType(),
                        e.getStatut(),
                        orDash(e.getNomPersonne()),
                        String.format("%.2f", e.getMontant()),
                        orDash(e.getMotif())
                });
            }

        } catch (BusinessException ex) {
            UiDialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur chargement emprunts : " + ex.getMessage());
        }
    }

    private Long resolveReparateurIdForListing() {
        if (session.isReparateur()) {
            // backend: reparateurId peut être null ou == userId
            return session.getCurrentUser().getId();
        }

        // Propriétaire : reparateurId obligatoire
        String txt = txtReparateurId.getText().trim();
        if (txt.isEmpty()) {
            throw new BusinessException("Propriétaire : veuillez saisir un Réparateur ID pour lister les emprunts.");
        }
        try {
            return Long.parseLong(txt);
        } catch (NumberFormatException nfe) {
            throw new BusinessException("Réparateur ID invalide.");
        }
    }

    private List<Emprunt> applyUiFilters(List<Emprunt> list) {
        String q = txtSearch.getText().trim().toLowerCase();

        TypeEmprunt type = null;
        Object selType = cbType.getSelectedItem();
        if (selType instanceof TypeEmprunt t) type = t;

        StatutEmprunt statut = null;
        Object selStatut = cbStatut.getSelectedItem();
        if (selStatut instanceof StatutEmprunt s) statut = s;

        LocalDate from = parseDateOrNull(txtDateFrom.getText().trim());
        LocalDate to = parseDateOrNull(txtDateTo.getText().trim());

        List<Emprunt> out = new ArrayList<>();
        for (Emprunt e : list) {
            if (type != null && e.getType() != type) continue;
            if (statut != null && e.getStatut() != statut) continue;

            if (!q.isEmpty()) {
                String person = e.getNomPersonne() != null ? e.getNomPersonne().toLowerCase() : "";
                String motif = e.getMotif() != null ? e.getMotif().toLowerCase() : "";
                if (!person.contains(q) && !motif.contains(q)) continue;
            }

            if (from != null || to != null) {
                if (e.getDateEmprunt() == null) continue;
                LocalDate d = e.getDateEmprunt().toLocalDate();
                if (from != null && d.isBefore(from)) continue;
                if (to != null && d.isAfter(to)) continue;
            }

            out.add(e);
        }

        return out;
    }

    private LocalDate parseDateOrNull(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDate.parse(s, DATE);
        } catch (Exception ex) {
            throw new BusinessException("Date invalide: " + s + " (format attendu: YYYY-MM-DD)");
        }
    }

    private Long getSelectedEmpruntId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object id = model.getValueAt(row, 0);
        if (id instanceof Long) return (Long) id;
        return Long.valueOf(String.valueOf(id));
    }

    private void createNew() {
        Long targetReparateurId = resolveReparateurIdForCreate();
        EmpruntDialog dlg = new EmpruntDialog(SwingUtilities.getWindowAncestor(this), session, targetReparateurId);
        dlg.setVisible(true);
        if (dlg.isSaved()) refresh();
    }

    private Long resolveReparateurIdForCreate() {
        if (session.getCurrentUser() instanceof Reparateur) {
            return session.getCurrentUser().getId();
        }
        // Propriétaire : on utilise le champ réparateurId
        return resolveReparateurIdForListing();
    }

    private void changeStatus() {
        Long id = getSelectedEmpruntId();
        if (id == null) {
            UiDialogs.error(this, "Sélectionnez un emprunt.");
            return;
        }

        EmpruntStatutDialog dlg = new EmpruntStatutDialog(SwingUtilities.getWindowAncestor(this), session, id);
        dlg.setVisible(true);
        if (dlg.isSaved()) refresh();
    }

    private void showDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiDialogs.error(this, "Sélectionnez un emprunt.");
            return;
        }

        String date = String.valueOf(model.getValueAt(row, 1));
        String type = String.valueOf(model.getValueAt(row, 2));
        String statut = String.valueOf(model.getValueAt(row, 3));
        String personne = String.valueOf(model.getValueAt(row, 4));
        String montant = String.valueOf(model.getValueAt(row, 5));
        String motif = String.valueOf(model.getValueAt(row, 6));

        UiDialogs.success(this,
                "Détails emprunt\n\n" +
                        "Date: " + date + "\n" +
                        "Type: " + type + "\n" +
                        "Statut: " + statut + "\n" +
                        "Personne: " + personne + "\n" +
                        "Montant: " + montant + "\n" +
                        "Motif: " + motif
        );
    }

    private void deleteSelected() {
        Long id = getSelectedEmpruntId();
        if (id == null) {
            UiDialogs.error(this, "Sélectionnez un emprunt.");
            return;
        }

        if (!UiDialogs.confirm(this, "Supprimer cet emprunt ?")) return;

        try {
            Long userId = session.getCurrentUser().getId();
            empruntService.supprimer(id, userId);
            UiDialogs.success(this, "Emprunt supprimé.");
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