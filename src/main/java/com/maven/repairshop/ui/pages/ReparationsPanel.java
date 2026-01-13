package com.maven.repairshop.ui.pages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.model.enums.StatutReparation;
import com.maven.repairshop.ui.controllers.ControllerRegistry;
import com.maven.repairshop.ui.controllers.ReparationController;
import com.maven.repairshop.ui.dialogs.ReparationDetailDialog;
import com.maven.repairshop.ui.dialogs.ReparationFormDialog;
import com.maven.repairshop.ui.session.SessionContext;

public class ReparationsPanel extends JPanel {

    private final SessionContext session;

    // controller via registry (UI -> ServiceRegistry -> backend)
    private final ReparationController controller = ControllerRegistry.get().reparations();

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

        initTopBar();
        initTable();
        initActionsBar();

        refresh(); // charge initial
    }

    // ---------------- UI ----------------

    private void initTopBar() {
        JPanel panelTop = new JPanel(new BorderLayout());

        // gauche : recherche + filtre
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtRecherche = new JTextField(16);

        cbStatut = new JComboBox<>();
        cbStatut.addItem("Tous");
        for (StatutReparation s : StatutReparation.values()) {
            cbStatut.addItem(s);
        }

        btnRechercher = new JButton("Rechercher");
        btnActualiser = new JButton("Actualiser");

        panelSearch.add(new JLabel("Recherche:"));
        panelSearch.add(txtRecherche);
        panelSearch.add(new JLabel("Statut:"));
        panelSearch.add(cbStatut);
        panelSearch.add(btnRechercher);
        panelSearch.add(btnActualiser);

        // droite : Ajouter
        JPanel panelRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAjouter = new JButton("+ Ajouter");
        panelRight.add(btnAjouter);

        // events
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

        TableColumn idCol = table.getColumnModel().getColumn(0);
        idCol.setMinWidth(0);
        idCol.setMaxWidth(0);
        idCol.setPreferredWidth(0);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void initActionsBar() {
        JPanel panelActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnDetail = new JButton("Détail");
        btnChangerStatut = new JButton("Changer statut");
        btnAnnuler = new JButton("Annuler");

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
            JOptionPane.showMessageDialog(this, "Session invalide (réparateur introuvable).");
            return;
        }

        String query = txtRecherche.getText();
        StatutReparation statut = selectedStatutOrNull();

        controller.rechercher(this, query, reparateurId, statut, this::fillTable);
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
    }

    private void onDetail() {
        Long id = selectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Sélectionne une réparation d'abord.");
            return;
        }

        Window w = SwingUtilities.getWindowAncestor(this);
        new ReparationDetailDialog(w, session, id).setVisible(true);
    }

    private void onChangerStatut() {
        Long id = selectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Sélectionne une réparation d'abord.");
            return;
        }

        Object stObj = table.getValueAt(table.getSelectedRow(), 3);
        StatutReparation current = (stObj instanceof StatutReparation) ? (StatutReparation) stObj : null;

        JComboBox<StatutReparation> cb = new JComboBox<>(StatutReparation.values());
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

        controller.changerStatut(this, id, nouveau, this::refresh);
    }

    private void onAnnuler() {
        Long id = selectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Sélectionne une réparation d'abord.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Confirmer l'annulation ?",
                "Annuler réparation",
                JOptionPane.YES_NO_OPTION
        );
        if (ok != JOptionPane.YES_OPTION) return;

        controller.changerStatut(this, id, StatutReparation.ANNULEE, this::refresh);
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
}