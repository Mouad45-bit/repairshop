package com.maven.repairshop.ui.frames;

import com.maven.repairshop.model.Utilisateur;
import com.maven.repairshop.service.exceptions.BusinessException;
import com.maven.repairshop.ui.controllers.AuthController;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final SessionContext session;
    private final AuthController authController = new AuthController();

    private JTextField txtLogin;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnTracking; // client

    public LoginFrame(SessionContext session) {
        super("RepairShop — Connexion");
        this.session = session;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(420, 320);
        setLocationRelativeTo(null);

        setContentPane(buildContent());
        getRootPane().setDefaultButton(btnLogin);
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Connexion");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JLabel subtitle = new JLabel("Veuillez saisir vos identifiants.");
        subtitle.setForeground(new Color(90, 90, 90));

        JPanel head = new JPanel(new GridLayout(2, 1, 0, 6));
        head.setOpaque(false);
        head.add(title);
        head.add(subtitle);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 0, 8, 0);
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        txtLogin = new JTextField();
        txtPassword = new JPasswordField();
        btnLogin = new JButton("Se connecter");
        btnTracking = new JButton("Suivi client (code)");

        c.gridy = 0;
        form.add(labeled("Login / Email", txtLogin), c);

        c.gridy = 1;
        form.add(labeled("Mot de passe", txtPassword), c);

        c.gridy = 2;
        form.add(btnLogin, c);

        c.gridy = 3;
        form.add(btnTracking, c);

        btnLogin.addActionListener(e -> doLogin());
        btnTracking.addActionListener(e -> openTracking());

        root.add(head, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);

        return root;
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void doLogin() {
        btnLogin.setEnabled(false);
        try {
            String login = txtLogin.getText().trim();
            String pass = new String(txtPassword.getPassword());

            Utilisateur user = authController.login(login, pass);
            session.setCurrentUser(user);

            SwingUtilities.invokeLater(() -> {
                dispose();
                new MainFrame(session).setVisible(true);
            });

        } catch (BusinessException ex) {
            UiDialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur inattendue : " + ex.getMessage());
        } finally {
            btnLogin.setEnabled(true);
        }
    }

    private void openTracking() {
        SwingUtilities.invokeLater(() -> new ClientTrackingFrame().setVisible(true));
    }
}