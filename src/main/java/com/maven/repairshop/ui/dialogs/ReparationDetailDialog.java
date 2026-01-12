package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.format.DateTimeFormatter;

import javax.swing.*;

import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.ui.controllers.ReparationController;
import com.maven.repairshop.ui.session.SessionContext;

public class ReparationDetailDialog extends JDialog {

    private final SessionContext session;
    private final ReparationController controller = new ReparationController();

    private final Long reparationId;

    private JLabel lblId;
    private JLabel lblCode;
    private JLabel lblClient;
    private JLabel lblTelephone;
    private JLabel lblStatut;
    private JLabel lblDernierStatut;

    private JButton btnFermer;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReparationDetailDialog(Window owner, SessionContext session, Long reparationId) {
        super(owner, "Détail réparation", ModalityType.APPLICATION_MODAL);
        this.session = session;
        this.reparationId = reparationId;

        setSize(560, 320);
        setLocationRelativeTo(owner);
        initUi();
        loadData();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        JPanel center = new JPanel(new GridLayout(6, 2, 10, 10));
        center.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        center.add(new JLabel("ID:"));
        lblId = new JLabel("-");
        center.add(lblId);

        center.add(new JLabel("Code unique:"));
        lblCode = new JLabel("-");
        center.add(lblCode);

        center.add(new JLabel("Client:"));
        lblClient = new JLabel("-");
        center.add(lblClient);

        center.add(new JLabel("Téléphone:"));
        lblTelephone = new JLabel("-");
        center.add(lblTelephone);

        center.add(new JLabel("Statut:"));
        lblStatut = new JLabel("-");
        center.add(lblStatut);

        center.add(new JLabel("Dernière mise à jour:"));
        lblDernierStatut = new JLabel("-");
        center.add(lblDernierStatut);

        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnFermer = new JButton("Fermer");
        btnFermer.addActionListener(e -> dispose());
        bottom.add(btnFermer);

        add(bottom, BorderLayout.SOUTH);
    }

    private void loadData() {
        controller.trouverParId(this, reparationId, this::fill);
    }

    private void fill(Reparation r) {
        lblId.setText(r.getId() != null ? String.valueOf(r.getId()) : "-");
        lblCode.setText(safe(r.getCodeUnique()));

        if (r.getClient() != null) {
            lblClient.setText(safe(r.getClient().getNom()));
            lblTelephone.setText(safe(r.getClient().getTelephone()));
        } else {
            lblClient.setText("");
            lblTelephone.setText("");
        }

        lblStatut.setText(r.getStatut() != null ? r.getStatut().name() : "");
        lblDernierStatut.setText(
                r.getDateDernierStatut() != null ? r.getDateDernierStatut().format(DT_FMT) : ""
        );
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}