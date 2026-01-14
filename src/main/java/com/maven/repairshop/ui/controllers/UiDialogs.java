package com.maven.repairshop.ui.controllers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog; // <--- Import Important ajouté
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window; // <--- Import Important ajouté
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

// Assurez-vous que ces classes existent bien dans votre projet
// Sinon, supprimez les blocs "if" correspondants plus bas
import com.maven.repairshop.service.exceptions.BusinessException;
import com.maven.repairshop.service.exceptions.NotFoundException;
import com.maven.repairshop.service.exceptions.ValidationException;

public final class UiDialogs {

    // Couleurs du Design
    private static final Color COL_INFO = new Color(44, 185, 152); // Vert Diprella
    private static final Color COL_WARN = new Color(243, 156, 18); // Orange
    private static final Color COL_ERR  = new Color(231, 76, 60);  // Rouge
    private static final Color COL_BG   = Color.WHITE;
    private static final Color COL_TXT  = new Color(50, 50, 50);

    private UiDialogs() {}

    // --- API PUBLIQUE ---

    public static void info(Component parent, String msg) {
        showModernDialog(parent, msg, "Information", COL_INFO, "ⓘ");
    }

    public static void warn(Component parent, String msg) {
        showModernDialog(parent, msg, "Attention", COL_WARN, "⚠");
    }

    public static void error(Component parent, String msg) {
        showModernDialog(parent, msg, "Erreur", COL_ERR, "✕");
    }

    public static void handle(Component parent, Throwable ex) {
        if (ex == null) {
            error(parent, "Erreur inattendue.");
            return;
        }

        // Si vous avez des erreurs ici, c'est que les classes d'exception n'existent pas.
        // Vous pouvez supprimer ces "if" si nécessaire.
        if (ex instanceof IllegalArgumentException || 
            ex instanceof ValidationException || 
            ex instanceof NotFoundException || 
            ex instanceof BusinessException) {
            warn(parent, ex.getMessage());
            return;
        }

        ex.printStackTrace();
        error(parent, "Erreur technique: " + ex.getMessage());
    }

    // --- MOTEUR DE DESIGN ---

    private static void showModernDialog(Component parent, String msg, String title, Color headerColor, String icon) {
        Window parentWin = SwingUtilities.getWindowAncestor(parent);
        
        // CORRECTION ICI : Utilisation explicite de Dialog.ModalityType
        JDialog dlg = new JDialog(parentWin, title, Dialog.ModalityType.APPLICATION_MODAL);
        
        dlg.setUndecorated(true);
        dlg.setLayout(new BorderLayout());
        dlg.getRootPane().setBorder(new LineBorder(new Color(200, 200, 200), 1));

        // En-tête
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(headerColor);
        header.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel lblTitle = new JLabel(icon + "  " + title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);

        makeDraggable(dlg, header);

        // Corps
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(COL_BG);
        body.setBorder(new EmptyBorder(20, 20, 20, 20));

        // HTML pour le retour à la ligne automatique
        JLabel lblMsg = new JLabel("<html><body style='width: 280px'>" + msg + "</body></html>");
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMsg.setForeground(COL_TXT);
        body.add(lblMsg, BorderLayout.CENTER);

        // Pied de page
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(COL_BG);
        footer.setBorder(new EmptyBorder(0, 10, 10, 10));

        JButton btnOk = new JButton("OK");
        styleButton(btnOk, headerColor);
        btnOk.addActionListener(e -> dlg.dispose());
        
        footer.add(btnOk);

        dlg.add(header, BorderLayout.NORTH);
        dlg.add(body, BorderLayout.CENTER);
        dlg.add(footer, BorderLayout.SOUTH);

        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    // --- Helpers ---

    private static void styleButton(JButton btn, Color color) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(color, 1, true),
                new EmptyBorder(8, 25, 8, 25)
        ));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(color.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(color); }
        });
    }

    private static void makeDraggable(JDialog dlg, JPanel handle) {
        final Point[] dragPoint = {null};
        handle.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dragPoint[0] = e.getPoint();
            }
        });
        handle.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragPoint[0] != null) {
                    Point current = e.getLocationOnScreen();
                    dlg.setLocation(current.x - dragPoint[0].x, current.y - dragPoint[0].y);
                }
            }
        });
    }
}