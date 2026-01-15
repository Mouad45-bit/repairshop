package com.maven.repairshop.ui.dialogs;

import com.maven.repairshop.model.enums.StatutEmprunt;
import com.maven.repairshop.service.EmpruntService;
import com.maven.repairshop.service.exceptions.BusinessException;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.ServiceRegistry;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EmpruntStatutDialog extends JDialog {

    private final SessionContext session;
    private final EmpruntService empruntService = ServiceRegistry.get().emprunts();

    private final Long empruntId;
    private boolean saved = false;

    private JComboBox<StatutEmprunt> cbStatut;
    private JButton btnSave;

    public EmpruntStatutDialog(Window owner, SessionContext session, Long empruntId) {
        super(owner, "Changer statut", ModalityType.APPLICATION_MODAL);
        this.session = session;
        this.empruntId = empruntId;

        setSize(420, 240);
        setLocationRelativeTo(owner);
        setContentPane(buildUi());
        getRootPane().setDefaultButton(btnSave);
    }

    public boolean isSaved() { return saved; }

    private JComponent buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Mettre à jour le statut");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        root.add(title, BorderLayout.NORTH);

        cbStatut = new JComboBox<>(StatutEmprunt.values());

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.add(new JLabel("Nouveau statut:"), BorderLayout.NORTH);
        center.add(cbStatut, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);
        root.add(buildActions(), BorderLayout.SOUTH);

        return root;
    }

    private JComponent buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton btnCancel = new JButton("Annuler");
        btnSave = new JButton("Enregistrer");

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> save());

        p.add(btnCancel);
        p.add(btnSave);
        return p;
    }

    private void save() {
        btnSave.setEnabled(false);
        try {
            StatutEmprunt statut = (StatutEmprunt) cbStatut.getSelectedItem();
            if (statut == null) {
                UiDialogs.error(this, "Statut invalide.");
                return;
            }

            Long userId = session.getCurrentUser().getId();
            // backend prend un String, on envoie le name()
            empruntService.changerStatut(empruntId, statut.name(), userId);

            saved = true;
            UiDialogs.success(this, "Statut mis à jour.");
            dispose();

        } catch (BusinessException ex) {
            UiDialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur: " + ex.getMessage());
        } finally {
            btnSave.setEnabled(true);
        }
    }
}