package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.format.DateTimeFormatter;

import javax.swing.*;

import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.ui.controllers.ControllerRegistry;
import com.maven.repairshop.ui.controllers.SuiviController;
import com.maven.repairshop.ui.session.SessionContext;

public class SuiviDialog extends JDialog {

    private final SessionContext session;

    // controller via registry (UI -> ServiceRegistry -> backend)
    private final SuiviController controller = ControllerRegistry.get().suivi();

    private JTextField txtCode;
    private JButton btnSuivre;
    private JButton btnFermer;

    private JLabel lblCode;
    private JLabel lblClient;
    private JLabel lblStatut;
    private JLabel lblDate;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public SuiviDialog(Window owner, SessionContext session) {
        super(owner, "Suivi réparation", ModalityType.APPLICATION_MODAL);
        this.session = session;

        setSize(520, 260);
        setLocationRelativeTo(owner);
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        // Top: input
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Code unique:"));
        txtCode = new JTextField(18);
        top.add(txtCode);

        btnSuivre = new JButton("Suivre");
        top.add(btnSuivre);

        add(top, BorderLayout.NORTH);

        // Center: result
        JPanel center = new JPanel(new GridLayout(4, 2, 10, 8));
        center.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        center.add(new JLabel("Code:"));
        lblCode = new JLabel("-");
        center.add(lblCode);

        center.add(new JLabel("Client:"));
        lblClient = new JLabel("-");
        center.add(lblClient);

        center.add(new JLabel("Statut:"));
        lblStatut = new JLabel("-");
        center.add(lblStatut);

        center.add(new JLabel("Dernière mise à jour:"));
        lblDate = new JLabel("-");
        center.add(lblDate);

        add(center, BorderLayout.CENTER);

        // Bottom: buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnFermer = new JButton("Fermer");
        bottom.add(btnFermer);
        add(bottom, BorderLayout.SOUTH);

        // events
        btnSuivre.addActionListener(e -> onSuivre());
        btnFermer.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(btnSuivre);
    }

    private void onSuivre() {
        String code = txtCode.getText();

        Long reparateurId = session.getReparateurId();
        if (reparateurId == null) {
            JOptionPane.showMessageDialog(this, "Session invalide (réparateur introuvable).");
            return;
        }

        controller.suivre(this, code, reparateurId, this::showResult);
    }

    private void showResult(Reparation r) {
        lblCode.setText(safe(r.getCodeUnique()));
        lblClient.setText(r.getClient() != null ? safe(r.getClient().getNom()) : "");
        lblStatut.setText(r.getStatut() != null ? r.getStatut().name() : "");
        lblDate.setText(r.getDateDernierStatut() != null ? r.getDateDernierStatut().format(DT_FMT) : "");
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}