package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.*;

import com.maven.repairshop.ui.controllers.UiDialogs;

public class EmpruntDialog extends JDialog {

    public enum Mode { CREATE, EDIT }

    public static final class EmpruntFormData {
        public final String type;     // "EMPRUNT" | "PRET"
        public final String personne;
        public final String montantStr;
        public final String dateStr;  // YYYY-MM-DD (UI)
        public final String statut;   // "EN_COURS" | "PARTIELLEMENT_REMBOURSE" | "REMBOURSE"
        public final String remarque;

        public EmpruntFormData(String type, String personne, String montantStr, String dateStr, String statut, String remarque) {
            this.type = type;
            this.personne = personne;
            this.montantStr = montantStr;
            this.dateStr = dateStr;
            this.statut = statut;
            this.remarque = remarque;
        }
    }

    private boolean saved = false;
    private Mode mode = Mode.CREATE;
    private Long editId = null;
    private EmpruntFormData formData = null;

    private JComboBox<String> cbType;
    private JTextField txtPersonne;
    private JTextField txtMontant;
    private JTextField txtDate; // YYYY-MM-DD (UI)
    private JComboBox<String> cbStatut;
    private JTextArea txtRemarque;

    public EmpruntDialog(Window owner) {
        super(owner, "Emprunt / Prêt", ModalityType.APPLICATION_MODAL);
        setSize(520, 420);
        setLocationRelativeTo(owner);
        initUi();
        setModeCreate();
    }

    public boolean isSaved() { return saved; }
    public Mode getMode() { return mode; }
    public Long getEditId() { return editId; }
    public EmpruntFormData getFormData() { return formData; }

    public void setModeCreate() {
        this.mode = Mode.CREATE;
        this.editId = null;
        this.saved = false;
        this.formData = null;
        setTitle("Ajouter emprunt / prêt");

        cbType.setSelectedIndex(0);
        cbStatut.setSelectedItem("EN_COURS");
        txtPersonne.setText("");
        txtMontant.setText("");
        txtDate.setText(""); // optionnel UI
        txtRemarque.setText("");
    }

    public void setModeEdit(Long id, String type, String personne, String montant, String date, String statut, String remarque) {
        this.mode = Mode.EDIT;
        this.editId = id;
        this.saved = false;
        this.formData = null;
        setTitle("Modifier emprunt/prêt #" + id);

        cbType.setSelectedItem(type != null ? type : "EMPRUNT");
        cbStatut.setSelectedItem(statut != null ? statut : "EN_COURS");

        txtPersonne.setText(personne != null ? personne : "");
        txtMontant.setText(montant != null ? montant : "");
        txtDate.setText(date != null ? date : "");
        txtRemarque.setText(remarque != null ? remarque : "");
    }

    private void initUi() {
        setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        cbType = new JComboBox<>(new String[] { "EMPRUNT", "PRET" });
        txtPersonne = new JTextField();
        txtMontant = new JTextField();
        txtDate = new JTextField(); // UI-only
        cbStatut = new JComboBox<>(new String[] { "EN_COURS", "PARTIELLEMENT_REMBOURSE", "REMBOURSE" });

        txtRemarque = new JTextArea(5, 20);
        txtRemarque.setLineWrap(true);
        txtRemarque.setWrapStyleWord(true);

        form.add(row("Type*", cbType));
        form.add(row("Personne*", txtPersonne));
        form.add(row("Montant (DH)*", txtMontant));
        form.add(row("Date (YYYY-MM-DD) (UI)", txtDate));
        form.add(row("Statut*", cbStatut));

        JPanel rem = new JPanel(new BorderLayout(8, 8));
        rem.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        rem.add(new JLabel("Remarque / Motif (optionnel)"), BorderLayout.NORTH);
        rem.add(new JScrollPane(txtRemarque), BorderLayout.CENTER);
        form.add(rem);

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
        String type = cbType.getSelectedItem() != null ? cbType.getSelectedItem().toString() : "EMPRUNT";
        String statut = cbStatut.getSelectedItem() != null ? cbStatut.getSelectedItem().toString() : "EN_COURS";

        String personne = txtPersonne.getText() != null ? txtPersonne.getText().trim() : "";
        String montantStr = txtMontant.getText() != null ? txtMontant.getText().trim() : "";
        String date = txtDate.getText() != null ? txtDate.getText().trim() : "";
        String remarque = txtRemarque.getText() != null ? txtRemarque.getText().trim() : "";

        if (personne.isEmpty()) {
            UiDialogs.warn(this, "Personne obligatoire.");
            return;
        }
        if (montantStr.isEmpty()) {
            UiDialogs.warn(this, "Montant obligatoire.");
            return;
        }

        String m = montantStr.replace(",", ".");
        double montant;
        try {
            montant = Double.parseDouble(m);
        } catch (Exception ex) {
            UiDialogs.warn(this, "Montant invalide.");
            return;
        }
        if (montant <= 0) {
            UiDialogs.warn(this, "Le montant doit être > 0.");
            return;
        }

        this.formData = new EmpruntFormData(type, personne, montantStr, date, statut, remarque);
        this.saved = true;
        dispose();
    }

    private JPanel row(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        p.add(new JLabel(label), BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }
}