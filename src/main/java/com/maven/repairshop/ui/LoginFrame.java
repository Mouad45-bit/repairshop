package com.maven.repairshop.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import com.maven.repairshop.model.Utilisateur;
import com.maven.repairshop.ui.session.SessionContext;

/**
 * Login UI (Swing).
 * - Rôle: Propriétaire / Réparateur
 * - Après succès: ouvre MainFrame
 *
 * NOTE (merge-friendly):
 * - Tant que le backend Auth n'est pas mergé, on autorise un bypass DEV.
 * - Le jour du merge Auth, il suffit de mettre DEV_AUTH_BYPASS à false
 *   et remplacer le bloc "TODO AUTH" par l'appel réel.
 */
public class LoginFrame extends JFrame {

    // UI-only: bypass temporaire (à mettre false après merge Auth)
    private static final boolean DEV_AUTH_BYPASS = true;

    private JPanel contentPane;
    private JTextField txtLogin;
    private JPasswordField txtPassword;

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
        setBounds(100, 100, 420, 260);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblLogin = new JLabel("Login :");
        lblLogin.setBounds(28, 40, 120, 20);
        contentPane.add(lblLogin);

        txtLogin = new JTextField();
        txtLogin.setBounds(150, 40, 210, 24);
        contentPane.add(txtLogin);
        txtLogin.setColumns(10);

        JLabel lblPassword = new JLabel("Mot de passe :");
        lblPassword.setBounds(28, 80, 120, 20);
        contentPane.add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(150, 80, 210, 24);
        contentPane.add(txtPassword);

        JButton btnLogin = new JButton("Se connecter");
        btnLogin.setBounds(150, 130, 210, 30);
        contentPane.add(btnLogin);

        // Action: Enter dans password = login
        txtPassword.addActionListener(e -> doLogin());
        btnLogin.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        String login = txtLogin.getText() != null ? txtLogin.getText().trim() : "";
        String password = new String(txtPassword.getPassword());

        if (login.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir le login et le mot de passe.",
                    "Champs obligatoires",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // --------------------
            // MODE DEV (frontend-only)
            // --------------------
            if (DEV_AUTH_BYPASS) {
                SessionContext session = createDevSession(login);
                openMain(session);
                return;
            }

            // --------------------
            // TODO AUTH (après merge backend Auth)
            // --------------------
            JOptionPane.showMessageDialog(this,
                    "AuthService pas encore branché.\n" +
                    "Après merge backend, remplacer ce bloc par authService.login().",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);

            // Exemple futur:
            // Utilisateur u = ServiceRegistry.get().auth().login(login, password);
            // SessionContext session = SessionContext.fromUser(u);
            // openMain(session);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Connexion échouée",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Création d'une session "dev" sans backend Auth.
     * IMPORTANT: on ne touche pas le backend; on crée un Utilisateur minimal côté UI.
     */
    private SessionContext createDevSession(String login) {
        // Choix simple: si le login contient "prop" => PROPRIETAIRE, sinon REPARATEUR
        boolean isOwner = login.toLowerCase().contains("prop");

        Utilisateur u = new Utilisateur();
        u.setLogin(login);

        // Adaptation: selon ton modèle Utilisateur/Role
        // Si tu as un enum Role, adapte ici.
        // On essaye de rester minimal:
        if (isOwner) {
            u.setRole("PROPRIETAIRE");
        } else {
            u.setRole("REPARATEUR");
        }

        // Si SessionContext.fromUser(u) existe déjà, c'est parfait:
        return SessionContext.fromUser(u);
    }

    private void openMain(SessionContext session) {
        MainFrame main = new MainFrame(session);
        main.setLocationRelativeTo(null);
        main.setVisible(true);
        dispose();
    }
}