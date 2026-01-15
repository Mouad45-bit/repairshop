package com.maven.repairshop.ui.dialogs;

import com.maven.repairshop.model.Client;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.service.ClientService;
import com.maven.repairshop.service.exceptions.BusinessException;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.ServiceRegistry;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ClientDialog extends JDialog {

    private final SessionContext session;
    private final ClientService clientService = ServiceRegistry.get().clients();

    private final Long editId; // null => create
    private boolean saved = false;

    private JTextField txtNom;
    private JTextField txtTel;
    private JTextField txtEmail;
    private JTextField txtVille;
    private JTextField txtAdresse;

    private JButton btnSave;

    public ClientDialog(Window owner, SessionContext session, Long editId) {
        super(owner, editId == null ? "Ajouter client" : "Modifier client", ModalityType.APPLICATION_MODAL);
        this.session = session;
        this.editId = editId;

        setSize(520, 420);
        setLocationRelativeTo(owner);
        setContentPane(buildUi());

        if (editId != null) loadClient(editId);
    }

    public boolean isSaved() { return saved; }

    private JComponent buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel(editId == null ? "Nouveau client" : "Modifier client");
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
        txtTel = new JTextField();
        txtEmail = new JTextField();
        txtVille = new JTextField();
        txtAdresse = new JTextField();

        int row = 0;
        c.gridy = row++; form.add(labeled("Nom *", txtNom), c);
        c.gridy = row++; form.add(labeled("Téléphone", txtTel), c);
        c.gridy = row++; form.add(labeled("Email", txtEmail), c);
        c.gridy = row++; form.add(labeled("Ville", txtVille), c);
        c.gridy = row++; form.add(labeled("Adresse", txtAdresse), c);

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
        btnSave = new JButton("Enregistrer");

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> save());

        p.add(btnCancel);
        p.add(btnSave);
        return p;
    }

    private void loadClient(Long id) {
        try {
            Long userId = session.getCurrentUser().getId();
            Client c = clientService.trouverParId(id, userId);
            if (c == null) {
                UiDialogs.error(this, "Client introuvable.");
                dispose();
                return;
            }

            txtNom.setText(nz(c.getNom()));
            txtTel.setText(nz(c.getTelephone()));
            txtEmail.setText(nz(c.getEmail()));
            txtVille.setText(nz(c.getVille()));
            txtAdresse.setText(nz(c.getAdresse()));

        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur chargement client : " + ex.getMessage());
            dispose();
        }
    }

    private void save() {
        btnSave.setEnabled(false);
        try {
            String nom = txtNom.getText().trim();
            String tel = txtTel.getText().trim();
            String email = txtEmail.getText().trim();
            String ville = txtVille.getText().trim();
            String adresse = txtAdresse.getText().trim();

            if (nom.isEmpty()) {
                UiDialogs.error(this, "Le nom est obligatoire.");
                return;
            }

            Long userId = session.getCurrentUser().getId();
            Long reparateurId = (session.getCurrentUser() instanceof Reparateur) ? userId : null;

            if (editId == null) {
                // création sécurisée
                if (reparateurId == null) {
                    UiDialogs.error(this, "Création client : réparateur non déterminé.");
                    return;
                }

                clientService.creerClient(
                        nom,
                        emptyToNull(tel),
                        emptyToNull(email),
                        emptyToNull(adresse),
                        emptyToNull(ville),
                        reparateurId,
                        userId
                );

                UiDialogs.success(this, "Client ajouté.");
            } else {
                // modification sécurisée
                clientService.modifierClient(
                        editId,
                        nom,
                        emptyToNull(tel),
                        emptyToNull(email),
                        emptyToNull(adresse),
                        emptyToNull(ville),
                        userId
                );
                UiDialogs.success(this, "Client modifié.");
            }

            saved = true;
            dispose();

        } catch (BusinessException ex) {
            UiDialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur: " + ex.getMessage());
        } finally {
            btnSave.setEnabled(true);
        }
    }

    private static String nz(String s) { return s == null ? "" : s; }
    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}