package com.maven.repairshop.ui.pages;

import com.maven.repairshop.model.Boutique;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.service.CaisseService;
import com.maven.repairshop.service.exceptions.BusinessException;
import com.maven.repairshop.ui.controllers.ProprietaireController;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.ServiceRegistry;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AllCaissesPanel extends JPanel {

    private final SessionContext session;

    private final CaisseService caisseService = ServiceRegistry.get().caisse();
    private final ProprietaireController proprietaireCtrl = new ProprietaireController();

    private JTextField txtFrom; // YYYY-MM-DD
    private JTextField txtTo;   // YYYY-MM-DD

    private JLabel lblBoutique;
    private JLabel lblTotalBoutique;

    private JTable table;
    private DefaultTableModel model;

    private JButton btnDetails;

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public AllCaissesPanel(SessionContext session) {
        this.session = session;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(14, 14, 14, 14));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        setDefaultPeriod();
        refresh();
    }

    private JComponent buildHeader() {
        JPanel wrap = new JPanel(new BorderLayout(10, 10));

        JLabel title = new JLabel("Caisses (boutique)");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        lblBoutique = new JLabel("-");
        lblBoutique.setForeground(new Color(90, 90, 90));

        JPanel left = new JPanel(new BorderLayout(0, 6));
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(lblBoutique, BorderLayout.SOUTH);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        txtFrom = new JTextField(10);
        txtTo = new JTextField(10);
        txtFrom.setToolTipText("YYYY-MM-DD");
        txtTo.setToolTipText("YYYY-MM-DD");

        JButton btn7 = new JButton("7 jours");
        JButton btn30 = new JButton("30 jours");
        JButton btnApply = new JButton("Appliquer");
        JButton btnRefresh = new JButton("Actualiser");

        btn7.addActionListener(e -> setPeriodDays(7));
        btn30.addActionListener(e -> setPeriodDays(30));
        btnApply.addActionListener(e -> refresh());
        btnRefresh.addActionListener(e -> refresh());

        right.add(new JLabel("Du:"));
        right.add(txtFrom);
        right.add(new JLabel("Au:"));
        right.add(txtTo);

        right.add(btn7);
        right.add(btn30);
        right.add(btnApply);
        right.add(btnRefresh);

        JPanel kpi = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel kpiLabel = new JLabel("Total boutique (paiements):");
        kpiLabel.setFont(kpiLabel.getFont().deriveFont(Font.BOLD));
        lblTotalBoutique = new JLabel("-");
        lblTotalBoutique.setFont(lblTotalBoutique.getFont().deriveFont(Font.BOLD, 16f));
        kpi.add(kpiLabel);
        kpi.add(lblTotalBoutique);

        wrap.add(left, BorderLayout.WEST);
        wrap.add(right, BorderLayout.EAST);
        wrap.add(kpi, BorderLayout.SOUTH);

        return wrap;
    }

    private JComponent buildTable() {
        model = new DefaultTableModel(
                new Object[]{"ID", "Nom", "Login", "Caisse (paiements)", "% du total"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // cacher ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.getSelectionModel().addListSelectionListener(e -> btnDetails.setEnabled(table.getSelectedRow() >= 0));

        return new JScrollPane(table);
    }

    private JComponent buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        btnDetails = new JButton("Détails");
        btnDetails.setEnabled(false);
        btnDetails.addActionListener(e -> showDetails());

        p.add(btnDetails);

        JLabel hint = new JLabel("NB: Ces montants proviennent de CaisseService (paiements des réparations).");
        hint.setForeground(new Color(90, 90, 90));
        p.add(Box.createHorizontalStrut(16));
        p.add(hint);

        return p;
    }

    private void setDefaultPeriod() {
        setPeriodDays(30);
    }

    private void setPeriodDays(int days) {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(days);
        txtFrom.setText(from.format(DATE));
        txtTo.setText(today.format(DATE));
        refresh();
    }

    private void refresh() {
        try {
            model.setRowCount(0);

            if (!session.isProprietaire()) {
                UiDialogs.error(this, "Accès refusé: module réservé au propriétaire.");
                return;
            }

            Boutique b = session.getCurrentUser().getBoutique();
            if (b == null || b.getId() == null) {
                UiDialogs.error(this, "Aucune boutique associée à ce propriétaire.");
                return;
            }

            lblBoutique.setText("Boutique: " + safe(b.getNom()) + " (id=" + b.getId() + ")");

            Long userId = session.getCurrentUser().getId();
            Long boutiqueId = b.getId();

            LocalDateTime from = parseFrom(txtFrom.getText().trim());
            LocalDateTime to = parseTo(txtTo.getText().trim());
            if (from.isAfter(to)) {
                UiDialogs.error(this, "Période invalide : date début > date fin.");
                return;
            }

            double totalBoutique = caisseService.caisseBoutique(boutiqueId, from, to, userId);
            lblTotalBoutique.setText(String.format("%.2f", totalBoutique));

            List<Reparateur> reps = proprietaireCtrl.listerReparateurs(boutiqueId);

            for (Reparateur r : reps) {
                double caisseRep = caisseService.caisseReparateur(r.getId(), from, to, userId);
                String pct = (totalBoutique <= 0.000001) ? "-" : String.format("%.0f%%", (caisseRep * 100.0) / totalBoutique);

                model.addRow(new Object[]{
                        r.getId(),
                        safe(r.getNom()),
                        safe(r.getLogin()),
                        String.format("%.2f", caisseRep),
                        pct
                });
            }

        } catch (BusinessException ex) {
            UiDialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur caisses : " + ex.getMessage());
        }
    }

    private void showDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiDialogs.error(this, "Sélectionnez un réparateur.");
            return;
        }

        String nom = String.valueOf(model.getValueAt(row, 1));
        String login = String.valueOf(model.getValueAt(row, 2));
        String caisse = String.valueOf(model.getValueAt(row, 3));
        String pct = String.valueOf(model.getValueAt(row, 4));

        UiDialogs.success(this,
                "Caisse réparateur\n\n" +
                        "Nom: " + nom + "\n" +
                        "Login: " + login + "\n" +
                        "Période: " + txtFrom.getText().trim() + " → " + txtTo.getText().trim() + "\n\n" +
                        "Caisse (paiements): " + caisse + "\n" +
                        "Part du total: " + pct
        );
    }

    private LocalDateTime parseFrom(String s) {
        if (s == null || s.isEmpty()) return LocalDate.now().minusDays(30).atStartOfDay();
        LocalDate d = LocalDate.parse(s, DATE);
        return d.atStartOfDay();
    }

    private LocalDateTime parseTo(String s) {
        if (s == null || s.isEmpty()) return LocalDate.now().atTime(23, 59, 59);
        LocalDate d = LocalDate.parse(s, DATE);
        return d.atTime(23, 59, 59);
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }
}