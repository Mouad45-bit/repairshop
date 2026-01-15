package com.maven.repairshop.ui.pages;

import com.maven.repairshop.ui.session.SessionContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardPanel extends JPanel {

    public DashboardPanel(SessionContext session) {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        String role = session.isProprietaire() ? "Propriétaire" : "Réparateur";
        JLabel subtitle = new JLabel("Bienvenue (" + role + "). Sélectionnez un module dans le menu.");
        subtitle.setForeground(new Color(90, 90, 90));

        JPanel head = new JPanel(new GridLayout(2, 1, 0, 6));
        head.setOpaque(false);
        head.add(title);
        head.add(subtitle);

        add(head, BorderLayout.NORTH);
    }
}