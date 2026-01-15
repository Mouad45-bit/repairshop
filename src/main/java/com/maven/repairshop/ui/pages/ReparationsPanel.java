package com.maven.repairshop.ui.pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.model.enums.StatutReparation;
import com.maven.repairshop.ui.controllers.ControllerRegistry;
import com.maven.repairshop.ui.controllers.ReparationController;
import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.dialogs.ReparationDetailDialog;
import com.maven.repairshop.ui.dialogs.ReparationFormDialog;
import com.maven.repairshop.ui.session.SessionContext;

public class ReparationsPanel extends JPanel {

    private final SessionContext session;

    // controller via registry (UI -> ServiceRegistry -> backend)
    private final ReparationController controller = ControllerRegistry.get().reparations();

    // --- Design V2 ---
    private final Color MAIN_COLOR = new Color(44, 185, 152);
    private final Color BG_WHITE = Color.WHITE;
    private final Color GRAY_TEXT = new Color(150, 150, 150);
    private final Color BORDER_LIGHT = new Color(230, 230, 230);

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtRecherche;
    private JComboBox<Object> cbStatut;

    private JButton btnAjouter;
    private JButton btnRechercher;
    private JButton btnActualiser;

    private JButton btnDetail;
    private JButton btnChangerStatut;
    private JButton btnAnnuler;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReparationsPanel(SessionContext session) {
        this.session = session;
        setLayout(new BorderLayout());
        setBackground(BG_WHITE);

        initTopBar();
        initTable();
        initActionsBar();

        refresh(); // charge initial
    }

    // ---------------- UI ----------------

    private void initTopBar() {
        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.setBackground(BG_WHITE);
        panelTop.setBorder(new EmptyBorder(15, 20, 10, 20));

        // gauche : recherche + filtre
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelSearch.setBackground(BG_WHITE);

        JLabel lblRecherche = new JLabel("Recherche:");
        lblRecherche.setForeground(GRAY_TEXT);
        lblRecherche.setFont(new Font("Segoe UI", Font.BOLD, 12));

        txtRecherche = new JTextField(16);
        styleInput(txtRecherche);

        JLabel lblStatut = new JLabel("Statut:");
        lblStatut.setForeground(GRAY_TEXT);
        lblStatut.setFont(new Font("Segoe UI", Font.BOLD, 12));

        cbStatut = new JComboBox<>();
        cbStatut.addItem("Tous");
        for (StatutReparation s : StatutReparation.values()) {
            cbStatut.addItem(s);
        }
        styleCombo(cbStatut);

        btnRechercher = createButton("Rechercher", MAIN_COLOR);
        btnActualiser = createButton("Actualiser", new Color(149, 165, 166));

        panelSearch.add(lblRecherche);
        panelSearch.add(txtRecherche);
        panelSearch.add(lblStatut);
        panelSearch.add(cbStatut);
        panelSearch.add(btnRechercher);
        panelSearch.add(btnActualiser);

        // droite : Ajouter
        JPanel panelRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelRight.setBackground(BG_WHITE);

        btnAjouter = createButton("+ Ajouter", MAIN_COLOR);
        panelRight.add(btnAjouter);

        // events (logique inchangée)
        btnRechercher.addActionListener(e -> refresh());
        btnActualiser.addActionListener(e -> {
            txtRecherche.setText("");
            cbStatut.setSelectedIndex(0);
            refresh();
        });
        cbStatut.addActionListener(e -> refresh());

        txtRecherche.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) refresh();
            }
        });

        btnAjouter.addActionListener(e -> onAjouter());

        panelTop.add(panelSearch, BorderLayout.CENTER);
        panelTop.add(panelRight, BorderLayout.EAST);

        add(panelTop, BorderLayout.NORTH);
    }

    private void initTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Code", "Client", "Statut", "Dernier statut"},
                0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(table);

        TableColumn idCol = table.getColumnModel().getColumn(0);
        idCol.setMinWidth(0);
        idCol.setMaxWidth(0);
        idCol.setPreferredWidth(0);

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(new LineBorder(BORDER_LIGHT, 1));
        add(sp, BorderLayout.CENTER);
    }

    private void initActionsBar() {
        JPanel panelActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelActions.setBackground(BG_WHITE);
        panelActions.setBorder(new EmptyBorder(10, 20, 15, 20));

        btnDetail = createButton("Détail", new Color(149, 165, 166));
        btnChangerStatut = createButton("Changer statut", MAIN_COLOR);
        btnAnnuler = createButton("Annuler", new Color(231, 76, 60));

        btnDetail.addActionListener(e -> onDetail());
        btnChangerStatut.addActionListener(e -> onChangerStatut());
        btnAnnuler.addActionListener(e -> onAnnuler());

        panelActions.add(btnDetail);
        panelActions.add(btnChangerStatut);
        panelActions.add(btnAnnuler);

        add(panelActions, BorderLayout.SOUTH);
    }

    // ---------------- Logic UI ----------------

    private void refresh() {
        Long reparateurId = session.getReparateurId();
        if (reparateurId == null) {
            UiDialogs.warn(this, "Session invalide (réparateur introuvable).");
            return;
        }

        String query = txtRecherche.getText();
        StatutReparation statut = selectedStatutOrNull();

        try {
            controller.rechercher(this, query, reparateurId, statut, this::fillTable);
        } catch (Exception ex) {
            UiDialogs.handle(this, ex);
        }
    }

    private void fillTable(List<Reparation> list) {
        tableModel.setRowCount(0);

        for (Reparation r : list) {
            Long id = r.getId();
            String code = r.getCodeUnique();
            String client = (r.getClient() != null) ? safe(r.getClient().getNom()) : "";
            StatutReparation st = r.getStatut();
            String last = (r.getDateDernierStatut() != null) ? r.getDateDernierStatut().format(DT_FMT) : "";

            tableModel.addRow(new Object[]{ id, code, client, st, last });
        }
    }

    private void onAjouter() {
        try {
            Window w = SwingUtilities.getWindowAncestor(this);
            ReparationFormDialog dlg = new ReparationFormDialog(w, session);
            dlg.setVisible(true);

            if (!dlg.isSaved()) return;

            // Refresh liste
            refresh();

            // Option UX : ouvrir directement le détail de la nouvelle réparation
            Reparation created = dlg.getCreated();
            if (created != null && created.getId() != null) {
                new ReparationDetailDialog(w, session, created.getId()).setVisible(true);
            }
        } catch (Exception ex) {
            UiDialogs.handle(this, ex);
        }
    }

    private void onDetail() {
        Long id = selectedId();
        if (id == null) {
            UiDialogs.warn(this, "Sélectionne une réparation d'abord.");
            return;
        }

        try {
            Window w = SwingUtilities.getWindowAncestor(this);
            new ReparationDetailDialog(w, session, id).setVisible(true);
        } catch (Exception ex) {
            UiDialogs.handle(this, ex);
        }
    }

    private void onChangerStatut() {
        Long id = selectedId();
        if (id == null) {
            UiDialogs.warn(this, "Sélectionne une réparation d'abord.");
            return;
        }

        Object stObj = table.getValueAt(table.getSelectedRow(), 3);
        StatutReparation current = (stObj instanceof StatutReparation) ? (StatutReparation) stObj : null;

        JComboBox<StatutReparation> cb = new JComboBox<>(StatutReparation.values());
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (current != null) cb.setSelectedItem(current);

        int ok = JOptionPane.showConfirmDialog(
                this,
                cb,
                "Nouveau statut",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (ok != JOptionPane.OK_OPTION) return;

        StatutReparation nouveau = (StatutReparation) cb.getSelectedItem();
        if (nouveau == null) return;

        try {
            controller.changerStatut(this, id, nouveau, this::refresh);
        } catch (Exception ex) {
            UiDialogs.handle(this, ex);
        }
    }

    private void onAnnuler() {
        Long id = selectedId();
        if (id == null) {
            UiDialogs.warn(this, "Sélectionne une réparation d'abord.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Confirmer l'annulation ?",
                "Annuler réparation",
                JOptionPane.YES_NO_OPTION
        );
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            controller.changerStatut(this, id, StatutReparation.ANNULEE, this::refresh);
        } catch (Exception ex) {
            UiDialogs.handle(this, ex);
        }
    }

    // ---------------- Helpers ----------------

    private Long selectedId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;

        Object v = table.getValueAt(row, 0);
        if (v instanceof Long) return (Long) v;
        if (v instanceof Number) return ((Number) v).longValue();
        return null;
    }

    private StatutReparation selectedStatutOrNull() {
        Object s = cbStatut.getSelectedItem();
        if (s instanceof StatutReparation) return (StatutReparation) s;
        return null;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
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
        btn.setPreferredSize(new Dimension(140, 38));

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