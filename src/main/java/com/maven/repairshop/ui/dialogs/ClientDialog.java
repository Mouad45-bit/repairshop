package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.*;

public class ClientDialog extends JDialog {

    public enum Mode { CREATE, EDIT }

    public static final class ClientFormData {
        public final String nom;
        public final String telephone;
        public final String email;
        public final String adresse;
        public final String ville;

        public ClientFormData(String nom, String telephone, String email, String adresse, String ville) {
            this.nom = nom;
            this.telephone = telephone;
            this.email = email;
            this.adresse = adresse;
            this.ville = ville;
        }
    }

    private JTextField txtNom;
    private JTextField txtTel;
    private JTextField txtEmail;
    private JTextField txtAdresse;
    private JTextField txtVille;

    private boolean saved = false;
    private Mode mode = Mode.CREATE;

    private Long clientId = null; // utile en EDIT
    private ClientFormData formData = null;

    public ClientDialog(Window owner) {
        super(owner, "Client", ModalityType.APPLICATION_MODAL);
        setSize(460, 340);
        setLocationRelativeTo(owner);
        initUi();
        setModeCreate(); // par défaut
    }

    // ===== API utilisée par ClientsPanel =====

    public boolean isSaved() {
        return saved;
    }

    public Mode getMode() {
        return mode;
    }

    public Long getClientId() {
        return clientId;
    }

    public ClientFormData getFormData() {
        return formData;
    }

    /** Mode création : champs vides */
    public void setModeCreate() {
        this.mode = Mode.CREATE;
        this.clientId = null;
        setTitle("Ajouter client");
        clearFields();
        this.saved = false;
        this.formData = null;
    }

    /**
     * Mode édition : tu passes les valeurs (pré-remplissage)
     * (On ne charge pas depuis DB ici, c’est le Panel qui a déjà les infos.)
     */
    public void setModeEdit(Long clientId, String nom, String tel, String email, String adresse, String ville) {
        this.mode = Mode.EDIT;
        this.clientId = clientId;
        setTitle("Modifier client #" + clientId);

        txtNom.setText(nvl(nom));
        txtTel.setText(nvl(tel));
        txtEmail.setText(nvl(email));
        txtAdresse.setText(nvl(adresse));
        txtVille.setText(nvl(ville));

        this.saved = false;
        this.formData = null;
    }

    // ===== UI =====

    private void initUi() {
        setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        txtNom = new JTextField();
        txtTel = new JTextField();
        txtEmail = new JTextField();
        txtAdresse = new JTextField();
        txtVille = new JTextField();

        form.add(row("Nom*", txtNom));
        form.add(row("Téléphone*", txtTel));
        form.add(row("Email", txtEmail));
        form.add(row("Adresse", txtAdresse));
        form.add(row("Ville", txtVille));

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancel = new JButton("Annuler");
        JButton btnSave = new JButton("Enregistrer");
        bottom.add(btnCancel);
        bottom.add(btnSave);
        add(bottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> {
            saved = false;
            formData = null;
            dispose();
        });

        btnSave.addActionListener(e -> onSave());

        getRootPane().setDefaultButton(btnSave);
    }

    private void onSave() {
        String nom = txtNom.getText() != null ? txtNom.getText().trim() : "";
        String tel = txtTel.getText() != null ? txtTel.getText().trim() : "";
        String email = txtEmail.getText() != null ? txtEmail.getText().trim() : "";
        String adresse = txtAdresse.getText() != null ? txtAdresse.getText().trim() : "";
        String ville = txtVille.getText() != null ? txtVille.getText().trim() : "";

        // Validations UI minimales (les vraies règles restent en service)
        if (nom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nom obligatoire.");
            return;
        }
        if (tel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Téléphone obligatoire.");
            return;
        }

        this.formData = new ClientFormData(nom, tel, email, adresse, ville);
        this.saved = true;
        dispose();
    }

    private void clearFields() {
        txtNom.setText("");
        txtTel.setText("");
        txtEmail.setText("");
        txtAdresse.setText("");
        txtVille.setText("");
    }

    private JPanel row(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.add(new JLabel(label), BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return p;
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}