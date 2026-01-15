package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
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

import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.ui.controllers.ControllerRegistry;
import com.maven.repairshop.ui.controllers.ReparationController;
import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.session.SessionContext;

public class ReparationFormDialog extends JDialog {

    private final SessionContext session;
    private final ReparationController controller = ControllerRegistry.get().reparations();

    private final Color HEADER_COLOR = new Color(44, 185, 152); 
    private final Color BLUE_ACTION = new Color(52, 152, 219); 
    private final Color BG_COLOR = Color.WHITE;

    private boolean saved = false;
    private Reparation created = null;

    private Long selectedClientId = null;
    private JTextField txtClient;
    private JButton btnEnregistrer;

    public ReparationFormDialog(Window owner, SessionContext session) {
        super(owner, "Ajouter réparation", ModalityType.APPLICATION_MODAL);
        this.session = session;
        setUndecorated(true);
        initUi();
    }

    public boolean isSaved() { return saved; }
    public Reparation getCreated() { return created; }

    private void initUi() {
        setSize(550, 320);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());
        getRootPane().setBorder(new LineBorder(new Color(200, 200, 200), 1));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("NOUVELLE RÉPARATION");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
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

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(BG_COLOR);
        body.setBorder(new EmptyBorder(30, 40, 20, 40));

        JPanel clientGroup = new JPanel(new BorderLayout(5, 5));
        clientGroup.setBackground(BG_COLOR);

        JLabel lblClient = new JLabel("Client propriétaire de l'appareil *");
        lblClient.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblClient.setForeground(new Color(100, 100, 100));
        clientGroup.add(lblClient, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BG_COLOR);

        txtClient = new JTextField("Aucun client sélectionné");
        txtClient.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        txtClient.setForeground(Color.GRAY);
        txtClient.setEditable(false);
        txtClient.setBackground(new Color(245, 245, 245));
        txtClient.setBorder(new CompoundBorder(new LineBorder(new Color(200, 200, 200)), new EmptyBorder(8, 10, 8, 10)));

        JButton btnChoisir = createButton("🔍 Choisir...", BLUE_ACTION);
        btnChoisir.setPreferredSize(new Dimension(110, 38));
        btnChoisir.addActionListener(e -> onPickClient());

        inputPanel.add(txtClient, BorderLayout.CENTER);
        inputPanel.add(btnChoisir, BorderLayout.EAST);

        clientGroup.add(inputPanel, BorderLayout.CENTER);
        
        JLabel lblInfo = new JLabel("<html><div style='color:#888; margin-top:5px;'>Sélectionnez le client pour activer l'enregistrement.<br>Un ticket sera généré automatiquement.</div></html>");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clientGroup.add(lblInfo, BorderLayout.SOUTH);

        body.add(clientGroup, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setBackground(BG_COLOR);
        footer.setBorder(new EmptyBorder(0, 20, 25, 20));

        JButton btnAnnuler = createButton("Annuler", new Color(149, 165, 166));
        btnEnregistrer = createButton("Créer Ticket", HEADER_COLOR);
        btnEnregistrer.setEnabled(false);

        btnAnnuler.addActionListener(e -> dispose());
        btnEnregistrer.addActionListener(e -> onSave());

        footer.add(btnAnnuler);
        footer.add(btnEnregistrer);
        add(footer, BorderLayout.SOUTH);
    }

    private void onPickClient() {
        Window w = getOwner();
        ClientPickerDialog dlg = new ClientPickerDialog(w, session);
        dlg.setVisible(true);

        if (!dlg.isSelected()) return;
        Long id = dlg.getPickedId();
        if (id == null) return;

        selectedClientId = id;
        String nom = (dlg.getPicked() != null) ? safe(dlg.getPicked().getNom()) : "Client #" + id;
        txtClient.setText(nom);
        txtClient.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtClient.setForeground(new Color(50, 50, 50));
        txtClient.setBackground(Color.WHITE);
        
        btnEnregistrer.setEnabled(true);
    }

    private void onSave() {
        if (selectedClientId == null) return;
        try {
            controller.creerReparation(this, selectedClientId, session.getReparateurId(), rep -> {
                this.created = rep;
                this.saved = true;
                dispose();
            });
        } catch (Exception ex) {
            this.saved = true;
            dispose();
        }
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 40));
        return btn;
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

    private static String safe(String s) { return s == null ? "" : s; }
}