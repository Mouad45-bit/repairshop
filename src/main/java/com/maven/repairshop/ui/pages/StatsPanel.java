package com.maven.repairshop.ui.pages;

import com.maven.repairshop.model.Boutique;
import com.maven.repairshop.service.StatistiquesService;
import com.maven.repairshop.service.exceptions.BusinessException;
import com.maven.repairshop.service.impl.StatistiquesServiceImpl;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class StatsPanel extends JPanel {

    private final SessionContext session;
    private final StatistiquesService statsService = new StatistiquesServiceImpl();

    private JTextField txtFrom; // YYYY-MM-DD
    private JTextField txtTo;   // YYYY-MM-DD

    private JLabel lblNb;
    private JLabel lblCa;
    private JLabel lblTerminees;
    private JLabel lblEnCours;
    private JProgressBar barTerminees;

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public StatsPanel(SessionContext session) {
        this.session = session;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(14, 14, 14, 14));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        setPeriodDays(30);
        refresh();
    }

    private JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout(10, 10));

        JLabel title = new JLabel("Statistiques (boutique)");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        txtFrom = new JTextField(10);
        txtTo = new JTextField(10);
        txtFrom.setToolTipText("YYYY-MM-DD");
        txtTo.setToolTipText("YYYY-MM-DD");

        JButton btnToday = new JButton("Aujourd’hui");
        JButton btn7 = new JButton("7 jours");
        JButton btn30 = new JButton("30 jours");
        JButton btnApply = new JButton("Appliquer");

        btnToday.addActionListener(e -> setPeriodDays(0));
        btn7.addActionListener(e -> setPeriodDays(7));
        btn30.addActionListener(e -> setPeriodDays(30));
        btnApply.addActionListener(e -> refresh());

        right.add(new JLabel("Du:"));
        right.add(txtFrom);
        right.add(new JLabel("Au:"));
        right.add(txtTo);

        right.add(btnToday);
        right.add(btn7);
        right.add(btn30);
        right.add(btnApply);

        p.add(title, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);

        JLabel hint = new JLabel("Réservé au propriétaire. Mesure la période sur les dates de création (réparations) / paiement (CA).");
        hint.setForeground(new Color(90, 90, 90));

        JPanel wrap = new JPanel(new BorderLayout(0, 6));
        wrap.add(p, BorderLayout.CENTER);
        wrap.add(hint, BorderLayout.SOUTH);
        return wrap;
    }

    private JComponent buildContent() {
        JPanel root = new JPanel(new BorderLayout(12, 12));

        lblNb = kpiValue("-");
        lblCa = kpiValue("-");
        lblTerminees = kpiValue("-");
        lblEnCours = kpiValue("-");

        JPanel top = new JPanel(new GridLayout(1, 2, 12, 12));
        top.add(kpiCard("Nombre de réparations", lblNb, "Toutes réparations (période)"));
        top.add(kpiCard("Chiffre d'affaires", lblCa, "Somme des paiements (période)"));

        JPanel bottom = new JPanel(new BorderLayout(12, 12));
        JPanel counts = new JPanel(new GridLayout(1, 2, 12, 12));
        counts.add(kpiCard("Terminées / Livrées", lblTerminees, "TERMINEE + LIVREE"));
        counts.add(kpiCard("En cours", lblEnCours, "ENREGISTREE + EN_COURS + EN_ATTENTE_PIECES"));

        barTerminees = new JProgressBar(0, 100);
        barTerminees.setStringPainted(true);

        JPanel barCard = new JPanel(new BorderLayout(0, 6));
        barCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(12, 12, 12, 12)
        ));
        JLabel barTitle = new JLabel("Taux de réparations terminées");
        barTitle.setFont(barTitle.getFont().deriveFont(Font.BOLD, 14f));
        JLabel barHint = new JLabel("Pourcentage = terminées / (terminées + en cours)");
        barHint.setForeground(new Color(90, 90, 90));
        barCard.add(barTitle, BorderLayout.NORTH);
        barCard.add(barTerminees, BorderLayout.CENTER);
        barCard.add(barHint, BorderLayout.SOUTH);

        bottom.add(counts, BorderLayout.NORTH);
        bottom.add(barCard, BorderLayout.CENTER);

        root.add(top, BorderLayout.NORTH);
        root.add(bottom, BorderLayout.CENTER);

        return root;
    }

    private JPanel kpiCard(String title, JLabel value, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel t = new JLabel(title);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 14f));

        JLabel s = new JLabel(subtitle);
        s.setForeground(new Color(90, 90, 90));

        card.add(t, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(s, BorderLayout.SOUTH);
        return card;
    }

    private JLabel kpiValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 22f));
        return l;
    }

    private void setPeriodDays(int days) {
        LocalDate today = LocalDate.now();
        LocalDate from = (days == 0) ? today : today.minusDays(days);
        txtFrom.setText(from.format(DATE));
        txtTo.setText(today.format(DATE));
        refresh();
    }

    private void refresh() {
        try {
            if (!session.isProprietaire()) {
                UiDialogs.error(this, "Accès refusé: ce module est réservé au propriétaire.");
                return;
            }

            Boutique b = session.getCurrentUser().getBoutique();
            if (b == null || b.getId() == null) {
                UiDialogs.error(this, "Aucune boutique pour ce propriétaire.");
                return;
            }

            Long boutiqueId = b.getId();
            Long userId = session.getCurrentUser().getId();

            LocalDateTime from = parseFrom(txtFrom.getText().trim());
            LocalDateTime to = parseTo(txtTo.getText().trim());

            if (from.isAfter(to)) {
                UiDialogs.error(this, "Période invalide : date début > date fin.");
                return;
            }

            long nb = statsService.nbReparationsParPeriode(boutiqueId, from, to, userId);
            Map<String, Long> m = statsService.termineesVsEnCours(boutiqueId, from, to, userId);
            double ca = statsService.chiffreAffairesParPeriode(boutiqueId, from, to, userId);

            long terminees = m.getOrDefault("TERMINEES", 0L);
            long enCours = m.getOrDefault("EN_COURS", 0L);

            lblNb.setText(String.valueOf(nb));
            lblCa.setText(String.format("%.2f", ca));
            lblTerminees.setText(String.valueOf(terminees));
            lblEnCours.setText(String.valueOf(enCours));

            long denom = terminees + enCours;
            int pct = (denom == 0) ? 0 : (int) Math.round((terminees * 100.0) / denom);
            barTerminees.setValue(pct);
            barTerminees.setString(pct + " %");

        } catch (BusinessException ex) {
            UiDialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur stats : " + ex.getMessage());
        }
    }

    private LocalDateTime parseFrom(String s) {
        if (s == null || s.isEmpty()) return LocalDate.now().minusDays(30).atStartOfDay();
        return LocalDate.parse(s, DATE).atStartOfDay();
    }

    private LocalDateTime parseTo(String s) {
        if (s == null || s.isEmpty()) return LocalDate.now().atTime(23, 59, 59);
        return LocalDate.parse(s, DATE).atTime(23, 59, 59);
    }
}