package com.maven.repairshop.ui.util;

import javax.swing.*;
import java.awt.*;

public final class UiDialogs {

    private UiDialogs() {}

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public static void success(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        int r = JOptionPane.showConfirmDialog(parent, message, "Confirmation", JOptionPane.YES_NO_OPTION);
        return r == JOptionPane.YES_OPTION;
    }
}