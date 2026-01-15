package com.maven.repairshop.ui.dialogs;

import com.maven.repairshop.model.enums.TypeEmprunt;
import com.maven.repairshop.service.EmpruntService;
import com.maven.repairshop.service.exceptions.BusinessException;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.ServiceRegistry;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EmpruntDialog extends JDialog {

    private final SessionContext session;
    private final EmpruntService empruntService = ServiceRegistry.get().emprunts();

    private final Long targetReparateurId;
    private boolean saved = false;

    private JComboBox<TypeEmprunt> cbType;
    private JTextField txtPersonne;
    private JTextField txtMontant;
    private JTextArea txtMotif;

    private JButton btnSave;

    public EmpruntDialog(Window owner, SessionContext session, Long targetReparateurId) {
        super(owner, "Nouvel emprunt / prêt", ModalityType.APPLICATION_MODAL);
        this.session = session;
        this.targetReparateurId = targetReparateurId;

        setSize(560, 440);
        setLocationRelativeTo(owner);
        setContentPane(buildUi());
        getRootPane().setDefaultButton(btnSave);
    }

    public boolean isSaved() { return saved; }

    private JComponent buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Créer un emprunt / prêt");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 0, 12, 0));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 0, 6, 0);
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        cbType = new JComboBox<>(TypeEmprunt.values());
        txtPersonne = new JTextField();
        txtMontant = new JTextField();
        txtMotif = new JTextArea(5, 20);
        txtMotif.setLineWrap(true);
        txtMotif.setWrapStyleWord(true);

        int row = 0;
        c.gridy = row++; form.add(readonlyLine("Réparateur ID", String.valueOf(targetReparateurId)), c);
        c.gridy = row++; form.add(labeled("Type *", cbType), c);
        c.gridy = row++; form.add(labeled("Personne *", txtPersonne), c);
        c.gridy = row++; form.add(labeled("Montant *", txtMontant), c);
        c.gridy = row++; form.add(labeled("Motif (optionnel)", new JScrollPane(txtMotif)), c);

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
            TypeEmprunt type = (TypeEmprunt) cbType.getSelectedItem();
            String personne = txtPersonne.getText().trim();
            String montantTxt = txtMontant.getText().trim();
            String motif = txtMotif.getText().trim();

            if (type == null) {
                UiDialogs.error(this, "Type invalide.");
                return;
            }
            if (personne.isEmpty()) {
                UiDialogs.error(this, "La personne est obligatoire.");
                return;
            }

            double montant;
            try {
                montant = Double.parseDouble(montantTxt);
                if (montant <= 0) throw new NumberFormatException();
            } catch (NumberFormatException nfe) {
                UiDialogs.error(this, "Montant invalide (nombre > 0).");
                return;
            }

            Long userId = session.getCurrentUser().getId();

            empruntService.creer(
                    targetReparateurId,
                    type,
                    personne,
                    montant,
                    (motif.isEmpty() ? null : motif),
                    userId
            );

            saved = true;
            UiDialogs.success(this, "Emprunt créé.");
            dispose();

        } catch (BusinessException ex) {
            UiDialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur inattendue : " + ex.getMessage());
        } finally {
            btnSave.setEnabled(true);
        }
    }
}