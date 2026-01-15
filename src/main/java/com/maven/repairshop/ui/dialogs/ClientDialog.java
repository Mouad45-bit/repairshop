package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.maven.repairshop.ui.controllers.UiDialogs;

public class ClientDialog extends JDialog {

    public enum Mode { CREATE, EDIT }

    // DTO pour récupérer les données saisies
    public static final class ClientFormData {
        public final String nom;
        public final String telephone;
        public final String email;
        public final String adresse;
        public final String ville;

        public ClientFormData(String nom, String telephone, String email, String adresse, String ville) {
            this.nom = nom;
            this.telephone = telephone;
            this.email = email;
            this.adresse = adresse;
            this.ville = ville;
        }
    }

    // --- DESIGN ---
    private final Color HEADER_COLOR = new Color(44, 185, 152); // Vert Diprella
    private final Color BG_COLOR = Color.WHITE;
    private final Color TEXT_LABEL = new Color(100, 100, 100);

    private JTextField txtNom;
    private JTextField txtTel;
    private JTextField txtEmail;
    private JTextField txtAdresse;
    private JTextField txtVille;
    private JLabel lblTitle;

    private boolean saved = false;
    private Mode mode = Mode.CREATE;
    private Long clientId = null;
    private ClientFormData formData = null;

    public ClientDialog(Window owner) {
        super(owner, "Client", ModalityType.APPLICATION_MODAL);
        setUndecorated(true); // Design Moderne sans bordure
        initUi();
        setModeCreate(); // Par défaut
    }

    // ===== UI MODERNE =====

    private void initUi() {
        setSize(480, 500);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        // Bordure grise fine autour de la fenêtre
        getRootPane().setBorder(new LineBorder(new Color(200, 200, 200), 1));

        // 1. HEADER (Vert)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        lblTitle = new JLabel("NOUVEAU CLIENT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);

        // Bouton Fermer (X)
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

        // 2. FORMULAIRE (Fond Blanc)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(5, 1, 0, 15)); // 5 champs avec espacement
        formPanel.setBackground(BG_COLOR);
        formPanel.setBorder(new EmptyBorder(25, 30, 15, 30));

        txtNom = createTextField();
        txtTel = createTextField();
        txtEmail = createTextField();
        txtAdresse = createTextField();
        txtVille = createTextField();

        formPanel.add(createFieldGroup("Nom complet *", txtNom));
        formPanel.add(createFieldGroup("Téléphone *", txtTel));
        formPanel.add(createFieldGroup("Email", txtEmail));
        formPanel.add(createFieldGroup("Adresse", txtAdresse));
        formPanel.add(createFieldGroup("Ville", txtVille));

        add(formPanel, BorderLayout.CENTER);

        // 3. ACTIONS (Boutons en bas)
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottom.setBackground(BG_COLOR);
        bottom.setBorder(new EmptyBorder(0, 20, 25, 20));

        JButton btnCancel = createButton("Annuler", new Color(149, 165, 166));
        JButton btnSave = createButton("Enregistrer", HEADER_COLOR);

        btnCancel.addActionListener(e -> {
            saved = false;
            formData = null;
            dispose();
        });

        btnSave.addActionListener(e -> onSave());

        bottom.add(btnCancel);
        bottom.add(btnSave);
        add(bottom, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnSave);
    }

    // --- HELPERS DESIGN ---

    private JPanel createFieldGroup(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(BG_COLOR);
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_LABEL);
        
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Bordure composée : Ligne grise + Padding interne
        tf.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200)), 
            new EmptyBorder(8, 10, 8, 10)
        ));
        return tf;
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
        // Effet Hover
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
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

    // ===== LOGIQUE MÉTIER (Inchangée) =====

    public boolean isSaved() { return saved; }
    public Mode getMode() { return mode; }
    public Long getClientId() { return clientId; }
    public ClientFormData getFormData() { return formData; }

    public void setModeCreate() {
        this.mode = Mode.CREATE;
        this.clientId = null;
        lblTitle.setText("AJOUTER UN CLIENT");
        clearFields();
        this.saved = false;
        this.formData = null;
    }

    public void setModeEdit(Long clientId, String nom, String tel, String email, String adresse, String ville) {
        this.mode = Mode.EDIT;
        this.clientId = clientId;
        lblTitle.setText("MODIFIER CLIENT");

        txtNom.setText(nvl(nom));
        txtTel.setText(nvl(tel));
        txtEmail.setText(nvl(email));
        txtAdresse.setText(nvl(adresse));
        txtVille.setText(nvl(ville));

        this.saved = false;
        this.formData = null;
    }

    private void onSave() {
        String nom = txtNom.getText().trim();
        String tel = txtTel.getText().trim();

        if (nom.isEmpty()) {
            UiDialogs.warn(this, "Le nom est obligatoire.");
            return;
        }
        if (tel.isEmpty()) {
            UiDialogs.warn(this, "Le téléphone est obligatoire.");
            return;
        }

        this.formData = new ClientFormData(nom, tel, txtEmail.getText().trim(), txtAdresse.getText().trim(), txtVille.getText().trim());
        this.saved = true;
        dispose();
    }

    private void clearFields() {
        txtNom.setText(""); txtTel.setText(""); txtEmail.setText(""); 
        txtAdresse.setText(""); txtVille.setText("");
    }
    private static String nvl(String s) { return s == null ? "" : s; }
}