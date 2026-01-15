package com.maven.repairshop.ui.pages;

import com.maven.repairshop.model.Boutique;
import com.maven.repairshop.model.Emprunt;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.model.enums.TypeEmprunt;
import com.maven.repairshop.service.CaisseService;
import com.maven.repairshop.service.EmpruntService;
import com.maven.repairshop.service.exceptions.BusinessException;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.ServiceRegistry;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CaissePanel extends JPanel {

    private final SessionContext session;

    private final CaisseService caisseService = ServiceRegistry.get().caisse();
    private final EmpruntService empruntService = ServiceRegistry.get().emprunts();

    private JTextField txtFrom; // YYYY-MM-DD
    private JTextField txtTo;   // YYYY-MM-DD

    private JLabel lblEntrees;
    private JLabel lblSorties;
    private JLabel lblSolde;

    private JLabel lblPaiements;
    private JLabel lblEmprunts;
    private JLabel lblPrets;

    private JLabel lblInfoRole;

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public CaissePanel(SessionContext session) {
        this.session = session;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(14, 14, 14, 14));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildKpis(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        initDefaultPeriod();
        refresh();
    }

    private JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout(10, 10));

        JLabel title = new JLabel("Caisse");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        txtFrom = new JTextField(10);
        txtTo = new JTextField(10);
        txtFrom.setToolTipText("YYYY-MM-DD");
        txtTo.setToolTipText("YYYY-MM-DD");

        JButton btnToday = new JButton("Aujourd’hui");
        JButton btn7 = new JButton("7 jours");
        JButton btn30 = new JButton("30 jours");
        JButton btnApply = new JButton("Appliquer");
        JButton btnRefresh = new JButton("Actualiser");

        btnToday.addActionListener(e -> setPeriodDays(0));
        btn7.addActionListener(e -> setPeriodDays(7));
        btn30.addActionListener(e -> setPeriodDays(30));
        btnApply.addActionListener(e -> refresh());
        btnRefresh.addActionListener(e -> refresh());

        filters.add(new JLabel("Du:"));
        filters.add(txtFrom);
        filters.add(new JLabel("Au:"));
        filters.add(txtTo);

        filters.add(btnToday);
        filters.add(btn7);
        filters.add(btn30);
        filters.add(btnApply);
        filters.add(btnRefresh);

        p.add(title, BorderLayout.WEST);
        p.add(filters, BorderLayout.EAST);

        return p;
    }

    private JComponent buildKpis() {
        JPanel center = new JPanel(new BorderLayout(12, 12));

        // KPI (Entrées / Sorties / Solde)
        JPanel kpisTop = new JPanel(new GridLayout(1, 3, 12, 12));
        lblEntrees = kpiValue("-");
        lblSorties = kpiValue("-");
        lblSolde = kpiValue("-");

        kpisTop.add(kpiCard("Entrées", lblEntrees, "Paiements + Emprunts"));
        kpisTop.add(kpiCard("Sorties", lblSorties, "Prêts"));
        kpisTop.add(kpiCard("Solde", lblSolde, "Entrées - Sorties"));

        // Détails (Paiements / Emprunts / Prêts)
        JPanel kpisBottom = new JPanel(new GridLayout(1, 3, 12, 12));
        lblPaiements = kpiValue("-");
        lblEmprunts = kpiValue("-");
        lblPrets = kpiValue("-");

        kpisBottom.add(kpiCard("Paiements (réparations)", lblPaiements, "CaisseService"));
        kpisBottom.add(kpiCard("Emprunts (tu reçois)", lblEmprunts, "Type: EMPRUNT"));
        kpisBottom.add(kpiCard("Prêts (tu donnes)", lblPrets, "Type: PRET"));

        lblInfoRole = new JLabel();
        lblInfoRole.setForeground(new Color(90, 90, 90));

        JPanel wrap = new JPanel(new BorderLayout(12, 12));
        wrap.add(kpisTop, BorderLayout.NORTH);
        wrap.add(kpisBottom, BorderLayout.CENTER);
        wrap.add(lblInfoRole, BorderLayout.SOUTH);

        center.add(wrap, BorderLayout.NORTH);

        return center;
    }

    private JComponent buildFooter() {
        JPanel p = new JPanel(new BorderLayout());

        JLabel hint = new JLabel(
                "<html><b>Rappel:</b> la caisse (paiements) vient du backend. Les emprunts/prêts sont calculés côté UI via EmpruntService.</html>"
        );
        hint.setForeground(new Color(90, 90, 90));

        p.add(hint, BorderLayout.WEST);
        return p;
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

    private void initDefaultPeriod() {
        // défaut: 30 derniers jours
        setPeriodDays(30);
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
            Long userId = session.getCurrentUser().getId();
            LocalDateTime from = parseFrom(txtFrom.getText().trim());
            LocalDateTime to = parseTo(txtTo.getText().trim());

            if (from.isAfter(to)) {
                UiDialogs.error(this, "Période invalide : la date début est après la date fin.");
                return;
            }

            if (session.isReparateur()) {
                Long reparateurId = session.getCurrentUser().getId();

                double paiements = caisseService.caisseReparateur(reparateurId, from, to, userId);

                // emprunts/prêts filtrés sur la période
                List<Emprunt> emprunts = empruntService.lister(reparateurId, userId);

                double totalEmprunts = 0d; // Type EMPRUNT
                double totalPrets = 0d;    // Type PRET

                for (Emprunt e : emprunts) {
                    if (e.getDateEmprunt() == null) continue;
                    if (e.getDateEmprunt().isBefore(from) || e.getDateEmprunt().isAfter(to)) continue;

                    if (e.getType() == TypeEmprunt.EMPRUNT) totalEmprunts += e.getMontant();
                    if (e.getType() == TypeEmprunt.PRET) totalPrets += e.getMontant();
                }

                double entrees = paiements + totalEmprunts;
                double sorties = totalPrets;
                double solde = entrees - sorties;

                setMoney(lblPaiements, paiements);
                setMoney(lblEmprunts, totalEmprunts);
                setMoney(lblPrets, totalPrets);

                setMoney(lblEntrees, entrees);
                setMoney(lblSorties, sorties);
                setMoney(lblSolde, solde);

                lblInfoRole.setText("Mode Réparateur : paiements + emprunts/prêts (filtrés sur la période).");

            } else {
                // Propriétaire: caisse boutique (paiements) only (backend dispo)
                Boutique b = session.getCurrentUser().getBoutique();
                if (b == null || b.getId() == null) {
                    UiDialogs.error(this, "Boutique introuvable pour ce propriétaire.");
                    return;
                }

                double paiementsBoutique = caisseService.caisseBoutique(b.getId(), from, to, userId);

                setMoney(lblPaiements, paiementsBoutique);
                setMoney(lblEmprunts, 0d);
                setMoney(lblPrets, 0d);

                // Entrées = paiements (car emprunts boutique non dispo dans stab)
                setMoney(lblEntrees, paiementsBoutique);
                setMoney(lblSorties, 0d);
                setMoney(lblSolde, paiementsBoutique);

                lblInfoRole.setText("Mode Propriétaire : caisse boutique = paiements (emprunts/prêts boutique non disponibles dans cette base).");
            }

        } catch (BusinessException ex) {
            UiDialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur caisse : " + ex.getMessage());
        }
    }

    private LocalDateTime parseFrom(String s) {
        if (s == null || s.isEmpty()) {
            return LocalDate.now().minusDays(30).atStartOfDay();
        }
        LocalDate d = LocalDate.parse(s, DATE);
        return d.atStartOfDay();
    }

    private LocalDateTime parseTo(String s) {
        if (s == null || s.isEmpty()) {
            return LocalDate.now().atTime(23, 59, 59);
        }
        LocalDate d = LocalDate.parse(s, DATE);
        return d.atTime(23, 59, 59);
    }

    private void setMoney(JLabel label, double value) {
        label.setText(String.format("%.2f", value));
    }
}