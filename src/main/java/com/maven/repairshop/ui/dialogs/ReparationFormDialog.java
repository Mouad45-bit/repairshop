package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.maven.repairshop.ui.session.SessionContext;

public class ReparationFormDialog extends JDialog {

    private final SessionContext session;

    private boolean saved = false;
    private Long editId = null;

    // Client
    private JTextField txtClientNom;
    private JTextField txtClientTel;

    // Appareils
    private JTable tblAppareils;
    private DefaultTableModel mdlAppareils;

    // Causes
    private JTable tblCauses;
    private DefaultTableModel mdlCauses;

    // Paiement initial (avance)
    private JTextField txtAvance;
    private JComboBox<String> cbTypePaiement;

    // Statut (en edit)
    private JComboBox<String> cbStatut;

    public ReparationFormDialog(Window owner, SessionContext session) {
        super(owner, "Réparation", ModalityType.APPLICATION_MODAL);
        this.session = session;
        setSize(820, 560);
        setLocationRelativeTo(owner);
        initUi();
    }

    /** Mode édition (UI-only). Plus tard on chargera depuis service par ID. */
    public void setModeEdit(Long reparationId) {
        this.editId = reparationId;
        setTitle("Modifier réparation #" + reparationId);
        cbStatut.setEnabled(true);
        // plus tard: loadFromService(reparationId)
    }

    public boolean isSaved() {
        return saved;
    }

    private void initUi() {
        setLayout(new BorderLayout());

        // ===== Tabs =====
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Client", buildClientTab());
        tabs.addTab("Appareil(s)", buildAppareilsTab());
        tabs.addTab("Cause(s)", buildCausesTab());
        tabs.addTab("Paiement", buildPaiementTab());

        add(tabs, BorderLayout.CENTER);

        // ===== Bottom buttons =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        cbStatut = new JComboBox<>(new String[] {
                "ENREGISTREE", "EN_COURS", "EN_ATTENTE_PIECES", "TERMINEE", "LIVREE", "ANNULEE"
        });
        cbStatut.setEnabled(false); // activé seulement en edit
        bottom.add(new JLabel("Statut:"));
        bottom.add(cbStatut);

        JButton btnCancel = new JButton("Annuler");
        JButton btnSave = new JButton("Enregistrer");
        bottom.add(btnCancel);
        bottom.add(btnSave);

        add(bottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());
    }

    private JPanel buildClientTab() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        txtClientNom = new JTextField();
        txtClientTel = new JTextField();

        p.add(row("Nom client*", txtClientNom));
        p.add(row("Téléphone", txtClientTel));

        JLabel hint = new JLabel("Plus tard: sélection client existant + création client.");
        hint.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(hint);

        return p;
    }

    private JPanel buildAppareilsTab() {
        JPanel p = new JPanel(new BorderLayout());

        mdlAppareils = new DefaultTableModel(new Object[] {"Type", "IMEI", "Remarque"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return true; }
        };
        tblAppareils = new JTable(mdlAppareils);

        p.add(new JScrollPane(tblAppareils), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("+ Ajouter appareil");
        JButton btnRemove = new JButton("Supprimer");
        actions.add(btnAdd);
        actions.add(btnRemove);
        p.add(actions, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> mdlAppareils.addRow(new Object[] {"Téléphone", "", ""}));
        btnRemove.addActionListener(e -> removeSelectedRow(tblAppareils, mdlAppareils));

        // 1 ligne par défaut
        if (mdlAppareils.getRowCount() == 0) {
            mdlAppareils.addRow(new Object[] {"Téléphone", "", ""});
        }

        return p;
    }

    private JPanel buildCausesTab() {
        JPanel p = new JPanel(new BorderLayout());

        mdlCauses = new DefaultTableModel(new Object[] {"Type cause", "Description", "Coût avance", "Coût restant"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return true; }
        };
        tblCauses = new JTable(mdlCauses);

        p.add(new JScrollPane(tblCauses), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("+ Ajouter cause");
        JButton btnRemove = new JButton("Supprimer");
        actions.add(btnAdd);
        actions.add(btnRemove);
        p.add(actions, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> mdlCauses.addRow(new Object[] {"Diagnostic", "", "0", "0"}));
        btnRemove.addActionListener(e -> removeSelectedRow(tblCauses, mdlCauses));

        // 1 ligne par défaut
        if (mdlCauses.getRowCount() == 0) {
            mdlCauses.addRow(new Object[] {"Diagnostic", "", "0", "0"});
        }

        return p;
    }

    private JPanel buildPaiementTab() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        txtAvance = new JTextField("0");
        cbTypePaiement = new JComboBox<>(new String[] { "ESPECES", "CARTE", "AUTRE" });

        p.add(row("Avance (DH)", txtAvance));
        p.add(row("Type paiement", cbTypePaiement));

        JLabel hint = new JLabel("Règle métier: avance optionnelle à la réception, le reste à la livraison.");
        hint.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(hint);

        return p;
    }

    private void onSave() {
        // ===== Validations UI minimales (la vraie règle métier sera dans Service) =====
        String nom = txtClientNom.getText() != null ? txtClientNom.getText().trim() : "";
        if (nom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nom client obligatoire.");
            return;
        }
        if (mdlAppareils.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Ajoute au moins 1 appareil.");
            return;
        }

        // Ici plus tard: appel ReparationService.creerReparation / modifierReparation
        // + PaiementService si avance > 0

        JOptionPane.showMessageDialog(this,
                (editId == null ? "Création réparation (à brancher service)" : "Modification réparation (à brancher service)"));

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

    private void removeSelectedRow(JTable table, DefaultTableModel mdl) {
        int row = table.getSelectedRow();
        if (row >= 0) mdl.removeRow(row);
    }
}