package com.maven.repairshop.ui.frames;

import com.maven.repairshop.ui.pages.DashboardPanel;
import com.maven.repairshop.ui.pages.NotImplementedPanel;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private final SessionContext session;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    private final Map<String, JComponent> pages = new LinkedHashMap<>();

    public MainFrame(SessionContext session) {
        super("RepairShop");
        this.session = session;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);

        setContentPane(buildRoot());
        showPage("dashboard");
    }

    private JComponent buildRoot() {
        JPanel root = new JPanel(new BorderLayout());

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildTopbar(), BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);

        return root;
    }

    private JComponent buildTopbar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(10, 14, 10, 14));

        String role = session.isProprietaire() ? "Propriétaire" : "Réparateur";
        String name = session.getCurrentUser() != null ? session.getCurrentUser().getNom() : "";

        JLabel left = new JLabel("RepairShop");
        left.setFont(left.getFont().deriveFont(Font.BOLD, 18f));

        JLabel right = new JLabel(role + " — " + name);
        right.setForeground(new Color(90, 90, 90));

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JComponent buildSidebar() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(240, 0));
        side.setLayout(new BorderLayout());
        side.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setOpaque(false);

        // Pages (placeholders au début, on les remplace fichier par fichier)
        register("dashboard", new DashboardPanel(session));

        if (session.isReparateur()) {
            register("reparations", new com.maven.repairshop.ui.pages.ReparationsPanel(session));
            register("clients", new NotImplementedPanel("Clients"));
            register("emprunts", new NotImplementedPanel("Emprunts"));
            register("caisse", new NotImplementedPanel("Caisse"));
        }

        if (session.isProprietaire()) {
            register("boutiques", new NotImplementedPanel("Boutiques"));
            register("reparateurs", new NotImplementedPanel("Réparateurs"));
            register("all_caisses", new NotImplementedPanel("Toutes les caisses"));
            register("all_reparations", new NotImplementedPanel("Toutes les réparations"));
            register("stats", new NotImplementedPanel("Statistiques"));
        }

        addMenuButton(menu, "Dashboard", "dashboard");
        if (session.isReparateur()) {
            addMenuButton(menu, "Réparations", "reparations");
            addMenuButton(menu, "Clients", "clients");
            addMenuButton(menu, "Emprunts", "emprunts");
            addMenuButton(menu, "Caisse", "caisse");
        }
        if (session.isProprietaire()) {
            addMenuButton(menu, "Boutiques", "boutiques");
            addMenuButton(menu, "Réparateurs", "reparateurs");
            addMenuButton(menu, "Caisses", "all_caisses");
            addMenuButton(menu, "Réparations", "all_reparations");
            addMenuButton(menu, "Stats", "stats");
        }

        menu.add(Box.createVerticalGlue());

        JButton btnLogout = new JButton("Déconnexion");
        btnLogout.addActionListener(e -> logout());
        btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);

        side.add(menu, BorderLayout.CENTER);
        side.add(btnLogout, BorderLayout.SOUTH);
        return side;
    }

    private JComponent buildContent() {
        for (Map.Entry<String, JComponent> e : pages.entrySet()) {
            content.add(e.getValue(), e.getKey());
        }
        content.setBorder(new EmptyBorder(10, 10, 10, 10));
        return content;
    }

    private void register(String key, JComponent component) {
        pages.put(key, component);
    }

    private void addMenuButton(JPanel menu, String label, String pageKey) {
        JButton b = new JButton(label);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.addActionListener(e -> showPage(pageKey));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(b);
        menu.add(Box.createVerticalStrut(8));
    }

    private void showPage(String key) {
        cardLayout.show(content, key);
    }

    private void logout() {
        if (!UiDialogs.confirm(this, "Se déconnecter ?")) return;
        session.clear();
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame(session).setVisible(true));
    }
}