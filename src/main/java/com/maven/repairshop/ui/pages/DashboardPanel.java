package com.maven.repairshop.ui.pages;

import java.awt.BorderLayout;
import java.awt.GridLayout;

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
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Dashboard", SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));

        grid.add(card("Mes réparations", lblReparations = new JLabel("—")));
        grid.add(card("Mes clients", lblClients = new JLabel("—")));
        grid.add(card("Ma caisse", lblCaisse = new JLabel("—")));
        grid.add(card("Emprunts / Prêts", lblEmprunts = new JLabel("—")));

        add(grid, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.add(new JButton("Voir réparations"));
        bottom.add(new JButton("Voir clients"));
        bottom.add(new JButton("Voir caisse"));
        bottom.add(new JButton("Voir emprunts"));
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel card(String title, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(title), BorderLayout.NORTH);
        value.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(value, BorderLayout.CENTER);
        return p;
    }
}
