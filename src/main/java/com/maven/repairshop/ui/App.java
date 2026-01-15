package com.maven.repairshop.ui;

import com.maven.repairshop.ui.frames.LoginFrame;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.AppTheme;

import javax.swing.*;

public class App {

    public static void main(String[] args) {
        AppTheme.install();

        SwingUtilities.invokeLater(() -> {
            SessionContext session = new SessionContext();
            new LoginFrame(session).setVisible(true);
        });
    }
}