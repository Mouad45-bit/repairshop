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
import com.maven.repairshop.ui.util.ServiceRegistry;

/**
 * Login UI (Swing).
 * - Rôle: Propriétaire / Réparateur
 * - Après succès: ouvre MainFrame
 */
public class LoginFrame extends JFrame {

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
            // IMPORTANT :
            // Pour l'instant, on n'a pas encore AuthService câblé dans ServiceRegistry.
            // Donc 2 options :
            // (A) si vous avez déjà AuthService dans le backend, on l'appelle ici
            // (B) sinon on met un faux login temporaire (mode maquette)
            //
            // Je te fais la version PRO (A) dès que AuthService est prêt.
            //
            // TEMP: message clair
            JOptionPane.showMessageDialog(this,
                    "AuthService pas encore branché.\n" +
                    "Dès que le module Auth est livré, on remplace ce bloc par authService.login().",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);

            // Exemple futur (quand AuthService existe) :
            // Utilisateur u = ServiceRegistry.get().authService().login(login, password);
            // SessionContext session = SessionContext.fromUser(u);
            // openMain(session);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Connexion échouée",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openMain(SessionContext session) {
        MainFrame main = new MainFrame(session);
        main.setLocationRelativeTo(null);
        main.setVisible(true);
        dispose();
    }
}