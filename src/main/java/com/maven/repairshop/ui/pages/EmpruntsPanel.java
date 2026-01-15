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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.maven.repairshop.model.Emprunt;
import com.maven.repairshop.model.enums.StatutEmprunt;
import com.maven.repairshop.model.enums.TypeEmprunt;
import com.maven.repairshop.ui.controllers.ControllerRegistry;
import com.maven.repairshop.ui.controllers.EmpruntController;
import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.dialogs.EmpruntDialog;
import com.maven.repairshop.ui.session.SessionContext;

public class EmpruntsPanel extends JPanel {

    private final SessionContext session;

    // controller via registry (UI -> ServiceRegistry -> backend)
    private final EmpruntController ctrl = ControllerRegistry.get().emprunts();

    // --- Design V2 ---
    private final Color MAIN_COLOR = new Color(44, 185, 152);
    private final Color BG_WHITE = Color.WHITE;
    private final Color GRAY_TEXT = new Color(150, 150, 150);
    private final Color BORDER_LIGHT = new Color(230, 230, 230);

    private JTable table;
    private DefaultTableModel model;

    private JTextField txtSearch;
    private JComboBox<Object> cbType;
    private JComboBox<Object> cbStatut;

    private JLabel lblTotalEmprunts;
    private JLabel lblTotalPrets;

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EmpruntsPanel(SessionContext session) {
        this.session = session;
        initUi();
        refresh();
    }

    private void initUi() {
        setLayout(new BorderLayout());
        setBackground(BG_WHITE);

        // ===== TOP (filtres + ajouter) =====
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG_WHITE);
        top.setBorder(new EmptyBorder(15, 20, 10, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setBackground(BG_WHITE);

        JLabel lblQ = new JLabel("Personne / Motif:");
        lblQ.setForeground(GRAY_TEXT);
        lblQ.setFont(new Font("Segoe UI", Font.BOLD, 12));

        txtSearch = new JTextField(16);
        styleInput(txtSearch);

        JLabel lblType = new JLabel("Type:");
        lblType.setForeground(GRAY_TEXT);
        lblType.setFont(new Font("Segoe UI", Font.BOLD, 12));

        cbType = new JComboBox<>();
        cbType.addItem("Tous");
        for (TypeEmprunt t : TypeEmprunt.values()) cbType.addItem(t);
        styleCombo(cbType);

        JLabel lblStatut = new JLabel("Statut:");
        lblStatut.setForeground(GRAY_TEXT);
        lblStatut.setFont(new Font("Segoe UI", Font.BOLD, 12));

        cbStatut = new JComboBox<>();
        cbStatut.addItem("Tous");
        for (StatutEmprunt s : StatutEmprunt.values()) cbStatut.addItem(s);
        styleCombo(cbStatut);

        JButton btnSearch = createButton("Rechercher", MAIN_COLOR);
        JButton btnRefresh = createButton("Actualiser", new Color(149, 165, 166));

        left.add(lblQ);
        left.add(txtSearch);
        left.add(lblType);
        left.add(cbType);
        left.add(lblStatut);
        left.add(cbStatut);
        left.add(btnSearch);
        left.add(btnRefresh);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(BG_WHITE);

        JButton btnAdd = createButton("+ Ajouter", MAIN_COLOR);
        right.add(btnAdd);

        top.add(left, BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(
                new Object[] { "ID", "Type", "Personne", "Montant", "Date", "Statut", "Motif" }, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(new LineBorder(BORDER_LIGHT, 1));
        add(sp, BorderLayout.CENTER);

        // ===== BOTTOM (totaux + actions) =====
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(BG_WHITE);
        bottom.setBorder(new EmptyBorder(10, 20, 15, 20));

        JPanel totals = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        totals.setBackground(BG_WHITE);

        lblTotalEmprunts = new JLabel("Total emprunts en cours: —");
        lblTotalEmprunts.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotalEmprunts.setForeground(GRAY_TEXT);

        lblTotalPrets = new JLabel("Total prêts en cours: —");
        lblTotalPrets.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotalPrets.setForeground(GRAY_TEXT);

        JLabel sep = new JLabel(" | ");
        sep.setForeground(new Color(180, 180, 180));

        totals.add(lblTotalEmprunts);
        totals.add(sep);
        totals.add(lblTotalPrets);

        bottom.add(totals, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(BG_WHITE);

        JButton btnMarkPaid = createButton("Marquer remboursé", MAIN_COLOR);
        JButton btnDetail = createButton("Détail", new Color(149, 165, 166));
        JButton btnDelete = createButton("Supprimer", new Color(231, 76, 60));

        actions.add(btnDetail);
        actions.add(btnMarkPaid);
        actions.add(btnDelete);

        bottom.add(actions, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        // ===== EVENTS (logique inchangée) =====
        btnAdd.addActionListener(e -> openCreate());
        btnDetail.addActionListener(e -> showDetailSelected());

        btnMarkPaid.addActionListener(e -> {
            Long id = getSelectedId();
            if (id == null) return;

            int ok = JOptionPane.showConfirmDialog(this,
                    "Marquer comme REMBOURSÉ ?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION);

            if (ok == JOptionPane.YES_OPTION) {
                try {
                    ctrl.changerStatut(this, id, StatutEmprunt.REMBOURSE.name(), () -> {
                        UiDialogs.info(this, "Statut mis à jour.");
                        refresh();
                    });
                } catch (Exception ex) {
                    UiDialogs.handle(this, ex);
                }
            }
        });

        btnDelete.addActionListener(e -> {
            Long id = getSelectedId();
            if (id == null) return;

            int ok = JOptionPane.showConfirmDialog(this,
                    "Supprimer cet emprunt/prêt ?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;

            try {
                ctrl.supprimer(this, id, () -> {
                    UiDialogs.info(this, "Supprimé.");
                    refresh();
                });
            } catch (Exception ex) {
                UiDialogs.handle(this, ex);
            }
        });

        btnSearch.addActionListener(e -> refresh());
        txtSearch.addActionListener(e -> refresh());
        cbType.addActionListener(e -> refresh());
        cbStatut.addActionListener(e -> refresh());

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cbType.setSelectedIndex(0);
            cbStatut.setSelectedIndex(0);
            refresh();
        });
    }

    private void openCreate() {
        Long reparateurId = session.getReparateurId();
        if (reparateurId == null) {
            UiDialogs.warn(this, "Cette page est réservée au réparateur.");
            return;
        }

        try {
            // EmpruntDialog requiert (Window, SessionContext)
            EmpruntDialog dlg = new EmpruntDialog(SwingUtilities.getWindowAncestor(this), session);
            dlg.setModeCreate();
            dlg.setVisible(true);

            if (!dlg.isSaved()) return;

            EmpruntDialog.EmpruntFormData data = dlg.getFormData();
            if (data == null) return;

            TypeEmprunt type = "PRET".equalsIgnoreCase(data.type) ? TypeEmprunt.PRET : TypeEmprunt.EMPRUNT;

            // create() n'accepte pas date/statut => statut = EN_COURS par défaut côté service
            ctrl.creer(this, reparateurId, type, data.personne, data.montantStr, data.remarque, created -> {
                UiDialogs.info(this, "Ajout OK.");
                refresh();
            });
        } catch (Exception ex) {
            UiDialogs.handle(this, ex);
        }
    }

    private void refresh() {
        Long reparateurId = session.getReparateurId();
        if (reparateurId == null) {
            UiDialogs.warn(this, "Cette page est réservée au réparateur (session invalide).");
            return;
        }

        try {
            ctrl.lister(this, reparateurId, list -> {
                List<Object[]> rows = toRowsFiltered(list);

                model.setRowCount(0);
                for (Object[] r : rows) model.addRow(r);

                updateTotalsFromRows(rows);
            });
        } catch (Exception ex) {
            UiDialogs.handle(this, ex);
        }
    }

    private List<Object[]> toRowsFiltered(List<Emprunt> list) {
        String q = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        Object typeSel = cbType.getSelectedItem();
        Object statutSel = cbStatut.getSelectedItem();

        TypeEmprunt typeFilter = (typeSel instanceof TypeEmprunt) ? (TypeEmprunt) typeSel : null;
        StatutEmprunt statutFilter = (statutSel instanceof StatutEmprunt) ? (StatutEmprunt) statutSel : null;

        List<Object[]> out = new ArrayList<>();

        for (Emprunt e : list) {
            String personne = safe(e.getNomPersonne());
            String motif = safe(e.getMotif());

            if (!q.isEmpty()) {
                String hay = (personne + " " + motif).toLowerCase();
                if (!hay.contains(q)) continue;
            }

            if (typeFilter != null && e.getType() != typeFilter) continue;
            if (statutFilter != null && e.getStatut() != statutFilter) continue;

            String dt = e.getDateEmprunt() != null ? e.getDateEmprunt().format(DT) : "";

            out.add(new Object[] {
                    e.getId(),
                    e.getType(),
                    personne,
                    e.getMontant(),
                    dt,
                    e.getStatut(),
                    motif
            });
        }

        return out;
    }

    private void updateTotalsFromRows(List<Object[]> rows) {
        double totalEmprunts = 0;
        double totalPrets = 0;

        for (Object[] r : rows) {
            String type = r[1] != null ? r[1].toString() : "";
            String statut = r[5] != null ? r[5].toString() : "";

            // "en cours" = EN_COURS ou PARTIELLEMENT_REMBOURSE
            if (!("EN_COURS".equalsIgnoreCase(statut) || "PARTIELLEMENT_REMBOURSE".equalsIgnoreCase(statut))) {
                continue;
            }

            double montant = 0;
            try {
                montant = Double.parseDouble(r[3].toString().replace(",", "."));
            } catch (Exception ignored) {}

            if ("EMPRUNT".equalsIgnoreCase(type)) totalEmprunts += montant;
            if ("PRET".equalsIgnoreCase(type)) totalPrets += montant;
        }

        lblTotalEmprunts.setText("Total emprunts en cours: " + formatDh(totalEmprunts));
        lblTotalPrets.setText("Total prêts en cours: " + formatDh(totalPrets));
    }

    private void showDetailSelected() {
        Long id = getSelectedId();
        if (id == null) return;

        int row = table.getSelectedRow();
        String type = safe(model.getValueAt(row, 1));
        String personne = safe(model.getValueAt(row, 2));
        String montant = safe(model.getValueAt(row, 3));
        String date = safe(model.getValueAt(row, 4));
        String statut = safe(model.getValueAt(row, 5));
        String motif = safe(model.getValueAt(row, 6));

        UiDialogs.info(this,
                "Emprunt/Prêt #" + id + "\n" +
                        "Type: " + type + "\n" +
                        "Personne: " + personne + "\n" +
                        "Montant: " + montant + "\n" +
                        "Date: " + date + "\n" +
                        "Statut: " + statut + "\n" +
                        "Motif: " + motif);
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

    private String formatDh(double v) {
        if (v == (long) v) return ((long) v) + " DH";
        return String.format("%.2f DH", v);
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
        btn.setPreferredSize(new Dimension(160, 38));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private void styleInput(JTextField txt) {
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 8, 5, 8)
        ));
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(Color.WHITE);
        cb.setBorder(new LineBorder(new Color(200, 200, 200)));
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