package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.*;

import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.ui.controllers.ReparationController;
import com.maven.repairshop.ui.session.SessionContext;

/**
 * - Créer une réparation via le contract ReparationService.creerReparation(clientId, reparateurId)
 * - Ici on demande seulement le clientId (le reste viendra plus tard: objets/causes/paiements)
 */
public class ReparationFormDialog extends JDialog {

    private final SessionContext session;
    private final ReparationController controller = new ReparationController();

    private boolean saved = false;
    private Reparation created = null;

    private JTextField txtClientId;

    private JButton btnEnregistrer;
    private JButton btnAnnuler;

    public ReparationFormDialog(Window owner, SessionContext session) {
        super(owner, "Ajouter réparation", ModalityType.APPLICATION_MODAL);
        this.session = session;

        setSize(520, 220);
        setLocationRelativeTo(owner);
        initUi();
    }

    public boolean isSaved() {
        return saved;
    }

    public Reparation getCreated() {
        return created;
    }

    private void initUi() {
        setLayout(new BorderLayout());

        JPanel center = new JPanel(new GridLayout(2, 2, 10, 10));
        center.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        center.add(new JLabel("Client ID:"));
        txtClientId = new JTextField();
        center.add(txtClientId);

        // petit texte aide
        center.add(new JLabel(""));
        center.add(new JLabel("<html><span style='color:#666'>Astuce: récupère l'ID depuis la page Clients.</span></html>"));

        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnEnregistrer = new JButton("Enregistrer");
        btnAnnuler = new JButton("Annuler");

        btnEnregistrer.addActionListener(e -> onSave());
        btnAnnuler.addActionListener(e -> dispose());

        bottom.add(btnEnregistrer);
        bottom.add(btnAnnuler);

        add(bottom, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnEnregistrer);
    }

    private void onSave() {
        Long clientId = parseLong(txtClientId.getText());
        if (clientId == null || clientId <= 0) {
            JOptionPane.showMessageDialog(this, "Client ID invalide.");
            return;
        }

        Long reparateurId = currentReparateurId();

        controller.creerReparation(this, clientId, reparateurId, rep -> {
            this.created = rep;
            this.saved = true;
            dispose();
        });
    }

    private Long currentReparateurId() {
        try {
            var user = session.getUser();
            if (user != null) return user.getId();
        } catch (Exception ignored) {}
        return null;
    }

    private static Long parseLong(String s) {
        try {
            if (s == null) return null;
            String t = s.trim();
            if (t.isEmpty()) return null;
            return Long.parseLong(t);
        } catch (Exception e) {
            return null;
        }
    }
}