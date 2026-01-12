package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.*;

import com.maven.repairshop.ui.session.SessionContext;

public class EmpruntDialog extends JDialog {

    private final SessionContext session;

    private boolean saved = false;
    private Long editId = null;

    private JComboBox<String> cbType;
    private JTextField txtPersonne;
    private JTextField txtMontant;
    private JTextField txtDate; // simple texte (YYYY-MM-DD) pour l’instant
    private JComboBox<String> cbStatut;
    private JTextArea txtRemarque;

    public EmpruntDialog(Window owner, SessionContext session) {
        super(owner, "Emprunt / Prêt", ModalityType.APPLICATION_MODAL);
        this.session = session;

        setSize(520, 420);
        setLocationRelativeTo(owner);
        initUi();
    }

    public boolean isSaved() {
        return saved;
    }

    /** Mode édition (UI-only). Plus tard on charge depuis service. */
    public void setModeEdit(Long id) {
        this.editId = id;
        setTitle("Modifier emprunt/prêt #" + id);
        // plus tard: loadFromService(id)
    }

    private void initUi() {
        setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        cbType = new JComboBox<>(new String[] { "EMPRUNT", "PRET" });
        txtPersonne = new JTextField();
        txtMontant = new JTextField();
        txtDate = new JTextField("2026-01-12");
        cbStatut = new JComboBox<>(new String[] { "EN_COURS", "PARTIEL", "REMBOURSE" });

        txtRemarque = new JTextArea(5, 20);
        txtRemarque.setLineWrap(true);
        txtRemarque.setWrapStyleWord(true);

        form.add(row("Type*", cbType));
        form.add(row("Personne*", txtPersonne));
        form.add(row("Montant (DH)*", txtMontant));
        form.add(row("Date (YYYY-MM-DD)*", txtDate));
        form.add(row("Statut*", cbStatut));

        JPanel rem = new JPanel(new BorderLayout(8, 8));
        rem.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        rem.add(new JLabel("Remarque (optionnel)"), BorderLayout.NORTH);
        rem.add(new JScrollPane(txtRemarque), BorderLayout.CENTER);
        form.add(rem);

        JLabel hint = new JLabel("Règle métier: montant > 0, personne obligatoire, et accès limité au réparateur/boutique.");
        hint.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        form.add(hint);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancel = new JButton("Annuler");
        JButton btnSave = new JButton("Enregistrer");
        bottom.add(btnCancel);
        bottom.add(btnSave);
        add(bottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());
    }

    private void onSave() {
        String personne = txtPersonne.getText() != null ? txtPersonne.getText().trim() : "";
        String montantStr = txtMontant.getText() != null ? txtMontant.getText().trim() : "";
        String date = txtDate.getText() != null ? txtDate.getText().trim() : "";

        if (personne.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Personne obligatoire.");
            return;
        }
        if (montantStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Montant obligatoire.");
            return;
        }
        double montant;
        try {
            montant = Double.parseDouble(montantStr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Montant invalide.");
            return;
        }
        if (montant <= 0) {
            JOptionPane.showMessageDialog(this, "Le montant doit être > 0.");
            return;
        }
        if (date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Date obligatoire.");
            return;
        }

        // Ici plus tard:
        // empruntService.creer(...) ou modifier(...)
        // + règles service: sécurité boutique, transitions statut, etc.
        JOptionPane.showMessageDialog(this,
                (editId == null ? "Création (à brancher service)" : "Modification (à brancher service)"));

        saved = true;
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