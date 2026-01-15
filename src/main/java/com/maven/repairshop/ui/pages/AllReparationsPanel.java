package com.maven.repairshop.ui.pages;

import com.maven.repairshop.model.Client;
import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.model.enums.StatutReparation;
import com.maven.repairshop.service.ReparationService;
import com.maven.repairshop.service.exceptions.BusinessException;
import com.maven.repairshop.service.impl.ReparationServiceImpl;
import com.maven.repairshop.ui.dialogs.ReparationDetailDialog;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AllReparationsPanel extends JPanel {

    private final SessionContext session;
    private final ReparationService reparationService = new ReparationServiceImpl();

    private JTextField txtSearch;
    private JComboBox<Object> cbStatut;
    private JTextField txtReparateurId; // filtre optionnel

    private JTable table;
    private DefaultTableModel model;

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AllReparationsPanel(SessionContext session) {
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

        JLabel title = new JLabel("Toutes les réparations (boutique)");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        txtSearch = new JTextField(16);

        cbStatut = new JComboBox<>();
        cbStatut.addItem("Tous statuts");
        for (StatutReparation s : StatutReparation.values()) cbStatut.addItem(s);

        txtReparateurId = new JTextField(8);
        txtReparateurId.setToolTipText("Filtrer par réparateur id (optionnel)");

        JButton btnSearch = new JButton("Rechercher");
        JButton btnRefresh = new JButton("Actualiser");

        btnSearch.addActionListener(e -> refresh());
        btnRefresh.addActionListener(e -> refresh());
        cbStatut.addActionListener(e -> refresh());

        right.add(new JLabel("Recherche:"));
        right.add(txtSearch);

        right.add(new JLabel("Statut:"));
        right.add(cbStatut);

        right.add(new JLabel("Réparateur ID:"));
        right.add(txtReparateurId);

        right.add(btnSearch);
        right.add(btnRefresh);

        p.add(title, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);

        JLabel hint = new JLabel("Propriétaire: vous voyez uniquement les réparations de votre boutique.");
        hint.setForeground(new Color(90, 90, 90));

        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.add(p, BorderLayout.CENTER);
        wrapper.add(hint, BorderLayout.SOUTH);

        return wrapper;
    }

    private JComponent buildTable() {
        model = new DefaultTableModel(
                new Object[]{"ID", "Code", "Client", "Réparateur", "Créée le", "Statut"}, 0
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

        JButton btnDetails = new JButton("Détails");
        JButton btnOpen = new JButton("Consulter");
        JButton btnRefresh = new JButton("Actualiser");

        btnDetails.addActionListener(e -> openDetails());
        btnOpen.addActionListener(e -> openDetails());
        btnRefresh.addActionListener(e -> refresh());

        p.add(btnDetails);
        p.add(btnOpen);
        p.add(btnRefresh);

        return p;
    }

    private void refresh() {
        try {
            model.setRowCount(0);

            if (!session.isProprietaire()) {
                UiDialogs.error(this, "Accès refusé: ce module est réservé au propriétaire.");
                return;
            }

            Long userId = session.getCurrentUser().getId();

            String q = txtSearch.getText().trim();

            StatutReparation statut = null;
            Object sel = cbStatut.getSelectedItem();
            if (sel instanceof StatutReparation s) statut = s;

            Long reparateurId = parseLongOrNull(txtReparateurId.getText().trim());

            // backend sécurisé: PROPRIETAIRE voit toute la boutique (reparateurId optionnel)
            List<Reparation> list = reparationService.rechercher(q, reparateurId, statut, userId);

            for (Reparation r : list) {
                Client c = r.getClient();
                Reparateur rep = (c != null ? c.getReparateur() : null);

                model.addRow(new Object[]{
                        r.getId(),
                        orDash(r.getCodeUnique()),
                        c != null ? orDash(c.getNom()) : "-",
                        rep != null ? (orDash(rep.getNom()) + " (id=" + rep.getId() + ")") : "-",
                        r.getDateCreation() != null ? r.getDateCreation().format(DT) : "-",
                        r.getStatut() != null ? r.getStatut().name() : "-"
                });
            }

        } catch (BusinessException ex) {
            UiDialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur chargement réparations : " + ex.getMessage());
        }
    }

    private void openDetails() {
        Long id = getSelectedReparationId();
        if (id == null) {
            UiDialogs.error(this, "Sélectionnez une réparation.");
            return;
        }

        // ouvre ton détail existant
        ReparationDetailDialog dlg =
                new ReparationDetailDialog(SwingUtilities.getWindowAncestor(this), session, id);
        dlg.setVisible(true);

        refresh();
    }

    private Long getSelectedReparationId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object id = model.getValueAt(row, 0);
        if (id instanceof Long) return (Long) id;
        return Long.valueOf(String.valueOf(id));
    }

    private static Long parseLongOrNull(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException nfe) {
            throw new BusinessException("Réparateur ID invalide.");
        }
    }

    private static String orDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }
}