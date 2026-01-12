package com.maven.repairshop.ui.pages;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.maven.repairshop.ui.dialogs.ClientDialog;
import com.maven.repairshop.ui.session.SessionContext;

public class ClientsPanel extends JPanel {

    private final SessionContext session;

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    public ClientsPanel(SessionContext session) {
        this.session = session;
        initUi();
        // plus tard: refresh();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        // Top
        JPanel top = new JPanel(new BorderLayout());

        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(18);
        JButton btnSearch = new JButton("Rechercher");
        search.add(new JLabel("Recherche:"));
        search.add(txtSearch);
        search.add(btnSearch);

        JPanel actionsTop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("+ Ajouter");
        actionsTop.add(btnAdd);

        top.add(search, BorderLayout.CENTER);
        top.add(actionsTop, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(new Object[] {"ID", "Nom", "Téléphone", "Email", "Ville"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom actions
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnEdit = new JButton("Modifier");
        JButton btnDelete = new JButton("Supprimer");
        JButton btnDetail = new JButton("Détail");
        bottom.add(btnEdit);
        bottom.add(btnDelete);
        bottom.add(btnDetail);
        add(bottom, BorderLayout.SOUTH);

        // Events (UI only pour l’instant)
        btnAdd.addActionListener(e -> new ClientDialog(SwingUtilities.getWindowAncestor(this)).setVisible(true));
        btnEdit.addActionListener(e -> JOptionPane.showMessageDialog(this, "Edit client (à brancher service)"));
        btnDelete.addActionListener(e -> JOptionPane.showMessageDialog(this, "Delete client (à brancher service)"));
        btnDetail.addActionListener(e -> JOptionPane.showMessageDialog(this, "Detail client (à coder)"));
        btnSearch.addActionListener(e -> JOptionPane.showMessageDialog(this, "Search (à brancher service)"));
    }
}