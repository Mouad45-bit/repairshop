package com.maven.repairshop.ui.dialogs;

import com.maven.repairshop.model.Proprietaire;
import com.maven.repairshop.service.exceptions.BusinessException;
import com.maven.repairshop.ui.controllers.ProprietaireController;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ReparateurDialog extends JDialog {

    private final SessionContext session;
    private final ProprietaireController proprietaireCtrl = new ProprietaireController();

    private final Long boutiqueId;
    private boolean saved = false;

    private JTextField txtNom;
    private JTextField txtLogin;
    private JPasswordField txtPwd;
    private JPasswordField txtPwd2;

    private JButton btnSave;

    public ReparateurDialog(Window owner, SessionContext session, Long boutiqueId) {
        super(owner, "Ajouter réparateur", ModalityType.APPLICATION_MODAL);
        this.session = session;
        this.boutiqueId = boutiqueId;

        setSize(560, 420);
        setLocationRelativeTo(owner);
        setContentPane(buildUi());
        getRootPane().setDefaultButton(btnSave);
    }

    public boolean isSaved() { return saved; }

    private JComponent buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Nouveau réparateur");
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
        txtLogin = new JTextField();
        txtPwd = new JPasswordField();
        txtPwd2 = new JPasswordField();

        int row = 0;
        c.gridy = row++; form.add(readonlyLine("Boutique ID", String.valueOf(boutiqueId)), c);
        c.gridy = row++; form.add(labeled("Nom *", txtNom), c);
        c.gridy = row++; form.add(labeled("Login *", txtLogin), c);
        c.gridy = row++; form.add(labeled("Mot de passe *", txtPwd), c);
        c.gridy = row++; form.add(labeled("Confirmer mot de passe *", txtPwd2), c);

        root.add(form, BorderLayout.CENTER);
        root.add(buildActions(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel readonlyLine(String label, String value) {
        JTextField t = new JTextField(value);
        t.setEnabled(false);
        return labeled(label, t);
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
                UiDialogs.error(this, "Accès refusé: seul un propriétaire peut créer un réparateur.");
                return;
            }

            String nom = txtNom.getText().trim();
            String login = txtLogin.getText().trim();
            String pwd = new String(txtPwd.getPassword()).trim();
            String pwd2 = new String(txtPwd2.getPassword()).trim();

            if (nom.isEmpty() || login.isEmpty() || pwd.isEmpty()) {
                UiDialogs.error(this, "Nom, login et mot de passe sont obligatoires.");
                return;
            }
            if (!pwd.equals(pwd2)) {
                UiDialogs.error(this, "Les mots de passe ne correspondent pas.");
                return;
            }

            Long userId = session.getCurrentUser().getId();
            proprietaireCtrl.creerReparateur(boutiqueId, nom, login, pwd, userId);

            saved = true;
            UiDialogs.success(this, "Réparateur créé.");
            dispose();

        } catch (BusinessException ex) {
            UiDialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur : " + ex.getMessage());
        } finally {
            btnSave.setEnabled(true);
        }
    }
}