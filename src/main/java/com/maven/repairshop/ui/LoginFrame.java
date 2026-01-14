package com.maven.repairshop.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.session.SessionContext;

/**
 * Login UI (Swing) - Design Diprella.
 * Ecran divisé : Formulaire à gauche (Blanc), Accueil à droite (Vert).
 */
public class LoginFrame extends JFrame {

    // Couleurs du design
    private final Color MAIN_COLOR = new Color(44, 185, 152); // Vert Diprella
    private final Color TEXT_GRAY = new Color(150, 150, 150);
    private final Color BG_WHITE = Color.WHITE;

    private JPanel mainPanel;
    private JTextField txtLogin;
    private JPasswordField txtPassword;

    // Bypass pour le développement (pour se connecter sans base de données)
    private static final boolean DEV_AUTH_BYPASS = true;

    // --- POINT D'ENTRÉE ---
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                LoginFrame frame = new LoginFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public LoginFrame() {
        setTitle("RepairShop - Connexion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 900, 550);
        setLocationRelativeTo(null); // Centrer à l'écran
        setResizable(false); // Fixer la taille pour préserver le layout

        // Conteneur principal : Grille de 2 colonnes (Gauche / Droite)
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 2)); 
        setContentPane(mainPanel);

        // --- PARTIE GAUCHE (Formulaire Blanc) ---
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(BG_WHITE);
        leftPanel.setLayout(null); // Layout absolu pour positionnement précis

        // Titre
        JLabel lblTitle = new JLabel("Connexion Atelier");
        lblTitle.setForeground(MAIN_COLOR);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBounds(0, 80, 435, 40);
        leftPanel.add(lblTitle);

        // Sous-titre
        JLabel lblSub = new JLabel("Connectez-vous pour gérer vos réparations");
        lblSub.setForeground(TEXT_GRAY);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        lblSub.setBounds(0, 125, 435, 20);
        leftPanel.add(lblSub);

        // Champ Email / Login
        JLabel lblEmailIcon = new JLabel("Identifiant");
        lblEmailIcon.setForeground(TEXT_GRAY);
        lblEmailIcon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEmailIcon.setBounds(85, 180, 100, 20);
        leftPanel.add(lblEmailIcon);

        txtLogin = new JTextField();
        txtLogin.setBounds(85, 200, 280, 35);
        txtLogin.setBorder(new MatteBorder(0, 0, 2, 0, new Color(230, 230, 230)));
        txtLogin.setBackground(BG_WHITE);
        txtLogin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        leftPanel.add(txtLogin);

        // Champ Mot de passe
        JLabel lblPassIcon = new JLabel("Mot de passe");
        lblPassIcon.setForeground(TEXT_GRAY);
        lblPassIcon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPassIcon.setBounds(85, 260, 100, 20);
        leftPanel.add(lblPassIcon);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(85, 280, 280, 35);
        txtPassword.setBorder(new MatteBorder(0, 0, 2, 0, new Color(230, 230, 230)));
        txtPassword.setBackground(BG_WHITE);
        leftPanel.add(txtPassword);

        // Bouton SE CONNECTER
        JButton btnLogin = new JButton("SE CONNECTER");
        btnLogin.setBounds(125, 360, 200, 45);
        btnLogin.setBackground(MAIN_COLOR);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Effet de survol
        btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnLogin.setBackground(MAIN_COLOR.darker()); }
            public void mouseExited(MouseEvent e) { btnLogin.setBackground(MAIN_COLOR); }
        });
        leftPanel.add(btnLogin);


        // --- PARTIE DROITE (Fond Vert) ---
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(MAIN_COLOR);
        rightPanel.setLayout(null);

        JLabel lblHello = new JLabel("Bienvenue !");
        lblHello.setForeground(Color.WHITE);
        lblHello.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblHello.setHorizontalAlignment(SwingConstants.CENTER);
        lblHello.setBounds(0, 150, 435, 40);
        rightPanel.add(lblHello);

        JLabel lblDesc = new JLabel("<html><center>Gérez vos clients, tickets et factures<br>avec simplicité et efficacité.</center></html>");
        lblDesc.setForeground(Color.WHITE);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDesc.setHorizontalAlignment(SwingConstants.CENTER);
        lblDesc.setBounds(0, 200, 435, 60);
        rightPanel.add(lblDesc);

        // Bouton Info (Style Ghost)
        JButton btnInfo = new JButton("VERSION DEMO");
        btnInfo.setBounds(125, 290, 200, 45);
        btnInfo.setBackground(MAIN_COLOR);
        btnInfo.setForeground(Color.WHITE);
        btnInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnInfo.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        btnInfo.setContentAreaFilled(false);
        btnInfo.setFocusPainted(false);
        rightPanel.add(btnInfo);

        // Ajout au panneau principal
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        // Actions
        txtPassword.addActionListener(e -> doLogin());
        btnLogin.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        String login = txtLogin.getText() != null ? txtLogin.getText().trim() : "";
        String password = new String(txtPassword.getPassword());

        if (login.isEmpty() || password.isEmpty()) {
            UiDialogs.warn(this, "Veuillez entrer vos identifiants.");
            return;
        }

        try {
            if (DEV_AUTH_BYPASS) {
                // Simulation simple des rôles
                SessionContext.Role role = login.toLowerCase().contains("admin") 
                        ? SessionContext.Role.PROPRIETAIRE 
                        : SessionContext.Role.REPARATEUR;

                SessionContext session = SessionContext.dev(role, login);
                openMain(session);
                return;
            }
            // Ici viendra la vraie connexion plus tard
        } catch (Exception ex) {
            UiDialogs.handle(this, ex);
        }
    }

    private void openMain(SessionContext session) {
        MainFrame main = new MainFrame(session);
        main.setLocationRelativeTo(null);
        main.setVisible(true);
        dispose(); // Ferme la fenêtre de login
    }
}