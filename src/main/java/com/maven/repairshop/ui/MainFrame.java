package com.maven.repairshop.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.ServiceRegistry;

import com.maven.repairshop.ui.pages.DashboardPanel;
import com.maven.repairshop.ui.pages.ReparationsPanel;
import com.maven.repairshop.ui.pages.ClientsPanel;
import com.maven.repairshop.ui.pages.CaissePanel;
import com.maven.repairshop.ui.pages.EmpruntsPanel;

import com.maven.repairshop.ui.dialogs.SuiviDialog;

/**
 * Fenêtre principale :
 * - Sidebar navigation
 * - Contenu central en CardLayout
 * - Affiche des pages (JPanel)
 */
public class MainFrame extends JFrame {

    private final SessionContext session;

    private CardLayout cardLayout;
    private JPanel contentPanel;

    // IDs des cartes
    private static final String CARD_DASHBOARD = "DASHBOARD";
    private static final String CARD_REPARATIONS = "REPARATIONS";
    private static final String CARD_CLIENTS = "CLIENTS";
    private static final String CARD_CAISSE = "CAISSE";
    private static final String CARD_EMPRUNTS = "EMPRUNTS";

    public MainFrame(SessionContext session) {
        this.session = session;

        setTitle("RepairShop - " + session.getRole() + " (" + session.getLogin() + ")");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 650));
        setLocationRelativeTo(null);

        // fermeture propre
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int ok = JOptionPane.showConfirmDialog(
                        MainFrame.this,
                        "Fermer l'application ?",
                        "Confirmation",
                        JOptionPane.YES_NO_OPTION
                );
                if (ok == JOptionPane.YES_OPTION) {
                    try {
                        ServiceRegistry.get().shutdown();
                    } finally {
                        dispose();
                        System.exit(0);
                    }
                }
            }
        });

        initUi();
    }

    private void initUi() {
        getContentPane().setLayout(new BorderLayout());

        // ---- Sidebar ----
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        sidebar.setPreferredSize(new Dimension(210, 0));

        JLabel title = new JLabel("REPAIRSHOP", SwingConstants.CENTER);
        title.setPreferredSize(new Dimension(190, 30));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        sidebar.add(title);

        JLabel role = new JLabel("Rôle : " + session.getRole(), SwingConstants.CENTER);
        role.setPreferredSize(new Dimension(190, 20));
        sidebar.add(role);

        JLabel user = new JLabel(session.getLogin(), SwingConstants.CENTER);
        user.setPreferredSize(new Dimension(190, 20));
        sidebar.add(user);

        sidebar.add(new JLabel(" ")); // espace

        JButton btnDashboard = new JButton("Dashboard");
        JButton btnReparations = new JButton("Réparations");
        JButton btnSuivi = new JButton("Suivi");
        JButton btnClients = new JButton("Clients");
        JButton btnCaisse = new JButton("Caisse");
        JButton btnEmprunts = new JButton("Emprunts / Prêts");

        // Bouton Déconnexion
        JButton btnLogout = new JButton("Déconnexion");

        // Taille uniforme
        Dimension btnSize = new Dimension(190, 32);
        btnDashboard.setPreferredSize(btnSize);
        btnReparations.setPreferredSize(btnSize);
        btnSuivi.setPreferredSize(btnSize);
        btnClients.setPreferredSize(btnSize);
        btnCaisse.setPreferredSize(btnSize);
        btnEmprunts.setPreferredSize(btnSize);
        btnLogout.setPreferredSize(btnSize);

        sidebar.add(btnDashboard);
        sidebar.add(btnReparations);
        sidebar.add(btnSuivi);
        sidebar.add(btnClients);
        sidebar.add(btnCaisse);
        sidebar.add(btnEmprunts);

        sidebar.add(new JLabel(" ")); // espace
        sidebar.add(btnLogout);

        getContentPane().add(sidebar, BorderLayout.WEST);

        // ---- Content (CardLayout) ----
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        contentPanel.add(new DashboardPanel(session), CARD_DASHBOARD);
        contentPanel.add(new ReparationsPanel(session), CARD_REPARATIONS);
        contentPanel.add(new ClientsPanel(session), CARD_CLIENTS);
        contentPanel.add(new CaissePanel(session), CARD_CAISSE);
        contentPanel.add(new EmpruntsPanel(session), CARD_EMPRUNTS);

        getContentPane().add(contentPanel, BorderLayout.CENTER);

        // ---- Navigation actions ----
        btnDashboard.addActionListener(e -> showCard(CARD_DASHBOARD));
        btnReparations.addActionListener(e -> showCard(CARD_REPARATIONS));
        btnClients.addActionListener(e -> showCard(CARD_CLIENTS));
        btnCaisse.addActionListener(e -> showCard(CARD_CAISSE));
        btnEmprunts.addActionListener(e -> showCard(CARD_EMPRUNTS));

        // Option A : Suivi dans un Dialog modal
        btnSuivi.addActionListener(e -> openSuiviDialog());

        // Déconnexion
        btnLogout.addActionListener(e -> doLogout());

        // Par défaut
        showCard(CARD_DASHBOARD);
    }

    private void openSuiviDialog() {
        SuiviDialog dlg = new SuiviDialog(this, session);
        dlg.setVisible(true);
    }

    private void doLogout() {
        int ok = JOptionPane.showConfirmDialog(
                this,
                "Se déconnecter ?",
                "Déconnexion",
                JOptionPane.YES_NO_OPTION
        );
        if (ok != JOptionPane.YES_OPTION) return;

        // IMPORTANT : on ne shutdown PAS Hibernate ici (reconnexion possible).
        dispose();

        LoginFrame login = new LoginFrame();
        login.setLocationRelativeTo(null);
        login.setVisible(true);
    }

    private JPanel simplePage(String text) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 18f));
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    private void showCard(String id) {
        cardLayout.show(contentPanel, id);
    }
}