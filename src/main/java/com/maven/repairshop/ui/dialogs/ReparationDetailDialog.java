package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.maven.repairshop.ui.session.SessionContext;

public class ReparationDetailDialog extends JDialog {

    private final SessionContext session;
    private final Long reparationId;

    // UI fields
    private JLabel lblCode;
    private JLabel lblStatut;
    private JLabel lblDate;
    private JLabel lblClient;

    private JTable tblAppareils;
    private DefaultTableModel mdlAppareils;

    private JTable tblPaiements;
    private DefaultTableModel mdlPaiements;

    private JLabel lblTotal;
    private JLabel lblDejaPaye;
    private JLabel lblReste;

    private JTextField txtMontant;
    private JComboBox<String> cbTypePaiement;

    public ReparationDetailDialog(Window owner, SessionContext session, Long reparationId) {
        super(owner, "Détail réparation #" + reparationId, ModalityType.APPLICATION_MODAL);
        this.session = session;
        this.reparationId = reparationId;

        setSize(900, 600);
        setLocationRelativeTo(owner);
        initUi();

        // plus tard: loadFromService(reparationId)
        seedFakeDetails();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        // ===== Header infos =====
        JPanel header = new JPanel(new GridLayout(2, 2, 8, 8));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblCode = new JLabel("Code: —");
        lblStatut = new JLabel("Statut: —");
        lblDate = new JLabel("Date: —");
        lblClient = new JLabel("Client: —");

        header.add(lblCode);
        header.add(lblStatut);
        header.add(lblDate);
        header.add(lblClient);

        add(header, BorderLayout.NORTH);

        // ===== Center: appareils + paiements =====
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.55);

        // Appareils
        mdlAppareils = new DefaultTableModel(new Object[] {"Type", "IMEI", "Remarque"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblAppareils = new JTable(mdlAppareils);
        JPanel appareilsBox = new JPanel(new BorderLayout());
        appareilsBox.add(new JLabel("Appareil(s)"), BorderLayout.NORTH);
        appareilsBox.add(new JScrollPane(tblAppareils), BorderLayout.CENTER);

        // Paiements
        mdlPaiements = new DefaultTableModel(new Object[] {"Date", "Montant", "Type"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPaiements = new JTable(mdlPaiements);

        JPanel paiementsBox = new JPanel(new BorderLayout());
        paiementsBox.add(new JLabel("Paiement(s)"), BorderLayout.NORTH);
        paiementsBox.add(new JScrollPane(tblPaiements), BorderLayout.CENTER);

        split.setTopComponent(appareilsBox);
        split.setBottomComponent(paiementsBox);

        add(split, BorderLayout.CENTER);

        // ===== Bottom: résumé + encaissement =====
        JPanel bottom = new JPanel(new BorderLayout());

        JPanel resume = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblTotal = new JLabel("Total: —");
        lblDejaPaye = new JLabel("Déjà payé: —");
        lblReste = new JLabel("Reste: —");
        resume.add(lblTotal);
        resume.add(new JLabel(" | "));
        resume.add(lblDejaPaye);
        resume.add(new JLabel(" | "));
        resume.add(lblReste);

        bottom.add(resume, BorderLayout.NORTH);

        JPanel payRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        txtMontant = new JTextField(10);
        cbTypePaiement = new JComboBox<>(new String[] { "AVANCE", "RESTE" });
        JButton btnEncaisser = new JButton("Encaisser");
        JButton btnClose = new JButton("Fermer");

        payRow.add(new JLabel("Montant:"));
        payRow.add(txtMontant);
        payRow.add(new JLabel("Type:"));
        payRow.add(cbTypePaiement);
        payRow.add(btnEncaisser);
        payRow.add(btnClose);

        bottom.add(payRow, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        btnClose.addActionListener(e -> dispose());

        btnEncaisser.addActionListener(e -> {
            String s = txtMontant.getText() != null ? txtMontant.getText().trim() : "";
            if (s.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Montant obligatoire.");
                return;
            }
            // plus tard: paiementService.enregistrerPaiement(reparationId, montant, type, session)
            JOptionPane.showMessageDialog(this, "Encaissement (à brancher service)");
        });
    }

    // ===== Données fake pour voir l'UI immédiatement =====
    private void seedFakeDetails() {
        lblCode.setText("Code: R-8F2A1");
        lblStatut.setText("Statut: EN_COURS");
        lblDate.setText("Date: 2026-01-12");
        lblClient.setText("Client: Sara B. (06xxxxxxxx)");

        mdlAppareils.setRowCount(0);
        mdlAppareils.addRow(new Object[] {"Téléphone", "356789012345678", "Écran cassé"});

        mdlPaiements.setRowCount(0);
        mdlPaiements.addRow(new Object[] {"2026-01-12", "100", "AVANCE"});

        lblTotal.setText("Total: 300 DH");
        lblDejaPaye.setText("Déjà payé: 100 DH");
        lblReste.setText("Reste: 200 DH");
    }
}