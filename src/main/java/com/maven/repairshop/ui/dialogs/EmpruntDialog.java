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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.session.SessionContext; // Import Important

public class EmpruntDialog extends JDialog {

    public enum Mode { CREATE, EDIT }

    // DTO
    public static final class EmpruntFormData {
        public final String type;
        public final String personne;
        public final String montantStr;
        public final String dateStr;
        public final String statut;
        public final String remarque;

        public EmpruntFormData(String type, String personne, String montantStr, String dateStr, String statut, String remarque) {
            this.type = type;
            this.personne = personne;
            this.montantStr = montantStr;
            this.dateStr = dateStr;
            this.statut = statut;
            this.remarque = remarque;
        }
    }

    // --- DESIGN ---
    private final Color HEADER_COLOR = new Color(44, 185, 152);
    private final Color BG_COLOR = Color.WHITE;
    private final Color TEXT_LABEL = new Color(100, 100, 100);

    private final SessionContext session; // Champ session

    private boolean saved = false;
    private Mode mode = Mode.CREATE;
    private Long editId = null;
    private EmpruntFormData formData = null;

    private JLabel lblTitle;
    private JComboBox<String> cbType;
    private JTextField txtPersonne;
    private JTextField txtMontant;
    private JTextField txtDate;
    private JComboBox<String> cbStatut;
    private JTextArea txtRemarque;

    // --- CONSTRUCTEUR MIS À JOUR ---
    public EmpruntDialog(Window owner, SessionContext session) {
        super(owner, "Emprunt / Prêt", ModalityType.APPLICATION_MODAL);
        this.session = session; // On récupère la session ici
        setUndecorated(true);
        initUi();
        setModeCreate();
    }

    // --- RESTE DU CODE (UI & LOGIQUE) ---

    public boolean isSaved() { return saved; }
    public Mode getMode() { return mode; }
    public Long getEditId() { return editId; }
    public EmpruntFormData getFormData() { return formData; }

    public void setModeCreate() {
        this.mode = Mode.CREATE;
        this.editId = null;
        this.saved = false;
        this.formData = null;
        lblTitle.setText("NOUVEAU MOUVEMENT");
        cbType.setSelectedIndex(0);
        cbStatut.setSelectedItem("EN_COURS");
        txtPersonne.setText("");
        txtMontant.setText("");
        txtDate.setText(""); 
        txtRemarque.setText("");
    }

    public void setModeEdit(Long id, String type, String personne, String montant, String date, String statut, String remarque) {
        this.mode = Mode.EDIT;
        this.editId = id;
        this.saved = false;
        this.formData = null;
        lblTitle.setText("MODIFIER #" + id);
        cbType.setSelectedItem(type != null ? type : "EMPRUNT");
        cbStatut.setSelectedItem(statut != null ? statut : "EN_COURS");
        txtPersonne.setText(personne != null ? personne : "");
        txtMontant.setText(montant != null ? montant : "");
        txtDate.setText(date != null ? date : "");
        txtRemarque.setText(remarque != null ? remarque : "");
    }

    private void initUi() {
        setSize(500, 580);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());
        getRootPane().setBorder(new LineBorder(new Color(200, 200, 200), 1));

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        lblTitle = new JLabel("NOUVEAU MOUVEMENT");
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

        // BODY
        JPanel body = new JPanel();
        body.setLayout(new GridLayout(6, 1, 0, 15));
        body.setBackground(BG_COLOR);
        body.setBorder(new EmptyBorder(25, 30, 15, 30));

        cbType = createComboBox(new String[] { "EMPRUNT (Entrée Argent)", "PRET (Sortie Argent)" });
        txtPersonne = createTextField();
        txtMontant = createTextField();
        txtDate = createTextField();
        cbStatut = createComboBox(new String[] { "EN_COURS", "PARTIELLEMENT_REMBOURSE", "REMBOURSE" });
        
        txtRemarque = new JTextArea();
        txtRemarque.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollRemarque = new JScrollPane(txtRemarque);
        scrollRemarque.setBorder(new LineBorder(new Color(200, 200, 200)));

        body.add(createFieldGroup("Type d'opération *", cbType));
        body.add(createFieldGroup("Personne concernée *", txtPersonne));
        body.add(createFieldGroup("Montant (DH) *", txtMontant));
        body.add(createFieldGroup("Date (AAAA-MM-JJ)", txtDate));
        body.add(createFieldGroup("Statut actuel *", cbStatut));
        
        JPanel pRem = new JPanel(new BorderLayout(5, 5));
        pRem.setBackground(BG_COLOR);
        JLabel lblRem = new JLabel("Remarque (Optionnel)");
        lblRem.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRem.setForeground(TEXT_LABEL);
        pRem.add(lblRem, BorderLayout.NORTH);
        pRem.add(scrollRemarque, BorderLayout.CENTER);
        body.add(pRem);

        add(body, BorderLayout.CENTER);

        // FOOTER
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setBackground(BG_COLOR);
        footer.setBorder(new EmptyBorder(10, 20, 25, 20));

        JButton btnCancel = createButton("Annuler", new Color(149, 165, 166));
        JButton btnSave = createButton("Enregistrer", HEADER_COLOR);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        add(footer, BorderLayout.SOUTH);
    }

    private void onSave() {
        String typeRaw = cbType.getSelectedItem() != null ? cbType.getSelectedItem().toString() : "EMPRUNT";
        String type = typeRaw.startsWith("PRET") ? "PRET" : "EMPRUNT";
        String statut = cbStatut.getSelectedItem() != null ? cbStatut.getSelectedItem().toString() : "EN_COURS";
        String personne = txtPersonne.getText().trim();
        String montantStr = txtMontant.getText().trim();
        String date = txtDate.getText().trim();
        String remarque = txtRemarque.getText().trim();

        if (personne.isEmpty()) { UiDialogs.warn(this, "Le nom est obligatoire."); return; }
        if (montantStr.isEmpty()) { UiDialogs.warn(this, "Le montant est obligatoire."); return; }

        this.formData = new EmpruntFormData(type, personne, montantStr, date, statut, remarque);
        this.saved = true;
        dispose();
    }

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
        tf.setBorder(new CompoundBorder(new LineBorder(new Color(200, 200, 200)), new EmptyBorder(8, 10, 8, 10)));
        return tf;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cb.setBackground(Color.WHITE);
        return cb;
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
}