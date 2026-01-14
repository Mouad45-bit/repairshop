package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.session.SessionContext; // <--- Import ajouté

public class SuiviDialog extends JDialog {

    private final Color HEADER_COLOR = new Color(44, 185, 152);
    private JTextField txtCode;

    // --- CONSTRUCTEUR CORRIGÉ ---
    // On ajoute 'SessionContext session' ici pour que MainFrame soit content
    public SuiviDialog(Window owner, SessionContext session) {
        super(owner, "Suivi Réparation", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        initUi();
    }

    private void initUi() {
        setSize(450, 250);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());
        getRootPane().setBorder(new LineBorder(new Color(200, 200, 200), 1));

        // 1. HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JLabel lblTitle = new JLabel("SUIVI PAR CODE");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);
        
        JLabel lblClose = new JLabel("✕");
        lblClose.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblClose.setForeground(Color.WHITE);
        lblClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblClose.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dispose(); }
        });
        header.add(lblClose, BorderLayout.EAST);
        makeDraggable(header);
        add(header, BorderLayout.NORTH);

        // 2. CORPS
        JPanel body = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 25));
        body.setBackground(Color.WHITE);
        
        JLabel lblInstruct = new JLabel("Entrez le code unique du ticket (ex: REP-123)");
        lblInstruct.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblInstruct.setForeground(Color.GRAY);
        body.add(lblInstruct);

        txtCode = new JTextField(20);
        txtCode.setFont(new Font("Segoe UI", Font.BOLD, 22));
        txtCode.setHorizontalAlignment(JTextField.CENTER);
        txtCode.setForeground(new Color(50, 50, 50));
        txtCode.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200)), 
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        txtCode.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if(e.getKeyCode()==KeyEvent.VK_ENTER) doSearch(); }
        });
        body.add(txtCode);

        add(body, BorderLayout.CENTER);

        // 3. FOOTER
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(0, 0, 30, 0));

        JButton btnSearch = new JButton("Vérifier le Statut");
        btnSearch.setBackground(HEADER_COLOR);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.setFocusPainted(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setPreferredSize(new Dimension(220, 45));
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.addActionListener(e -> doSearch());

        footer.add(btnSearch);
        add(footer, BorderLayout.SOUTH);
    }

    private void doSearch() {
        String code = txtCode.getText().trim();
        if (code.isEmpty()) { UiDialogs.warn(this, "Veuillez entrer un code."); return; }

        // Simulation Design
        String msg = "🔎 RÉSULTAT DU SUIVI\n\n" +
                     "Ticket : " + code.toUpperCase() + "\n" +
                     "Appareil : Samsung S21 (Simulation)\n" +
                     "STATUT : ⚡ EN COURS DE RÉPARATION\n\n" +
                     "(Simulation Design : Backend non connecté)";
        
        UiDialogs.info(this, msg);
    }

    private void makeDraggable(JPanel handle) {
        final Point[] dragPoint = {null};
        handle.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragPoint[0] = e.getPoint(); }
        });
        handle.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragPoint[0] != null) {
                    Point current = e.getLocationOnScreen();
                    setLocation(current.x - dragPoint[0].x, current.y - dragPoint[0].y);
                }
            }
        });
    }
}