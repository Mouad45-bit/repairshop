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
        JButton btnClients = new JButton("Clients");
        JButton btnCaisse = new JButton("Caisse");
        JButton btnEmprunts = new JButton("Emprunts / Prêts");

        // Taille uniforme
        Dimension btnSize = new Dimension(190, 32);
        btnDashboard.setPreferredSize(btnSize);
        btnReparations.setPreferredSize(btnSize);
        btnClients.setPreferredSize(btnSize);
        btnCaisse.setPreferredSize(btnSize);
        btnEmprunts.setPreferredSize(btnSize);

        sidebar.add(btnDashboard);
        sidebar.add(btnReparations);
        sidebar.add(btnClients);
        sidebar.add(btnCaisse);
        sidebar.add(btnEmprunts);

        // Exemple : certaines pages peuvent être visibles uniquement pour propriétaire
        // (tu pourras ajuster après selon votre cahier)
        if (session.isReparateur()) {
            // le réparateur peut avoir accès à caisse/emprunts aussi, donc on ne masque rien pour l'instant
            // si tu veux masquer "Stats" plus tard, on fera un bouton Stats réservé propriétaire
        }

        getContentPane().add(sidebar, BorderLayout.WEST);

        // ---- Content (CardLayout) ----
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Pages placeholder pour l’instant (on les fera fichier par fichier)
        contentPanel.add(simplePage("Dashboard (à coder)"), CARD_DASHBOARD);
        contentPanel.add(simplePage("Réparations (à coder)"), CARD_REPARATIONS);
        contentPanel.add(simplePage("Clients (à coder)"), CARD_CLIENTS);
        contentPanel.add(simplePage("Caisse (à coder)"), CARD_CAISSE);
        contentPanel.add(simplePage("Emprunts / Prêts (à coder)"), CARD_EMPRUNTS);

        getContentPane().add(contentPanel, BorderLayout.CENTER);

        // ---- Navigation actions ----
        btnDashboard.addActionListener(e -> showCard(CARD_DASHBOARD));
        btnReparations.addActionListener(e -> showCard(CARD_REPARATIONS));
        btnClients.addActionListener(e -> showCard(CARD_CLIENTS));
        btnCaisse.addActionListener(e -> showCard(CARD_CAISSE));
        btnEmprunts.addActionListener(e -> showCard(CARD_EMPRUNTS));

        // Par défaut
        showCard(CARD_DASHBOARD);
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