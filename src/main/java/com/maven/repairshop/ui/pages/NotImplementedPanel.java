package com.maven.repairshop.ui.pages;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class NotImplementedPanel extends JPanel {

    public NotImplementedPanel(String moduleName) {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel(moduleName);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JLabel info = new JLabel("Module en cours d’implémentation (on le fait fichier par fichier).");
        info.setForeground(new Color(90, 90, 90));

        JPanel head = new JPanel(new GridLayout(2, 1, 0, 6));
        head.setOpaque(false);
        head.add(title);
        head.add(info);

        add(head, BorderLayout.NORTH);
    }
}