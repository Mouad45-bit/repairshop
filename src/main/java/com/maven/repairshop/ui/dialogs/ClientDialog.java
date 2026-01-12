package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.*;

public class ClientDialog extends JDialog {

    private JTextField txtNom;
    private JTextField txtTel;
    private JTextField txtEmail;
    private JTextField txtVille;

    public ClientDialog(Window owner) {
        super(owner, "Client", ModalityType.APPLICATION_MODAL);
        setSize(420, 280);
        setLocationRelativeTo(owner);
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        txtNom = new JTextField();
        txtTel = new JTextField();
        txtEmail = new JTextField();
        txtVille = new JTextField();

        form.add(row("Nom*", txtNom));
        form.add(row("Téléphone", txtTel));
        form.add(row("Email", txtEmail));
        form.add(row("Ville", txtVille));

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Enregistrer");
        JButton btnCancel = new JButton("Annuler");
        bottom.add(btnCancel);
        bottom.add(btnSave);
        add(bottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Sauvegarde (à brancher service)");
            dispose();
        });
    }

    private JPanel row(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.add(new JLabel(label), BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return p;
    }
}