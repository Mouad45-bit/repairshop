package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.ui.controllers.ControllerRegistry;
import com.maven.repairshop.ui.controllers.SuiviController;
import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.session.SessionContext;

public class SuiviDialog extends JDialog {

    private final SessionContext session;

    // controller via registry (UI -> ServiceRegistry -> backend)
    private final SuiviController controller = ControllerRegistry.get().suivi();

    // --- Design V2 ---
    private final Color MAIN_COLOR = new Color(44, 185, 152);
    private final Color BG_WHITE = Color.WHITE;
    private final Color GRAY_TEXT = new Color(150, 150, 150);

    private JTextField txtCode;
    private JButton btnSuivre;
    private JButton btnFermer;

    private JLabel lblCode;
    private JLabel lblClient;
    private JLabel lblStatut;
    private JLabel lblDate;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public SuiviDialog(Window owner, SessionContext session) {
        super(owner, "Suivi réparation", ModalityType.APPLICATION_MODAL);
        this.session = session;

        setSize(560, 320);
        setLocationRelativeTo(owner);

        // Style moderne V2
        setUndecorated(true);
        getRootPane().setBorder(new LineBorder(new Color(200, 200, 200), 1));

        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        // ===== HEADER =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MAIN_COLOR);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("SUIVI RÉPARATION");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);

        JLabel lblClose = new JLabel("✕");
        lblClose.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblClose.setForeground(Color.WHITE);
        lblClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblClose.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dispose(); }
        });
        header.add(lblClose, BorderLayout.EAST);

        makeDraggable(header);
        add(header, BorderLayout.NORTH);

        // ===== BODY =====
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(BG_WHITE);
        body.setBorder(new EmptyBorder(18, 20, 10, 20));

        // Top: input (dans le body)
        JPanel inputRow = new JPanel(new BorderLayout(10, 0));
        inputRow.setBackground(BG_WHITE);

        JPanel left = new JPanel(new BorderLayout(8, 0));
        left.setBackground(BG_WHITE);

        JLabel lbl = new JLabel("Code unique");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(GRAY_TEXT);

        txtCode = new JTextField();
        styleInput(txtCode);

        left.add(lbl, BorderLayout.NORTH);
        left.add(txtCode, BorderLayout.CENTER);

        btnSuivre = createButton("Suivre", MAIN_COLOR);
        btnSuivre.setPreferredSize(new Dimension(120, 38));

        inputRow.add(left, BorderLayout.CENTER);
        inputRow.add(btnSuivre, BorderLayout.EAST);

        body.add(inputRow, BorderLayout.NORTH);

        // Center: result (grid)
        JPanel center = new JPanel(new GridLayout(4, 2, 12, 10));
        center.setBackground(BG_WHITE);
        center.setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(14, 14, 14, 14)
        ));

        center.add(makeKeyLabel("Code:"));
        lblCode = makeValueLabel("-");
        center.add(lblCode);

        center.add(makeKeyLabel("Client:"));
        lblClient = makeValueLabel("-");
        center.add(lblClient);

        center.add(makeKeyLabel("Statut:"));
        lblStatut = makeValueLabel("-");
        center.add(lblStatut);

        center.add(makeKeyLabel("Dernière mise à jour:"));
        lblDate = makeValueLabel("-");
        center.add(lblDate);

        body.add(center, BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);

        // ===== FOOTER =====
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG_WHITE);
        footer.setBorder(new EmptyBorder(0, 20, 15, 20));

        JPanel right = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        right.setBackground(BG_WHITE);

        btnFermer = createButton("Fermer", new Color(149, 165, 166));
        btnFermer.setPreferredSize(new Dimension(120, 38));

        right.add(btnFermer);
        footer.add(right, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);

        // ===== EVENTS (logique inchangée) =====
        btnSuivre.addActionListener(e -> onSuivre());
        btnFermer.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnSuivre);
    }

    private void onSuivre() {
        String code = txtCode.getText();

        Long reparateurId = session.getReparateurId();
        if (reparateurId == null) {
            UiDialogs.warn(this, "Session invalide (réparateur introuvable).");
            return;
        }

        controller.suivre(this, code, reparateurId, this::showResult);
    }

    private void showResult(Reparation r) {
        lblCode.setText(safe(r.getCodeUnique()));
        lblClient.setText(r.getClient() != null ? safe(r.getClient().getNom()) : "");
        lblStatut.setText(r.getStatut() != null ? r.getStatut().name() : "");
        lblDate.setText(r.getDateDernierStatut() != null ? r.getDateDernierStatut().format(DT_FMT) : "");
    }

    private JLabel makeKeyLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(GRAY_TEXT);
        return l;
    }

    private JLabel makeValueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(Color.BLACK);
        return l;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 38));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private void styleInput(JTextField txt) {
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    private void makeDraggable(JPanel handle) {
        final Point[] dragPoint = { null };
        handle.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragPoint[0] = e.getPoint(); }
        });
        handle.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (dragPoint[0] == null) return;
                Point current = e.getLocationOnScreen();
                setLocation(current.x - dragPoint[0].x, current.y - dragPoint[0].y);
            }
        });
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}