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
import com.maven.repairshop.model.enums.StatutReparation;
import com.maven.repairshop.ui.controllers.ControllerRegistry;
import com.maven.repairshop.ui.controllers.ReparationController;
import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.session.SessionContext;

/**
 * - Afficher détail réparation
 * - Modifier le statut depuis le détail (Combo + bouton Enregistrer)
 * - Appels uniquement via ReparationController (contract)
 */
public class ReparationDetailDialog extends JDialog {

    private final SessionContext session;

    // controller via registry (UI -> ServiceRegistry -> backend)
    private final ReparationController controller = ControllerRegistry.get().reparations();

    private final Long reparationId;

    // --- Design V2 ---
    private final Color MAIN_COLOR = new Color(44, 185, 152);
    private final Color BG_WHITE = Color.WHITE;
    private final Color GRAY_TEXT = new Color(150, 150, 150);

    // Affichage
    private JLabel lblId;
    private JLabel lblCode;
    private JLabel lblClient;
    private JLabel lblTelephone;
    private JLabel lblDernierStatut;

    // Statut éditable
    private JComboBox<StatutReparation> cbStatut;
    private JButton btnEnregistrer;

    private JButton btnFermer;

    private Reparation current; // snapshot chargé

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReparationDetailDialog(Window owner, SessionContext session, Long reparationId) {
        super(owner, "Détail réparation", ModalityType.APPLICATION_MODAL);
        this.session = session;
        this.reparationId = reparationId;

        setSize(680, 420);
        setLocationRelativeTo(owner);

        // Style moderne V2
        setUndecorated(true);
        getRootPane().setBorder(new LineBorder(new Color(200, 200, 200), 1));

        initUi();
        loadData();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        // ===== HEADER =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MAIN_COLOR);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("DÉTAIL RÉPARATION");
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

        JPanel card = new JPanel(new GridLayout(6, 2, 12, 10));
        card.setBackground(BG_WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(14, 14, 14, 14)
        ));

        card.add(makeKeyLabel("ID:"));
        lblId = makeValueLabel("-");
        card.add(lblId);

        card.add(makeKeyLabel("Code unique:"));
        lblCode = makeValueLabel("-");
        card.add(lblCode);

        card.add(makeKeyLabel("Client:"));
        lblClient = makeValueLabel("-");
        card.add(lblClient);

        card.add(makeKeyLabel("Téléphone:"));
        lblTelephone = makeValueLabel("-");
        card.add(lblTelephone);

        // Statut (modifiable)
        card.add(makeKeyLabel("Statut:"));
        cbStatut = new JComboBox<>(StatutReparation.values());
        styleCombo(cbStatut);
        card.add(cbStatut);

        card.add(makeKeyLabel("Dernière mise à jour:"));
        lblDernierStatut = makeValueLabel("-");
        card.add(lblDernierStatut);

        body.add(card, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        // ===== FOOTER =====
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG_WHITE);
        footer.setBorder(new EmptyBorder(0, 20, 15, 20));

        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        actions.setBackground(BG_WHITE);

        btnEnregistrer = createButton("Enregistrer", MAIN_COLOR);
        btnFermer = createButton("Fermer", new Color(149, 165, 166));

        btnEnregistrer.setEnabled(false);

        btnEnregistrer.addActionListener(e -> onEnregistrer());
        btnFermer.addActionListener(e -> dispose());

        cbStatut.addActionListener(e -> refreshSaveEnabled());

        actions.add(btnEnregistrer);
        actions.add(btnFermer);

        footer.add(actions, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnEnregistrer);
    }

    private void loadData() {
        controller.trouverParId(this, reparationId, this::fill);
    }

    private void fill(Reparation r) {
        this.current = r;

        lblId.setText(r.getId() != null ? String.valueOf(r.getId()) : "-");
        lblCode.setText(safe(r.getCodeUnique()));

        if (r.getClient() != null) {
            lblClient.setText(safe(r.getClient().getNom()));
            lblTelephone.setText(safe(r.getClient().getTelephone()));
        } else {
            lblClient.setText("");
            lblTelephone.setText("");
        }

        if (r.getStatut() != null) cbStatut.setSelectedItem(r.getStatut());

        lblDernierStatut.setText(
                r.getDateDernierStatut() != null ? r.getDateDernierStatut().format(DT_FMT) : ""
        );

        refreshSaveEnabled();
    }

    private void refreshSaveEnabled() {
        if (current == null) {
            btnEnregistrer.setEnabled(false);
            return;
        }
        StatutReparation selected = (StatutReparation) cbStatut.getSelectedItem();
        btnEnregistrer.setEnabled(selected != null && current.getStatut() != selected);
    }

    private void onEnregistrer() {
        if (current == null) return;

        StatutReparation nouveau = (StatutReparation) cbStatut.getSelectedItem();
        if (nouveau == null) return;

        // Si pas de changement, rien à faire
        if (current.getStatut() == nouveau) {
            UiDialogs.info(this, "Aucun changement de statut.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Confirmer le changement de statut vers : " + nouveau.name() + " ?",
                "Changer statut",
                JOptionPane.YES_NO_OPTION
        );
        if (ok != JOptionPane.YES_OPTION) {
            // remet l'ancien statut (sécurité UI)
            cbStatut.setSelectedItem(current.getStatut());
            refreshSaveEnabled();
            return;
        }

        // Appel contract via controller
        controller.changerStatut(this, reparationId, nouveau, () -> {
            // Recharge après save pour refléter dateDernierStatut, statut, etc.
            loadData();
            UiDialogs.info(this, "Statut mis à jour.");
        });
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

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(Color.WHITE);
        cb.setBorder(new LineBorder(new Color(200, 200, 200)));
        cb.setPreferredSize(new Dimension(220, 32));
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 38));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
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