package com.maven.repairshop.ui.pages;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.maven.repairshop.ui.session.SessionContext;

public class DashboardPanel extends JPanel {

    private final SessionContext session;

    private JLabel lblReparations;
    private JLabel lblClients;
    private JLabel lblCaisse;
    private JLabel lblEmprunts;

    public DashboardPanel(SessionContext session) {
        this.session = session;
        initUi();
        // plus tard: refreshStats();
    }

    private void initUi() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Dashboard", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));

        grid.add(card("Mes réparations", lblReparations = new JLabel("—")));
        grid.add(card("Mes clients", lblClients = new JLabel("—")));
        grid.add(card("Ma caisse", lblCaisse = new JLabel("—")));
        grid.add(card("Emprunts / Prêts", lblEmprunts = new JLabel("—")));

        add(grid, BorderLayout.CENTER);

        // IMPORTANT:
        // La navigation est gérée par MainFrame (sidebar + CardLayout).
        // Ici on met des boutons "placeholder" (désactivés) pour éviter incohérences.
        JPanel bottom = new JPanel();
        JButton btnReps = new JButton("Voir réparations");
        JButton btnClients = new JButton("Voir clients");
        JButton btnCaisse = new JButton("Voir caisse");
        JButton btnEmprunts = new JButton("Voir emprunts");

        btnReps.setEnabled(false);
        btnClients.setEnabled(false);
        btnCaisse.setEnabled(false);
        btnEmprunts.setEnabled(false);

        bottom.add(btnReps);
        bottom.add(btnClients);
        bottom.add(btnCaisse);
        bottom.add(btnEmprunts);

        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel card(String title, JLabel value) {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel t = new JLabel(title);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 13f));

        value.setHorizontalAlignment(SwingConstants.CENTER);
        value.setFont(value.getFont().deriveFont(Font.BOLD, 22f));

        p.add(t, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }
}