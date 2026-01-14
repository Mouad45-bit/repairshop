package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.format.DateTimeFormatter;

import javax.swing.*;

import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.model.enums.StatutReparation;
import com.maven.repairshop.ui.controllers.ControllerRegistry;
import com.maven.repairshop.ui.controllers.ReparationController;
import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.session.SessionContext;

/**
 * - Afficher détail réparation
 * - Modifier le statut depuis le détail (Combo + bouton Enregistrer)
 * - Appels uniquement via ReparationController (contract)
 */
public class ReparationDetailDialog extends JDialog {

    private final SessionContext session;

    // controller via registry (UI -> ServiceRegistry -> backend)
    private final ReparationController controller = ControllerRegistry.get().reparations();

    private final Long reparationId;

    // Affichage
    private JLabel lblId;
    private JLabel lblCode;
    private JLabel lblClient;
    private JLabel lblTelephone;
    private JLabel lblDernierStatut;

    // Statut éditable
    private JComboBox<StatutReparation> cbStatut;
    private JButton btnEnregistrer;

    private JButton btnFermer;

    private Reparation current; // snapshot chargé

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReparationDetailDialog(Window owner, SessionContext session, Long reparationId) {
        super(owner, "Détail réparation", ModalityType.APPLICATION_MODAL);
        this.session = session;
        this.reparationId = reparationId;

        setSize(620, 360);
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

        // Statut (modifiable)
        center.add(new JLabel("Statut:"));
        cbStatut = new JComboBox<>(StatutReparation.values());
        center.add(cbStatut);

        center.add(new JLabel("Dernière mise à jour:"));
        lblDernierStatut = new JLabel("-");
        center.add(lblDernierStatut);

        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnEnregistrer = new JButton("Enregistrer");
        btnFermer = new JButton("Fermer");

        btnEnregistrer.setEnabled(false);

        btnEnregistrer.addActionListener(e -> onEnregistrer());
        btnFermer.addActionListener(e -> dispose());

        cbStatut.addActionListener(e -> refreshSaveEnabled());

        bottom.add(btnEnregistrer);
        bottom.add(btnFermer);

        add(bottom, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnEnregistrer);
    }

    private void loadData() {
        controller.trouverParId(this, reparationId, this::fill);
    }

    private void fill(Reparation r) {
        this.current = r;

        lblId.setText(r.getId() != null ? String.valueOf(r.getId()) : "-");
        lblCode.setText(safe(r.getCodeUnique()));

        if (r.getClient() != null) {
            lblClient.setText(safe(r.getClient().getNom()));
            lblTelephone.setText(safe(r.getClient().getTelephone()));
        } else {
            lblClient.setText("");
            lblTelephone.setText("");
        }

        if (r.getStatut() != null) cbStatut.setSelectedItem(r.getStatut());

        lblDernierStatut.setText(
                r.getDateDernierStatut() != null ? r.getDateDernierStatut().format(DT_FMT) : ""
        );

        refreshSaveEnabled();
    }

    private void refreshSaveEnabled() {
        if (current == null) {
            btnEnregistrer.setEnabled(false);
            return;
        }
        StatutReparation selected = (StatutReparation) cbStatut.getSelectedItem();
        btnEnregistrer.setEnabled(selected != null && current.getStatut() != selected);
    }

    private void onEnregistrer() {
        if (current == null) return;

        StatutReparation nouveau = (StatutReparation) cbStatut.getSelectedItem();
        if (nouveau == null) return;

        // Si pas de changement, rien à faire
        if (current.getStatut() == nouveau) {
            UiDialogs.info(this, "Aucun changement de statut.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Confirmer le changement de statut vers : " + nouveau.name() + " ?",
                "Changer statut",
                JOptionPane.YES_NO_OPTION
        );
        if (ok != JOptionPane.YES_OPTION) {
            // remet l'ancien statut (sécurité UI)
            cbStatut.setSelectedItem(current.getStatut());
            refreshSaveEnabled();
            return;
        }

        // Appel contract via controller
        controller.changerStatut(this, reparationId, nouveau, () -> {
            // Recharge après save pour refléter dateDernierStatut, statut, etc.
            loadData();
            UiDialogs.info(this, "Statut mis à jour.");
        });
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}