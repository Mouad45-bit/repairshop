package com.maven.repairshop.ui.frames;

import com.maven.repairshop.ui.pages.TrackingPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ClientTrackingFrame extends JFrame {

    public ClientTrackingFrame() {
        super("RepairShop — Suivi Client");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(900, 520);
        setLocationRelativeTo(null);

        setContentPane(buildRoot());
    }

    private JComponent buildRoot() {
        JPanel root = new JPanel(new BorderLayout());

        root.add(buildTopbar(), BorderLayout.NORTH);
        root.add(new TrackingPanel(), BorderLayout.CENTER);

        return root;
    }

    private JComponent buildTopbar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel left = new JLabel("RepairShop — Suivi");
        left.setFont(left.getFont().deriveFont(Font.BOLD, 18f));

        JLabel right = new JLabel("Accès client (lecture seule)");
        right.setForeground(new Color(90, 90, 90));

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }
}