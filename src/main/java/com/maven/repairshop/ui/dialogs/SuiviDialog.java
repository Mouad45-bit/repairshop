package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.*;

public class SuiviDialog extends JDialog {

    private JTextField txtCode;

    private JLabel lblStatut;
    private JLabel lblDate;
    private JLabel lblInfo; // message complémentaire (reste à payer, etc.)

    public SuiviDialog(Window owner) {
        super(owner, "Suivi réparation", ModalityType.APPLICATION_MODAL);
        setSize(520, 280);
        setLocationRelativeTo(owner);
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        // ===== Top : saisie code =====
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Code unique :"));
        txtCode = new JTextField(18);
        top.add(txtCode);

        JButton btnSuivre = new JButton("Suivre");
        top.add(btnSuivre);

        add(top, BorderLayout.NORTH);

        // ===== Centre : résultat =====
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        lblStatut = new JLabel("Statut : —");
        lblDate = new JLabel("Dernière mise à jour : —");
        lblInfo = new JLabel(" ");

        lblStatut.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        lblDate.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        lblInfo.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        center.add(lblStatut);
        center.add(lblDate);
        center.add(lblInfo);

        add(center, BorderLayout.CENTER);

        // ===== Bas : boutons =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnFermer = new JButton("Fermer");
        bottom.add(btnFermer);
        add(bottom, BorderLayout.SOUTH);

        // ===== Events =====
        btnFermer.addActionListener(e -> dispose());

        btnSuivre.addActionListener(e -> doSuivre());
        txtCode.addActionListener(e -> doSuivre()); // Entrée = suivre
    }

    private void doSuivre() {
        String code = txtCode.getText() != null ? txtCode.getText().trim() : "";
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir le code unique.");
            return;
        }

        // ===== Version UI-only pour l’instant =====
        // Plus tard, remplacer par :
        // SuiviDTO dto = ServiceRegistry.get().suiviService().suivreParCode(code);
        // lblStatut.setText("Statut : " + dto.getStatut());
        // lblDate.setText("Dernière mise à jour : " + dto.getDateDernierStatut());
        // lblInfo.setText(dto.getResteAPayer() != null ? "Reste à payer : " + dto.getResteAPayer() : " ");

        // Fake data pour voir l’écran fonctionner :
        if (code.equalsIgnoreCase("R-8F2A1")) {
            lblStatut.setText("Statut : EN_COURS");
            lblDate.setText("Dernière mise à jour : 2026-01-12");
            lblInfo.setText("Veuillez patienter. Votre appareil est en cours de réparation.");
        } else if (code.equalsIgnoreCase("R-91BC0")) {
            lblStatut.setText("Statut : TERMINEE");
            lblDate.setText("Dernière mise à jour : 2026-01-10");
            lblInfo.setText("Réparation terminée. Passez récupérer votre appareil.");
        } else {
            lblStatut.setText("Statut : —");
            lblDate.setText("Dernière mise à jour : —");
            lblInfo.setText("Aucune réparation trouvée pour ce code.");
        }
    }
}
