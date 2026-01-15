package com.maven.repairshop.ui.pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import com.maven.repairshop.model.Client;
import com.maven.repairshop.ui.controllers.ClientController;
import com.maven.repairshop.ui.controllers.ControllerRegistry;
import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.dialogs.ClientDialog;
import com.maven.repairshop.ui.session.SessionContext;

public class ClientsPanel extends JPanel {

    private final SessionContext session;

    // Controller récupéré depuis le registre (branché sur ServiceRegistry -> backend)
    private final ClientController ctrl = ControllerRegistry.get().clients();

    // --- Design V2 ---
    private final Color MAIN_COLOR = new Color(44, 185, 152);
    private final Color BG_WHITE = Color.WHITE;
    private final Color GRAY_TEXT = new Color(150, 150, 150);
    private final Color BORDER_LIGHT = new Color(230, 230, 230);

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    public ClientsPanel(SessionContext session) {
        this.session = session;
        initUi();
        refresh();
    }

    private void initUi() {
        setLayout(new BorderLayout());
        setBackground(BG_WHITE);

        // ===== TOP =====
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG_WHITE);
        top.setBorder(new EmptyBorder(15, 20, 10, 20));

        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        search.setBackground(BG_WHITE);

        txtSearch = new JTextField(18);
        styleInput(txtSearch);

        JButton btnSearch = createButton("Rechercher", MAIN_COLOR);
        JButton btnRefresh = createButton("Actualiser", new Color(149, 165, 166));

        JLabel lblSearch = new JLabel("Recherche:");
        lblSearch.setForeground(GRAY_TEXT);
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));

        search.add(lblSearch);
        search.add(txtSearch);
        search.add(btnSearch);
        search.add(btnRefresh);

        JPanel actionsTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsTop.setBackground(BG_WHITE);

        JButton btnAdd = createButton("+ Ajouter", MAIN_COLOR);
        actionsTop.add(btnAdd);

        top.add(search, BorderLayout.CENTER);
        top.add(actionsTop, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(new Object[] { "ID", "Nom", "Téléphone", "Email", "Adresse", "Ville" }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(table);

        // cacher colonne ID
        TableColumn idCol = table.getColumnModel().getColumn(0);
        idCol.setMinWidth(0);
        idCol.setMaxWidth(0);
        idCol.setPreferredWidth(0);

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(new LineBorder(BORDER_LIGHT, 1));
        add(sp, BorderLayout.CENTER);

        // ===== BOTTOM =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(BG_WHITE);
        bottom.setBorder(new EmptyBorder(10, 20, 15, 20));

        JButton btnEdit = createButton("Modifier", MAIN_COLOR);
        JButton btnDelete = createButton("Supprimer", new Color(231, 76, 60));
        JButton btnDetail = createButton("Détail", new Color(149, 165, 166));

        bottom.add(btnDetail);
        bottom.add(btnEdit);
        bottom.add(btnDelete);

        add(bottom, BorderLayout.SOUTH);

        // ===== EVENTS (LOGIQUE INCHANGÉE) =====
        btnSearch.addActionListener(e -> refresh());
        txtSearch.addActionListener(e -> refresh());

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            refresh();
        });

        btnAdd.addActionListener(e -> createClient());
        btnEdit.addActionListener(e -> editClient());
        btnDelete.addActionListener(e -> deleteClient());
        btnDetail.addActionListener(e -> showDetail());
    }

    private void refresh() {
        Long reparateurId = session.getReparateurId();
        if (reparateurId == null) {
            UiDialogs.warn(this, "Cette page est réservée au réparateur (session invalide).");
            return;
        }

        String q = txtSearch.getText() == null ? "" : txtSearch.getText().trim();

        // Contract backend: rechercher(query, reparateurId)
        try {
            ctrl.rechercher(this, q, reparateurId, this::fillTable);
        } catch (Exception ex) {
            UiDialogs.handle(this, ex);
        }
    }

    private void fillTable(java.util.List<Client> list) {
        model.setRowCount(0);
        for (Client c : list) {
            model.addRow(new Object[] {
                    c.getId(),
                    safe(c.getNom()),
                    safe(c.getTelephone()),
                    safe(c.getEmail()),
                    safe(c.getAdresse()),
                    safe(c.getVille())
            });
        }
    }

    private void createClient() {
        Long reparateurId = session.getReparateurId();
        if (reparateurId == null) {
            UiDialogs.warn(this, "Cette page est réservée au réparateur.");
            return;
        }

        try {
            ClientDialog dlg = new ClientDialog(SwingUtilities.getWindowAncestor(this));
            dlg.setModeCreate();
            dlg.setVisible(true);

            if (!dlg.isSaved()) return;

            ClientDialog.ClientFormData data = dlg.getFormData();
            if (data == null) return;

            ctrl.creer(this,
                    data.nom, data.telephone, data.email, data.adresse, data.ville,
                    reparateurId,
                    created -> {
                        UiDialogs.info(this, "Client ajouté.");
                        refresh();
                    });
        } catch (Exception ex) {
            UiDialogs.handle(this, ex);
        }
    }

    private void editClient() {
        Long id = getSelectedId();
        if (id == null) return;

        int row = table.getSelectedRow();
        String oldNom = safe(model.getValueAt(row, 1));
        String oldTel = safe(model.getValueAt(row, 2));
        String oldEmail = safe(model.getValueAt(row, 3));
        String oldAdresse = safe(model.getValueAt(row, 4));
        String oldVille = safe(model.getValueAt(row, 5));

        try {
            ClientDialog dlg = new ClientDialog(SwingUtilities.getWindowAncestor(this));
            dlg.setModeEdit(id, oldNom, oldTel, oldEmail, oldAdresse, oldVille);
            dlg.setVisible(true);

            if (!dlg.isSaved()) return;

            ClientDialog.ClientFormData data = dlg.getFormData();
            if (data == null) return;

            ctrl.modifier(this, id,
                    data.nom, data.telephone, data.email, data.adresse, data.ville,
                    () -> {
                        UiDialogs.info(this, "Client modifié.");
                        refresh();
                    });
        } catch (Exception ex) {
            UiDialogs.handle(this, ex);
        }
    }

    private void deleteClient() {
        Long id = getSelectedId();
        if (id == null) return;

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Supprimer ce client ?\n(Interdit si le client a des réparations, selon règles métier.)",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            ctrl.supprimer(this, id, () -> {
                UiDialogs.info(this, "Client supprimé.");
                refresh();
            });
        } catch (Exception ex) {
            UiDialogs.handle(this, ex);
        }
    }

    private void showDetail() {
        Long id = getSelectedId();
        if (id == null) return;

        int row = table.getSelectedRow();
        String nom = safe(model.getValueAt(row, 1));
        String tel = safe(model.getValueAt(row, 2));
        String email = safe(model.getValueAt(row, 3));
        String adresse = safe(model.getValueAt(row, 4));
        String ville = safe(model.getValueAt(row, 5));

        UiDialogs.info(this,
                "Client #" + id + "\n" +
                        "Nom: " + nom + "\n" +
                        "Téléphone: " + tel + "\n" +
                        "Email: " + email + "\n" +
                        "Adresse: " + adresse + "\n" +
                        "Ville: " + ville + "\n\n" +
                        "(Plus tard: ouvrir un vrai écran détail + historique réparations.)");
    }

    private Long getSelectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiDialogs.warn(this, "Sélectionne une ligne d'abord.");
            return null;
        }
        Object v = model.getValueAt(row, 0);
        if (v == null) return null;
        try {
            return Long.valueOf(v.toString());
        } catch (Exception ex) {
            return null;
        }
    }

    private String safe(Object o) {
        return o == null ? "" : o.toString();
    }

    // ====== Helpers style (copiés du style V2) ======

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
                new EmptyBorder(5, 8, 5, 8)
        ));
    }

    private void styleTable(JTable t) {
        t.setRowHeight(40);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(240, 240, 240));
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setSelectionBackground(new Color(235, 248, 245));
        t.setSelectionForeground(Color.BLACK);

        JTableHeader header = t.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBackground(Color.WHITE);
                l.setForeground(GRAY_TEXT);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(240, 240, 240)));
                return l;
            }
        });
    }
}