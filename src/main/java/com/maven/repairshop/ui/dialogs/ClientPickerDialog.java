package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.maven.repairshop.model.Client;
import com.maven.repairshop.ui.controllers.ClientController;
import com.maven.repairshop.ui.session.SessionContext;
import com.maven.repairshop.ui.util.UiServices;

public class ClientPickerDialog extends JDialog {

    private final SessionContext session;

    private final ClientController controller;

    private boolean selected = false;
    private Client picked = null;

    private JTextField txtRecherche;
    private JButton btnRechercher;
    private JButton btnActualiser;

    private JTable table;
    private DefaultTableModel tableModel;

    private JButton btnChoisir;
    private JButton btnAnnuler;

    private Long pickedId = null;

    public ClientPickerDialog(Window owner, SessionContext session) {
        super(owner, "Choisir un client", ModalityType.APPLICATION_MODAL);
        this.session = session;

        // UI-only : via UiServices (mock today / real impl later)
        this.controller = new ClientController(UiServices.get().clients());

        setSize(760, 440);
        setLocationRelativeTo(owner);
        initUi();
        refresh();
    }

    public boolean isSelected() {
        return selected;
    }

    public Client getPicked() {
        return picked;
    }

    public Long getPickedId() {
        return pickedId;
    }

    private void initUi() {
        setLayout(new BorderLayout());

        // Top bar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Recherche:"));

        txtRecherche = new JTextField(18);
        top.add(txtRecherche);

        btnRechercher = new JButton("Rechercher");
        btnActualiser = new JButton("Actualiser");

        top.add(btnRechercher);
        top.add(btnActualiser);

        btnRechercher.addActionListener(e -> refresh());
        btnActualiser.addActionListener(e -> {
            txtRecherche.setText("");
            refresh();
        });

        txtRecherche.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) refresh();
            }
        });

        add(top, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Nom", "Téléphone", "Email", "Ville"},
                0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Hide ID column
        TableColumn idCol = table.getColumnModel().getColumn(0);
        idCol.setMinWidth(0);
        idCol.setMaxWidth(0);
        idCol.setPreferredWidth(0);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnChoisir = new JButton("Choisir");
        btnAnnuler = new JButton("Annuler");

        btnChoisir.addActionListener(e -> onChoisir());
        btnAnnuler.addActionListener(e -> dispose());

        bottom.add(btnChoisir);
        bottom.add(btnAnnuler);

        add(bottom, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnChoisir);

        // Double click = choose
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) onChoisir();
            }
        });
    }

    private void refresh() {
        Long reparateurId = currentReparateurId();
        String q = txtRecherche.getText();
        if (q == null) q = "";
        q = q.trim();

        // Pas de "lister" dans le contract => rechercher("") doit retourner tous
        controller.rechercher(this, q, reparateurId, this::fillTable);
    }

    private void fillTable(List<Client> list) {
        tableModel.setRowCount(0);
        for (Client c : list) {
            tableModel.addRow(new Object[]{
                    c.getId(),
                    safe(c.getNom()),
                    safe(c.getTelephone()),
                    safe(c.getEmail()),
                    safe(c.getVille())
            });
        }
    }

    private void onChoisir() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionne un client.");
            return;
        }

        Long id = selectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Client invalide.");
            return;
        }

        Client c = new Client();
        try {
            c.setNom((String) table.getValueAt(row, 1));
            c.setTelephone((String) table.getValueAt(row, 2));
            c.setEmail((String) table.getValueAt(row, 3));
            c.setVille((String) table.getValueAt(row, 4));
        } catch (Exception ignored) {}

        this.picked = c;
        this.pickedId = id;
        this.selected = true;

        dispose();
    }

    private Long selectedId() {
        int row = table.getSelectedRow();
        Object v = table.getValueAt(row, 0);
        if (v instanceof Long) return (Long) v;
        if (v instanceof Number) return ((Number) v).longValue();
        return null;
    }

    private Long currentReparateurId() {
        try {
            // Chez toi tu utilises déjà session.getReparateurId() dans ClientsPanel
            Long id = session.getReparateurId();
            if (id != null) return id;
        } catch (Exception ignored) {}

        // fallback possible si session expose user
        try {
            var user = session.getUser();
            if (user != null) return user.getId();
        } catch (Exception ignored) {}

        return null;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}