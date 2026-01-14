package com.maven.repairshop.ui.controllers;

import java.awt.Component;
import javax.swing.JOptionPane;

import com.maven.repairshop.service.exceptions.BusinessException;
import com.maven.repairshop.service.exceptions.NotFoundException;
import com.maven.repairshop.service.exceptions.ValidationException;

public final class UiDialogs {

    private UiDialogs() {}

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warn(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Attention", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public static void handle(Component parent, Throwable ex) {
        if (ex == null) {
            error(parent, "Erreur inattendue.");
            return;
        }

        // Validation "UI-friendly" (souvent utilisé dans controllers)
        if (ex instanceof IllegalArgumentException) {
            warn(parent, ex.getMessage());
            return;
        }

        if (ex instanceof ValidationException) {
            warn(parent, ex.getMessage());
            return;
        }
        if (ex instanceof NotFoundException) {
            warn(parent, ex.getMessage());
            return;
        }
        if (ex instanceof BusinessException) {
            warn(parent, ex.getMessage());
            return;
        }

        error(parent, "Erreur inattendue: " + ex.getMessage());
        ex.printStackTrace();
    }
}