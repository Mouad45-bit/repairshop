package com.maven.repairshop.ui.dialogs;

import com.maven.repairshop.model.Appareil;
import com.maven.repairshop.model.Cause;
import com.maven.repairshop.model.Paiement;
import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.model.enums.StatutReparation;
import com.maven.repairshop.model.enums.TypePaiement;
import com.maven.repairshop.service.exceptions.BusinessException;
import com.maven.repairshop.ui.controllers.ReparationController;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReparationDetailDialog extends JDialog {

    private final SessionContext session;
    private final ReparationController reparationCtrl = new ReparationController();

    private final Long reparationId;
    private Reparation reparation;

    private JLabel lblCode;
    private JLabel lblClient;
    private JLabel lblCreatedAt;
    private JLabel lblStatut;
    private JLabel lblLastStatut;

    private JComboBox<StatutReparation> cbStatut;
    private JButton btnSaveStatut;

    private JTable tableAppareils;
    private DefaultTableModel modelAppareils;

    private JTable tableCauses;
    private DefaultTableModel modelCauses;

    private JTable tablePaiements;
    private DefaultTableModel modelPaiements;

    private JLabel lblTotalAvance;
    private JLabel lblTotalReste;
    private JLabel lblTotalGlobal;

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReparationDetailDialog(Window owner, SessionContext session, Long reparationId) {
        super(owner, "Détails réparation", ModalityType.APPLICATION_MODAL);
        this.session = session;
        this.reparationId = reparationId;

        setSize(980, 680);
        setLocationRelativeTo(owner);

        setContentPane(buildUi());
        reload();
    }

    private JComponent buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Détails réparation");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        root.add(title, BorderLayout.NORTH);

        JPanel top = new JPanel(new BorderLayout(12, 12));
        top.add(buildSummary(), BorderLayout.CENTER);
        top.add(buildStatusBox(), BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Appareils", buildAppareilsTab());
        tabs.addTab("Causes", buildCausesTab());
        tabs.addTab("Paiements", buildPaiementsTab());
        tabs.addTab("Objets", buildNotReadyTab("Objets",
                "Pas encore disponible dans cette base backend (module à venir)."));

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.add(top, BorderLayout.NORTH);
        center.add(tabs, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);
        root.add(buildBottomActions(), BorderLayout.SOUTH);

        return root;
    }

    private JComponent buildSummary() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(BorderFactory.createTitledBorder("Résumé"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 10, 6, 10);
        c.anchor = GridBagConstraints.WEST;

        lblCode = new JLabel("-");
        lblClient = new JLabel("-");
        lblCreatedAt = new JLabel("-");
        lblStatut = new JLabel("-");
        lblLastStatut = new JLabel("-");

        int row = 0;
        addRow(card, c, row++, "Code", lblCode);
        addRow(card, c, row++, "Client", lblClient);
        addRow(card, c, row++, "Créée le", lblCreatedAt);
        addRow(card, c, row++, "Statut", lblStatut);
        addRow(card, c, row++, "Dernier statut", lblLastStatut);

        return card;
    }

    private void addRow(JPanel card, GridBagConstraints c, int row, String label, JLabel value) {
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        JLabel l = new JLabel(label + " :");
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        card.add(l, c);

        c.gridx = 1; c.gridy = row; c.weightx = 1;
        value.setForeground(new Color(60, 60, 60));
        card.add(value, c);
    }

    private JComponent buildStatusBox() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createTitledBorder("Statut"));

        cbStatut = new JComboBox<>(StatutReparation.values());
        btnSaveStatut = new JButton("Enregistrer statut");

        btnSaveStatut.addActionListener(e -> saveStatut());

        JPanel inner = new JPanel(new GridLayout(2, 1, 8, 8));
        inner.add(cbStatut);
        inner.add(btnSaveStatut);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildAppareilsTab() {
        modelAppareils = new DefaultTableModel(
                new Object[]{"IMEI", "Type", "Description", "Nb causes"}, 0
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };

        tableAppareils = new JTable(modelAppareils);
        tableAppareils.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        return new JScrollPane(tableAppareils);
    }

    private JComponent buildCausesTab() {
        modelCauses = new DefaultTableModel(
                new Object[]{"Appareil (IMEI)", "Type cause", "Description", "Coût avance", "Coût restant"}, 0
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };

        tableCauses = new JTable(modelCauses);
        tableCauses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.add(new JScrollPane(tableCauses), BorderLayout.CENTER);
        p.add(buildHint("Lecture seule : la création/suppression de causes arrive quand le service backend existe."),
                BorderLayout.SOUTH);
        return p;
    }

    private JComponent buildPaiementsTab() {
        modelPaiements = new DefaultTableModel(
                new Object[]{"Date", "Type", "Montant"}, 0
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };

        tablePaiements = new JTable(modelPaiements);
        tablePaiements.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        lblTotalAvance = new JLabel("-");
        lblTotalReste = new JLabel("-");
        lblTotalGlobal = new JLabel("-");

        JPanel totals = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        totals.add(bold("Total avance:"));
        totals.add(lblTotalAvance);
        totals.add(bold("Total reste:"));
        totals.add(lblTotalReste);
        totals.add(bold("Total payé:"));
        totals.add(lblTotalGlobal);

        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.add(totals, BorderLayout.NORTH);
        p.add(new JScrollPane(tablePaiements), BorderLayout.CENTER);
        p.add(buildHint("Lecture seule : l’ajout de paiements arrive dans le module Paiements/Caisse."),
                BorderLayout.SOUTH);
        return p;
    }

    private JLabel bold(String s) {
        JLabel l = new JLabel(s);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        return l;
    }

    private JComponent buildHint(String text) {
        JLabel hint = new JLabel(text);
        hint.setForeground(new Color(90, 90, 90));
        JPanel p = new JPanel(new BorderLayout());
        p.add(hint, BorderLayout.WEST);
        return p;
    }

    private JComponent buildNotReadyTab(String title, String message) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel t = new JLabel(title);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 18f));

        JLabel m = new JLabel("<html><div style='width:520px'>" + message + "</div></html>");
        m.setForeground(new Color(90, 90, 90));

        p.add(t, BorderLayout.NORTH);
        p.add(m, BorderLayout.CENTER);
        return p;
    }

    private JComponent buildBottomActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnClose = new JButton("Fermer");
        btnClose.addActionListener(e -> dispose());
        p.add(btnClose);
        return p;
    }

    private void reload() {
        try {
            this.reparation = reparationCtrl.trouverParId(reparationId);
            if (this.reparation == null) {
                UiDialogs.error(this, "Réparation introuvable.");
                dispose();
                return;
            }
            fillSummary();
            fillAppareils();
            fillCauses();
            fillPaiements();

            if (reparation.getStatut() != null) cbStatut.setSelectedItem(reparation.getStatut());

        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur chargement détails : " + ex.getMessage());
        }
    }

    private void fillSummary() {
        lblCode.setText(orDash(reparation.getCodeUnique()));
        lblClient.setText(reparation.getClient() != null ? orDash(reparation.getClient().getNom()) : "-");
        lblCreatedAt.setText(reparation.getDateCreation() != null ? reparation.getDateCreation().format(DT) : "-");
        lblStatut.setText(reparation.getStatut() != null ? reparation.getStatut().name() : "-");
        lblLastStatut.setText(reparation.getDateDernierStatut() != null ? reparation.getDateDernierStatut().format(DT) : "-");
    }

    private void fillAppareils() {
        modelAppareils.setRowCount(0);
        List<Appareil> apps = safeList(reparation.getAppareils());
        for (Appareil a : apps) {
            int nb = (a.getCauses() != null) ? a.getCauses().size() : 0;
            modelAppareils.addRow(new Object[]{
                    orDash(a.getImei()),
                    orDash(a.getTypeAppareil()),
                    orDash(a.getDescription()),
                    nb
            });
        }
    }

    private void fillCauses() {
        modelCauses.setRowCount(0);

        List<Appareil> apps = safeList(reparation.getAppareils());
        for (Appareil a : apps) {
            List<Cause> causes = safeList(a.getCauses());
            for (Cause c : causes) {
                modelCauses.addRow(new Object[]{
                        orDash(a.getImei()),
                        orDash(c.getTypeCause()),
                        orDash(c.getDescription()),
                        String.format("%.2f", c.getCoutAvance()),
                        String.format("%.2f", c.getCoutRestant())
                });
            }
        }
    }

    private void fillPaiements() {
        modelPaiements.setRowCount(0);

        double totalAvance = 0;
        double totalReste = 0;

        List<Paiement> pays = safeList(reparation.getPaiements());
        for (Paiement p : pays) {
            String date = (p.getDatePaiement() != null) ? p.getDatePaiement().format(DT) : "-";
            String type = (p.getTypePaiement() != null) ? p.getTypePaiement().name() : "-";
            String montant = String.format("%.2f", p.getMontant());

            if (p.getTypePaiement() == TypePaiement.AVANCE) totalAvance += p.getMontant();
            if (p.getTypePaiement() == TypePaiement.RESTE) totalReste += p.getMontant();

            modelPaiements.addRow(new Object[]{date, type, montant});
        }

        lblTotalAvance.setText(String.format("%.2f", totalAvance));
        lblTotalReste.setText(String.format("%.2f", totalReste));
        lblTotalGlobal.setText(String.format("%.2f", (totalAvance + totalReste)));
    }

    private void saveStatut() {
        btnSaveStatut.setEnabled(false);
        try {
            StatutReparation s = (StatutReparation) cbStatut.getSelectedItem();
            if (s == null) {
                UiDialogs.error(this, "Statut invalide.");
                return;
            }
            reparationCtrl.changerStatut(reparationId, s);
            UiDialogs.success(this, "Statut mis à jour.");
            reload();

        } catch (BusinessException ex) {
            UiDialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur: " + ex.getMessage());
        } finally {
            btnSaveStatut.setEnabled(true);
        }
    }

    private static String orDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }
}