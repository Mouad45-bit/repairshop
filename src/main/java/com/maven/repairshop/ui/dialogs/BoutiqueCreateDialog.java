package com.maven.repairshop.ui.dialogs;

import com.maven.repairshop.model.Boutique;
import com.maven.repairshop.model.Proprietaire;
import com.maven.repairshop.service.exceptions.BusinessException;
import com.maven.repairshop.ui.controllers.ProprietaireController;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BoutiqueCreateDialog extends JDialog {

    private final SessionContext session;
    private final ProprietaireController proprietaireCtrl = new ProprietaireController();

    private boolean saved = false;
    private Boutique created;

    private JTextField txtNom;
    private JTextField txtAdresse;
    private JTextField txtTel;

    private JButton btnSave;

    public BoutiqueCreateDialog(Window owner, SessionContext session) {
        super(owner, "Créer boutique", ModalityType.APPLICATION_MODAL);
        this.session = session;

        setSize(560, 380);
        setLocationRelativeTo(owner);
        setContentPane(buildUi());
        getRootPane().setDefaultButton(btnSave);
    }

    public boolean isSaved() { return saved; }
    public Boutique getCreated() { return created; }

    private JComponent buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Nouvelle boutique");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 0, 12, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 0, 6, 0);
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        txtNom = new JTextField();
        txtAdresse = new JTextField();
        txtTel = new JTextField();

        int row = 0;
        c.gridy = row++; form.add(labeled("Nom *", txtNom), c);
        c.gridy = row++; form.add(labeled("Adresse", txtAdresse), c);
        c.gridy = row++; form.add(labeled("Téléphone", txtTel), c);

        root.add(form, BorderLayout.CENTER);
        root.add(buildActions(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        JLabel l = new JLabel(label);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JComponent buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton btnCancel = new JButton("Annuler");
        btnSave = new JButton("Créer");

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> save());

        p.add(btnCancel);
        p.add(btnSave);
        return p;
    }

    private void save() {
        btnSave.setEnabled(false);
        try {
            if (!(session.getCurrentUser() instanceof Proprietaire)) {
                UiDialogs.error(this, "Accès refusé: seul un propriétaire peut créer une boutique.");
                return;
            }

            String nom = txtNom.getText().trim();
            String adresse = txtAdresse.getText().trim();
            String tel = txtTel.getText().trim();

            if (nom.isEmpty()) {
                UiDialogs.error(this, "Le nom est obligatoire.");
                return;
            }

            Long proprietaireId = session.getCurrentUser().getId();
            created = proprietaireCtrl.creerBoutique(
                    proprietaireId,
                    nom,
                    emptyToNull(adresse),
                    emptyToNull(tel)
            );

            saved = true;
            UiDialogs.success(this, "Boutique créée : " + created.getNom());
            dispose();

        } catch (BusinessException ex) {
            UiDialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur : " + ex.getMessage());
        } finally {
            btnSave.setEnabled(true);
        }
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}