package com.maven.repairshop.ui.pages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.maven.repairshop.model.Emprunt;
import com.maven.repairshop.model.enums.TypeEmprunt;
import com.maven.repairshop.ui.controllers.ControllerRegistry;
import com.maven.repairshop.ui.controllers.EmpruntController;
import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.session.SessionContext;

public class CaissePanel extends JPanel {

    private final SessionContext session;

    // controller via registry (UI -> ServiceRegistry -> backend)
    private final EmpruntController empruntCtrl = ControllerRegistry.get().emprunts();

    private JTextField txtDateDebut;
    private JTextField txtDateFin;

    private JTable table;
    private DefaultTableModel model;

    private JLabel lblEntrees;
    private JLabel lblSorties;
    private JLabel lblSolde;

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public CaissePanel(SessionContext session) {
        this.session = session;
        initUi();
        refresh();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        // ===== TOP : filtres =====
        JPanel top = new JPanel(new BorderLayout());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtDateDebut = new JTextField(10); // YYYY-MM-DD
        txtDateFin = new JTextField(10);

        JButton btnApply = new JButton("Appliquer");
        JButton btnReset = new JButton("Réinitialiser");

        left.add(new JLabel("Date début:"));
        left.add(txtDateDebut);
        left.add(new JLabel("Date fin:"));
        left.add(txtDateFin);
        left.add(btnApply);
        left.add(btnReset);

        top.add(left, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // ===== TABLE : mouvements =====
        model = new DefaultTableModel(
                new Object[] { "Date", "Type", "Catégorie", "Description", "Montant" }, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== BOTTOM : totaux =====
        JPanel bottom = new JPanel(new BorderLayout());

        JPanel totals = new JPanel(new GridLayout(1, 3, 12, 12));
        lblEntrees = new JLabel("Total entrées: —");
        lblSorties = new JLabel("Total sorties: —");
        lblSolde = new JLabel("Solde: —");

        totals.add(lblEntrees);
        totals.add(lblSorties);
        totals.add(lblSolde);

        bottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottom.add(totals, BorderLayout.CENTER);

        add(bottom, BorderLayout.SOUTH);

        // ===== EVENTS =====
        btnApply.addActionListener(e -> refresh());

        btnReset.addActionListener(e -> {
            txtDateDebut.setText("");
            txtDateFin.setText("");
            refresh();
        });

        txtDateDebut.addActionListener(e -> refresh());
        txtDateFin.addActionListener(e -> refresh());
    }

    private void refresh() {
        Long reparateurId = session.getReparateurId();
        if (reparateurId == null) {
            UiDialogs.warn(this, "Cette page est réservée au réparateur (session invalide).");
            return;
        }

        LocalDate from = parseDateOrNull(txtDateDebut.getText(), "Date début");
        if (from == INVALID_DATE) return;

        LocalDate to = parseDateOrNull(txtDateFin.getText(), "Date fin");
        if (to == INVALID_DATE) return;

        // Optional: si from > to => warning + stop
        if (from != null && to != null && from.isAfter(to)) {
            UiDialogs.warn(this, "Période invalide: la date début est après la date fin.");
            return;
        }

        try {
            // On récupère la liste via le controller
            empruntCtrl.lister(this, reparateurId, list -> fillFromEmprunts(list, from, to));
        } catch (Exception ex) {
            UiDialogs.handle(this, ex);
        }
    }

    private void fillFromEmprunts(List<Emprunt> list, LocalDate from, LocalDate to) {
        model.setRowCount(0);

        double entrees = 0;
        double sorties = 0;

        for (Emprunt e : list) {
            LocalDateTime dt = e.getDateEmprunt();
            LocalDate d = (dt != null) ? dt.toLocalDate() : null;

            if (d != null) {
                if (from != null && d.isBefore(from)) continue;
                if (to != null && d.isAfter(to)) continue;
            }

            boolean isEntree = (e.getType() == TypeEmprunt.EMPRUNT); // tu reçois de l'argent
            String type = isEntree ? "ENTREE" : "SORTIE";
            String categorie = e.getType() != null ? e.getType().name() : "—";

            String dateStr = (dt != null) ? dt.format(DT) : "";
            String desc = (isEntree ? "Emprunt de " : "Prêt à ") + safe(e.getNomPersonne());
            if (!safe(e.getMotif()).isEmpty()) desc += " — " + safe(e.getMotif());

            double montant = e.getMontant();
            double abs = Math.abs(montant);

            String montantStr = (isEntree ? "+" : "-") + formatDh(abs);

            model.addRow(new Object[] { dateStr, type, categorie, desc, montantStr });

            // Totaux: on cumule en positif
            if (isEntree) entrees += abs;
            else sorties += abs;
        }

        double solde = entrees - sorties;

        lblEntrees.setText("Total entrées: " + formatDh(entrees));
        lblSorties.setText("Total sorties: " + formatDh(sorties));
        lblSolde.setText("Solde: " + formatDh(solde));
    }

    // Trick: on renvoie un marqueur spécial pour dire "date invalide"
    private static final LocalDate INVALID_DATE = LocalDate.of(1900, 1, 1);

    private LocalDate parseDateOrNull(String s, String fieldName) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        try {
            return LocalDate.parse(t); // ISO yyyy-MM-dd
        } catch (Exception ex) {
            UiDialogs.warn(this, fieldName + " invalide: " + t + " (format attendu: YYYY-MM-DD)");
            return INVALID_DATE;
        }
    }

    private String formatDh(double v) {
        if (v == (long) v) return ((long) v) + " DH";
        return String.format("%.2f DH", v);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}