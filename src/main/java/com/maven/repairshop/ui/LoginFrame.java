package com.maven.repairshop.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;

import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.session.SessionContext;

/**
 * Login UI (Swing).
 * - Rôle: Propriétaire / Réparateur
 * - Après succès: ouvre MainFrame
 *
 * Frontend-only (merge-friendly):
 * - Tant que le backend Auth n'est pas mergé, on utilise un bypass DEV.
 * - Après merge, mettre DEV_AUTH_BYPASS = false et brancher AuthService.
 */
public class LoginFrame extends JFrame {

    private JPanel contentPane;
    private JTextField txtLogin;
    private JPasswordField txtPassword;

    // ✅ UI-only: bypass temporaire (mettre false après merge Auth)
    private static final boolean DEV_AUTH_BYPASS = true;

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
            UiDialogs.warn(this, "Veuillez saisir le login et le mot de passe.");
            return;
        }

        try {
            if (DEV_AUTH_BYPASS) {
                // Règle simple de démo:
                // - si login contient "prop" => propriétaire
                // - sinon => réparateur
                SessionContext.Role role =
                        login.toLowerCase().contains("prop")
                                ? SessionContext.Role.PROPRIETAIRE
                                : SessionContext.Role.REPARATEUR;

                SessionContext session = SessionContext.dev(role, login);
                openMain(session);
                return;
            }

            // Quand le backend Auth sera mergé, on remplacera ceci par authService.login()
            UiDialogs.info(this,
                    "AuthService pas encore branché.\n" +
                    "Après merge backend, on remplacera ce bloc par authService.login().");

        } catch (Exception ex) {
            // cohérent avec le reste
            UiDialogs.handle(this, ex);
        }
    }

    private void openMain(SessionContext session) {
        MainFrame main = new MainFrame(session);
        main.setLocationRelativeTo(null);
        main.setVisible(true);
        dispose();
    }
}