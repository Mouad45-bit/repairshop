package com.maven.repairshop.ui.util;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;

public final class AppTheme {

    private AppTheme() {}

    public static void install() {
        setNimbusLookAndFeel();
        setGlobalFont(new Font("SansSerif", Font.PLAIN, 13));
        tuneDefaults();
    }

    private static void setNimbusLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception ignored) {
            // on garde le LAF par défaut si Nimbus indisponible
        }
    }

    private static void setGlobalFont(Font font) {
        FontUIResource fur = new FontUIResource(font);
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object val = UIManager.get(key);
            if (val instanceof FontUIResource) {
                UIManager.put(key, fur);
            }
        }
    }

    private static void tuneDefaults() {
        UIManager.put("Table.rowHeight", 26);
        UIManager.put("OptionPane.minimumSize", new Dimension(420, 160));
        UIManager.put("TextField.margin", new Insets(6, 8, 6, 8));
        UIManager.put("PasswordField.margin", new Insets(6, 8, 6, 8));
        UIManager.put("Button.margin", new Insets(8, 12, 8, 12));
    }
}