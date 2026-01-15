package com.maven.repairshop.ui.pages;

import com.maven.repairshop.model.Boutique;
import com.maven.repairshop.model.Proprietaire;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.ui.controllers.ProprietaireController;
import com.maven.repairshop.ui.dialogs.ReparateurDialog;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReparateursPanel extends JPanel {

    private final SessionContext session;
    private final ProprietaireController proprietaireCtrl = new ProprietaireController();

    private JLabel lblBoutique;
    private JButton btnAdd;
    private JButton btnRefresh;

    private JTable table;
    private DefaultTableModel model;

    private Boutique boutique; // boutique du propriétaire connecté

    public ReparateursPanel(SessionContext session) {
        this.session = session;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(14, 14, 14, 14));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);

        refresh();
    }

    private JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout(10, 10));

        JLabel title = new JLabel("Réparateurs");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        lblBoutique = new JLabel("-");
        lblBoutique.setForeground(new Color(90, 90, 90));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnAdd = new JButton("Ajouter réparateur");
        btnRefresh = new JButton("Actualiser");

        btnAdd.addActionListener(e -> addReparateur());
        btnRefresh.addActionListener(e -> refresh());

        right.add(btnAdd);
        right.add(btnRefresh);

        JPanel left = new JPanel(new BorderLayout(0, 6));
        left.add(title, BorderLayout.NORTH);
        left.add(lblBoutique, BorderLayout.SOUTH);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);

        return p;
    }

    private JComponent buildTable() {
        model = new DefaultTableModel(
                new Object[]{"ID", "Nom", "Login"}, 0
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

    private void refresh() {
        try {
            model.setRowCount(0);

            if (!(session.getCurrentUser() instanceof Proprietaire)) {
                btnAdd.setEnabled(false);
                UiDialogs.error(this, "Accès refusé: module réservé au propriétaire.");
                return;
            }

            Long ownerId = session.getCurrentUser().getId();
            boutique = proprietaireCtrl.getBoutiqueByProprietaire(ownerId);

            if (boutique == null) {
                lblBoutique.setText("Aucune boutique. Crée d’abord une boutique.");
                btnAdd.setEnabled(false);
                return;
            }

            lblBoutique.setText("Boutique: " + boutique.getNom() + " (id=" + boutique.getId() + ")");
            btnAdd.setEnabled(true);

            List<Reparateur> reps = proprietaireCtrl.listerReparateurs(boutique.getId());
            for (Reparateur r : reps) {
                model.addRow(new Object[]{
                        r.getId(),
                        orDash(r.getNom()),
                        orDash(r.getLogin())
                });
            }

        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur chargement réparateurs : " + ex.getMessage());
        }
    }

    private void addReparateur() {
        if (boutique == null) {
            UiDialogs.error(this, "Crée d’abord une boutique.");
            return;
        }

        ReparateurDialog dlg = new ReparateurDialog(SwingUtilities.getWindowAncestor(this), session, boutique.getId());
        dlg.setVisible(true);
        if (dlg.isSaved()) refresh();
    }

    private static String orDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }
}