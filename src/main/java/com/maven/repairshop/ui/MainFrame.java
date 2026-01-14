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

import com.maven.repairshop.ui.controllers.UiDialogs;
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
 *
 * IMPORTANT (merge-friendly):
 * - Lazy pages: on instancie les panels uniquement quand on en a besoin
 *   => évite les crash si un module backend n’est pas encore mergé.
 * - Désactivation des boutons si le service backend correspondant est indisponible.
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

    // Lazy panels
    private DashboardPanel dashboardPanel;
    private ReparationsPanel reparationsPanel;
    private ClientsPanel clientsPanel;
    private CaissePanel caissePanel;
    private EmpruntsPanel empruntsPanel;

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

        // On démarre avec des placeholders (évite d'instancier les pages trop tôt)
        contentPanel.add(simplePage("Chargement..."), CARD_DASHBOARD);
        contentPanel.add(simplePage("Module Réparations indisponible."), CARD_REPARATIONS);
        contentPanel.add(simplePage("Module Clients indisponible."), CARD_CLIENTS);
        contentPanel.add(simplePage("Chargement..."), CARD_CAISSE);
        contentPanel.add(simplePage("Chargement..."), CARD_EMPRUNTS);

        getContentPane().add(contentPanel, BorderLayout.CENTER);

        // ---- Disponibilité backend (frontend-only) ----
        boolean repOk = ServiceRegistry.get().isReparationsAvailable();
        boolean cliOk = ServiceRegistry.get().isClientsAvailable();
        boolean empOk = ServiceRegistry.get().isEmpruntsAvailable();

        // Désactiver boutons si module backend absent
        btnReparations.setEnabled(repOk);
        btnSuivi.setEnabled(repOk);
        btnClients.setEnabled(cliOk);
        btnEmprunts.setEnabled(empOk);

        // Tooltip explicatif (utile pour toi + pour démo)
        if (!repOk) {
            btnReparations.setToolTipText("Backend Réparations non mergé (ReparationServiceImpl manquant).");
            btnSuivi.setToolTipText("Backend Réparations non mergé (ReparationServiceImpl manquant).");
        }
        if (!cliOk) {
            btnClients.setToolTipText("Backend Clients non mergé (ClientServiceImpl manquant).");
        }
        if (!empOk) {
            btnEmprunts.setToolTipText("Backend Emprunts non mergé (EmpruntServiceImpl manquant).");
        }

        // ---- Navigation actions ----
        btnDashboard.addActionListener(e -> showDashboard());
        btnCaisse.addActionListener(e -> showCaisse());

        // Réparations
        btnReparations.addActionListener(e -> {
            if (!repOk) {
                UiDialogs.info(this,
                        "Module Réparations indisponible.\n" +
                        "Merge le backend correspondant puis relance.");
                return;
            }
            showReparations();
        });

        // Clients
        btnClients.addActionListener(e -> {
            if (!cliOk) {
                UiDialogs.info(this,
                        "Module Clients indisponible.\n" +
                        "Merge le backend correspondant puis relance.");
                return;
            }
            showClients();
        });

        // Emprunts
        btnEmprunts.addActionListener(e -> {
            if (!empOk) {
                UiDialogs.info(this,
                        "Module Emprunts / Prêts indisponible.\n" +
                        "Merge le backend correspondant puis relance.");
                return;
            }
            showEmprunts();
        });

        // Suivi
        btnSuivi.addActionListener(e -> {
            if (!repOk) {
                UiDialogs.info(this,
                        "Module Suivi indisponible.\n" +
                        "Merge le backend Réparations puis relance.");
                return;
            }
            openSuiviDialog();
        });

        // Déconnexion
        btnLogout.addActionListener(e -> doLogout());

        // Par défaut
        showDashboard();
    }

    // --- Lazy show helpers ---

    private void showDashboard() {
        if (dashboardPanel == null) {
            dashboardPanel = new DashboardPanel(session);
            contentPanel.add(dashboardPanel, CARD_DASHBOARD);
        }
        showCard(CARD_DASHBOARD);
    }

    private void showReparations() {
        if (reparationsPanel == null) {
            reparationsPanel = new ReparationsPanel(session);
            contentPanel.add(reparationsPanel, CARD_REPARATIONS);
        }
        showCard(CARD_REPARATIONS);
    }

    private void showClients() {
        if (clientsPanel == null) {
            clientsPanel = new ClientsPanel(session);
            contentPanel.add(clientsPanel, CARD_CLIENTS);
        }
        showCard(CARD_CLIENTS);
    }

    private void showCaisse() {
        if (caissePanel == null) {
            caissePanel = new CaissePanel(session);
            contentPanel.add(caissePanel, CARD_CAISSE);
        }
        showCard(CARD_CAISSE);
    }

    private void showEmprunts() {
        if (empruntsPanel == null) {
            empruntsPanel = new EmpruntsPanel(session);
            contentPanel.add(empruntsPanel, CARD_EMPRUNTS);
        }
        showCard(CARD_EMPRUNTS);
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