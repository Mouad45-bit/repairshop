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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.session.SessionContext;

public class ReparationDetailDialog extends JDialog {

    private final SessionContext session;
    private final Long reparationId;

    private final Color HEADER_COLOR = new Color(44, 185, 152);
    private final Color BG_COLOR = Color.WHITE;
    private final Color SECTION_BG = new Color(250, 250, 250);
    private final Color LABEL_GRAY = new Color(120, 120, 120);

    private JLabel lblTitle, lblClientName, lblClientTel, lblTicketRef, lblDateDepot, lblAppareil, lblPanne, lblLastUpdate;
    private JComboBox<String> cbStatut;
    private JButton btnEnregistrer;
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ReparationDetailDialog(Window owner, SessionContext session, Long reparationId) {
        super(owner, "Détail réparation", ModalityType.APPLICATION_MODAL);
        this.session = session;
        this.reparationId = reparationId;
        setUndecorated(true);
        initUi();
        fillMockData();
    }

    private void initUi() {
        setSize(700, 480);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());
        getRootPane().setBorder(new LineBorder(new Color(200, 200, 200), 1));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        lblTitle = new JLabel("FICHE TECHNIQUE");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);

        JLabel lblClose = new JLabel("✕");
        lblClose.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblClose.setForeground(Color.WHITE);
        lblClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblClose.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { dispose(); } });
        header.add(lblClose, BorderLayout.EAST);
        makeDraggable(header);
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.setBackground(BG_COLOR);

        JPanel infoPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        infoPanel.setBackground(BG_COLOR);
        infoPanel.setBorder(new EmptyBorder(20, 25, 10, 25));

        JPanel pClient = createSectionPanel("INFORMATIONS CLIENT");
        lblClientName = createValueLabel("-");
        lblClientTel = createValueLabel("-");
        pClient.add(createLabel("Nom Complet:")); pClient.add(lblClientName);
        pClient.add(createLabel("Téléphone:")); pClient.add(lblClientTel);
        infoPanel.add(pClient);

        JPanel pTicket = createSectionPanel("DÉTAIL TICKET");
        lblTicketRef = createValueLabel("-");
        lblDateDepot = createValueLabel("-");
        pTicket.add(createLabel("Référence:")); pTicket.add(lblTicketRef);
        pTicket.add(createLabel("Date Dépôt:")); pTicket.add(lblDateDepot);
        infoPanel.add(pTicket);

        body.add(infoPanel, BorderLayout.NORTH);

        JPanel techPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        techPanel.setBackground(BG_COLOR);
        techPanel.setBorder(new EmptyBorder(10, 25, 20, 25));

        JPanel pAppareil = new JPanel(new BorderLayout(10, 5));
        pAppareil.setBackground(SECTION_BG);
        pAppareil.setBorder(new EmptyBorder(15, 15, 15, 15));
        lblAppareil = new JLabel("Appareil Inconnu");
        lblAppareil.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblAppareil.setForeground(new Color(50, 50, 50));
        lblPanne = new JLabel("Description de la panne...");
        lblPanne.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblPanne.setForeground(new Color(100, 100, 100));
        pAppareil.add(lblAppareil, BorderLayout.NORTH);
        pAppareil.add(lblPanne, BorderLayout.CENTER);
        techPanel.add(pAppareil);

        JPanel pAction = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        pAction.setBackground(Color.WHITE);
        pAction.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220,220,220)), "Mise à jour du Statut"));
        cbStatut = new JComboBox<>(new String[] { "EN_ATTENTE", "EN_COURS", "TERMINE", "LIVRE", "ANNULE" });
        cbStatut.setPreferredSize(new Dimension(200, 35));
        lblLastUpdate = new JLabel("Dernière maj: -");
        pAction.add(cbStatut);
        pAction.add(lblLastUpdate);
        techPanel.add(pAction);

        body.add(techPanel, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setBackground(BG_COLOR);
        footer.setBorder(new EmptyBorder(0, 20, 20, 20));
        JButton btnFermer = createButton("Fermer", new Color(149, 165, 166));
        btnEnregistrer = createButton("Enregistrer Modifications", HEADER_COLOR);
        footer.add(btnFermer);
        footer.add(btnEnregistrer);
        add(footer, BorderLayout.SOUTH);

        btnFermer.addActionListener(e -> dispose());
        btnEnregistrer.addActionListener(e -> onEnregistrer());
    }

    private void fillMockData() {
        lblTitle.setText("DÉTAIL TICKET #Mock-" + reparationId);
        lblClientName.setText("Ahmed Benali");
        lblClientTel.setText("0661123456");
        lblTicketRef.setText("REP-2024-001");
        lblDateDepot.setText("14/01/2026");
        lblAppareil.setText("Samsung Galaxy S21 Ultra");
        lblPanne.setText("Écran fissuré + Problème connecteur de charge.");
        lblLastUpdate.setText("Màj: " + LocalDateTime.now().format(DT_FMT));
        cbStatut.setSelectedItem("EN_COURS");
    }

    private void onEnregistrer() {
        UiDialogs.info(this, "Statut mis à jour (Simulation) !");
        dispose();
    }

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel(new GridLayout(4, 1, 0, 2));
        p.setBackground(SECTION_BG);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lTitle.setForeground(HEADER_COLOR);
        p.add(lTitle);
        return p;
    }
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(LABEL_GRAY);
        return l;
    }
    private JLabel createValueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(40, 40, 40));
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
        btn.setPreferredSize(new Dimension(180, 40));
        return btn;
    }
    private void makeDraggable(JPanel handle) {
        final Point[] dragPoint = {null};
        handle.addMouseListener(new MouseAdapter() { public void mousePressed(MouseEvent e) { dragPoint[0] = e.getPoint(); } });
        handle.addMouseMotionListener(new MouseAdapter() { public void mouseDragged(MouseEvent e) { if (dragPoint[0] != null) { Point current = e.getLocationOnScreen(); setLocation(current.x - dragPoint[0].x, current.y - dragPoint[0].y); } } });
    }
}