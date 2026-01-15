package com.maven.repairshop.ui.pages;

import com.maven.repairshop.model.Boutique;
import com.maven.repairshop.model.Proprietaire;
import com.maven.repairshop.ui.controllers.ProprietaireController;
import com.maven.repairshop.ui.dialogs.BoutiqueCreateDialog;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BoutiquePanel extends JPanel {

    private final SessionContext session;
    private final ProprietaireController proprietaireCtrl = new ProprietaireController();

    private JLabel lblId;
    private JLabel lblNom;
    private JLabel lblAdresse;
    private JLabel lblTel;

    private JButton btnCreate;
    private JButton btnRefresh;

    public BoutiquePanel(SessionContext session) {
        this.session = session;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(14, 14, 14, 14));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        refresh();
    }

    private JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout(10, 10));

        JLabel title = new JLabel("Boutique");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnCreate = new JButton("Créer boutique");
        btnRefresh = new JButton("Actualiser");

        btnCreate.addActionListener(e -> createBoutique());
        btnRefresh.addActionListener(e -> refresh());

        right.add(btnCreate);
        right.add(btnRefresh);

        p.add(title, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JComponent buildContent() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);
        c.anchor = GridBagConstraints.WEST;

        lblId = value("-");
        lblNom = value("-");
        lblAdresse = value("-");
        lblTel = value("-");

        int row = 0;
        addRow(card, c, row++, "ID", lblId);
        addRow(card, c, row++, "Nom", lblNom);
        addRow(card, c, row++, "Adresse", lblAdresse);
        addRow(card, c, row++, "Téléphone", lblTel);

        return card;
    }

    private void addRow(JPanel card, GridBagConstraints c, int row, String label, JLabel value) {
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        JLabel l = new JLabel(label + " :");
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        card.add(l, c);

        c.gridx = 1; c.gridy = row; c.weightx = 1;
        card.add(value, c);
    }

    private JLabel value(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(new Color(60, 60, 60));
        return l;
    }

    private void refresh() {
        try {
            if (!(session.getCurrentUser() instanceof Proprietaire)) {
                btnCreate.setEnabled(false);
                UiDialogs.error(this, "Accès refusé: module réservé au propriétaire.");
                return;
            }

            Long proprietaireId = session.getCurrentUser().getId();
            Boutique b = proprietaireCtrl.getBoutiqueByProprietaire(proprietaireId);

            if (b == null) {
                lblId.setText("-");
                lblNom.setText("Aucune boutique");
                lblAdresse.setText("-");
                lblTel.setText("-");
                btnCreate.setEnabled(true);
                return;
            }

            lblId.setText(String.valueOf(b.getId()));
            lblNom.setText(orDash(b.getNom()));
            lblAdresse.setText(orDash(b.getAdresse()));
            lblTel.setText(orDash(b.getTelephone()));

            // si la boutique existe, on évite 2 boutiques pour le même owner
            btnCreate.setEnabled(false);

        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur chargement boutique : " + ex.getMessage());
        }
    }

    private void createBoutique() {
        BoutiqueCreateDialog dlg = new BoutiqueCreateDialog(SwingUtilities.getWindowAncestor(this), session);
        dlg.setVisible(true);
        if (dlg.isSaved()) refresh();
    }

    private static String orDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }
}