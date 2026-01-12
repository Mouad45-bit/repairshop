package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.*;

import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.ui.controllers.ReparationController;
import com.maven.repairshop.ui.session.SessionContext;

public class ReparationFormDialog extends JDialog {

    private final SessionContext session;
    private final ReparationController controller = new ReparationController();

    private boolean saved = false;
    private Reparation created = null;

    private Long selectedClientId = null;
    private String selectedClientLabel = "";

    private JTextField txtClient;
    private JButton btnChoisirClient;

    private JButton btnEnregistrer;
    private JButton btnAnnuler;

    public ReparationFormDialog(Window owner, SessionContext session) {
        super(owner, "Ajouter réparation", ModalityType.APPLICATION_MODAL);
        this.session = session;

        setSize(580, 250);
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

        JPanel center = new JPanel(new GridLayout(2, 3, 10, 10));
        center.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        center.add(new JLabel("Client:"));

        txtClient = new JTextField();
        txtClient.setEditable(false);
        center.add(txtClient);

        btnChoisirClient = new JButton("Choisir...");
        center.add(btnChoisirClient);

        center.add(new JLabel(""));
        center.add(new JLabel("<html><span style='color:#666'>Choisis un client puis Enregistrer.</span></html>"));
        center.add(new JLabel(""));

        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnEnregistrer = new JButton("Enregistrer");
        btnAnnuler = new JButton("Annuler");

        btnEnregistrer.setEnabled(false);

        btnChoisirClient.addActionListener(e -> onPickClient());
        btnEnregistrer.addActionListener(e -> onSave());
        btnAnnuler.addActionListener(e -> dispose());

        bottom.add(btnEnregistrer);
        bottom.add(btnAnnuler);

        add(bottom, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnEnregistrer);
    }

    private void onPickClient() {
        Window w = getOwner();
        ClientPickerDialog dlg = new ClientPickerDialog(w, session);
        dlg.setVisible(true);

        if (!dlg.isSelected()) return;

        Long id = dlg.getPickedId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Client invalide.");
            return;
        }

        selectedClientId = id;

        // label depuis le client minimal affiché
        String nom = (dlg.getPicked() != null) ? safe(dlg.getPicked().getNom()) : "";
        selectedClientLabel = id + (nom.isEmpty() ? "" : " - " + nom);

        txtClient.setText(selectedClientLabel);
        btnEnregistrer.setEnabled(true);
    }

    private void onSave() {
        if (selectedClientId == null || selectedClientId <= 0) {
            JOptionPane.showMessageDialog(this, "Choisis un client.");
            return;
        }

        Long reparateurId = currentReparateurId();

        controller.creerReparation(this, selectedClientId, reparateurId, rep -> {
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

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}