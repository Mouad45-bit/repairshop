package com.maven.repairshop.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.dialogs.SuiviDialog;
import com.maven.repairshop.ui.pages.CaissePanel;
import com.maven.repairshop.ui.pages.ClientsPanel;
import com.maven.repairshop.ui.pages.DashboardPanel;
import com.maven.repairshop.ui.pages.EmpruntsPanel;
import com.maven.repairshop.ui.pages.ReparationsPanel;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.ServiceRegistry;

/**
 * Fenêtre principale - Design Modern "Diprella".
 * MODE DESIGN : Tous les accès sont forcés à TRUE pour tester l'interface.
 */
public class MainFrame extends JFrame {

    private final SessionContext session;

    // --- COULEURS DU DESIGN ---
    private final Color SIDEBAR_BG = new Color(44, 185, 152); // Vert Diprella
    private final Color SIDEBAR_HOVER = new Color(35, 160, 130);
    private final Color TEXT_WHITE = Color.WHITE;
    private final Color TEXT_DISABLED = new Color(255, 255, 255, 100);
    private final Color BG_CONTENT = new Color(240, 242, 245);

    private CardLayout cardLayout;
    private JPanel contentPanel;

    // Lazy panels
    private DashboardPanel dashboardPanel;
    private ReparationsPanel reparationsPanel;
    private ClientsPanel clientsPanel;
    private CaissePanel caissePanel;
    private EmpruntsPanel empruntsPanel;

    // IDs
    private static final String CARD_DASHBOARD = "DASHBOARD";
    private static final String CARD_REPARATIONS = "REPARATIONS";
    private static final String CARD_CLIENTS = "CLIENTS";
    private static final String CARD_CAISSE = "CAISSE";
    private static final String CARD_EMPRUNTS = "EMPRUNTS";

    public MainFrame(SessionContext session) {
        this.session = session;
        setTitle("RepairShop v2.0 - Mode Design");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 750)); // Grande fenêtre confortable
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { confirmExit(); }
        });

        initUi();
    }

    private void initUi() {
        getContentPane().setLayout(new BorderLayout());

        // ==========================================
        // 1. SIDEBAR (GAUCHE)
        // ==========================================
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        // -- PROFIL (Cercle avatar + Nom) --
        JPanel profilePanel = new JPanel();
        profilePanel.setOpaque(false);
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Avatar simulé (Cercle blanc avec initiales)
        JLabel avatar = new JLabel(session.getLogin().substring(0, 1).toUpperCase()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, 48, 48);
                super.paintComponent(g);
            }
        };
        avatar.setFont(new Font("SansSerif", Font.BOLD, 24));
        avatar.setForeground(SIDEBAR_BG); // Lettre en vert
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(50, 50));
        avatar.setMaximumSize(new Dimension(50, 50));
        avatar.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblUser = new JLabel(session.getLogin());
        lblUser.setForeground(TEXT_WHITE);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JLabel lblRole = new JLabel(session.getRole().toString());
        lblRole.setForeground(new Color(230, 255, 240));
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        profilePanel.add(avatar);
        profilePanel.add(Box.createVerticalStrut(10));
        profilePanel.add(lblUser);
        profilePanel.add(lblRole);

        sidebar.add(profilePanel);
        sidebar.add(Box.createVerticalStrut(50)); // Espace après profil

        // -- MENU NAVIGATION --
        
        // ---------------------------------------------------------
        // BYPASS DESIGN : ON FORCE TOUT A TRUE POUR TESTER LA VUE
        // ---------------------------------------------------------
        boolean repOk = true; 
        boolean cliOk = true;
        boolean empOk = true;
        // ---------------------------------------------------------

        // Ajout des boutons (Tous activés car repOk/cliOk = true)
        addMenuBtn(sidebar, "Dashboard", true, e -> showDashboard());
        addMenuBtn(sidebar, "Réparations", repOk, e -> showReparations());
        addMenuBtn(sidebar, "Suivi Atelier", repOk, e -> openSuiviDialog());
        addMenuBtn(sidebar, "Clients", cliOk, e -> showClients());
        addMenuBtn(sidebar, "Caisse / Factures", true, e -> showCaisse());
        addMenuBtn(sidebar, "Prêts & Emprunts", empOk, e -> showEmprunts());

        // -- DECONNEXION (en bas) --
        sidebar.add(Box.createVerticalGlue()); // Pousse vers le bas
        JButton btnLogout = createFlatButton("Se déconnecter", true);
        btnLogout.addActionListener(e -> doLogout());
        sidebar.add(btnLogout);

        getContentPane().add(sidebar, BorderLayout.WEST);

        // ==========================================
        // 2. CONTENU CENTRAL (CardLayout)
        // ==========================================
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_CONTENT);
        
        // Placeholder pour éviter erreur "Card not found" avant le premier clic
        contentPanel.add(new JPanel(), CARD_DASHBOARD); 
        
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        // Afficher Dashboard au démarrage
        showDashboard();
    }

    /**
     * Crée un bouton de menu "Flat" propre.
     */
    private void addMenuBtn(JPanel sidebar, String text, boolean isAvailable, java.awt.event.ActionListener action) {
        JButton btn = createFlatButton(text, isAvailable);
        btn.addActionListener(action);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(5)); // Petit espace
    }

    private JButton createFlatButton(String text, boolean isAvailable) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(isAvailable ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        
        if (isAvailable) {
            btn.setForeground(TEXT_WHITE);
            btn.setBackground(SIDEBAR_BG);
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setBackground(SIDEBAR_HOVER); }
                public void mouseExited(MouseEvent e) { btn.setBackground(SIDEBAR_BG); }
            });
        } else {
            btn.setForeground(TEXT_DISABLED);
            btn.setBackground(SIDEBAR_BG);
        }
        
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return btn;
    }

    // --- Helpers Navigation (Lazy Loading) ---

    private void showDashboard() {
        if (dashboardPanel == null) {
            dashboardPanel = new DashboardPanel(session);
            contentPanel.add(dashboardPanel, CARD_DASHBOARD);
        }
        cardLayout.show(contentPanel, CARD_DASHBOARD);
    }
    
    private void showReparations() {
        if (reparationsPanel == null) {
            reparationsPanel = new ReparationsPanel(session);
            contentPanel.add(reparationsPanel, CARD_REPARATIONS);
        }
        cardLayout.show(contentPanel, CARD_REPARATIONS);
    }
    
    private void showClients() {
        if (clientsPanel == null) {
            clientsPanel = new ClientsPanel(session);
            contentPanel.add(clientsPanel, CARD_CLIENTS);
        }
        cardLayout.show(contentPanel, CARD_CLIENTS);
    }

    private void showCaisse() {
        if (caissePanel == null) {
            caissePanel = new CaissePanel(session);
            contentPanel.add(caissePanel, CARD_CAISSE);
        }
        cardLayout.show(contentPanel, CARD_CAISSE);
    }

    private void showEmprunts() {
        if (empruntsPanel == null) {
            empruntsPanel = new EmpruntsPanel(session);
            contentPanel.add(empruntsPanel, CARD_EMPRUNTS);
        }
        cardLayout.show(contentPanel, CARD_EMPRUNTS);
    }

    private void openSuiviDialog() {
        SuiviDialog dlg = new SuiviDialog(this, session);
        dlg.setVisible(true);
    }

    private void doLogout() {
        int ok = JOptionPane.showConfirmDialog(this, "Se déconnecter ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if(ok == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
    
    private void confirmExit() {
        int ok = JOptionPane.showConfirmDialog(this, "Quitter l'application ?", "Sortie", JOptionPane.YES_NO_OPTION);
        if(ok == JOptionPane.YES_OPTION) {
            try { ServiceRegistry.get().shutdown(); } finally { dispose(); System.exit(0); }
        }
    }
}