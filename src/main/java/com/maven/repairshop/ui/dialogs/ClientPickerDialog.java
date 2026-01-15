package com.maven.repairshop.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
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
import com.maven.repairshop.ui.session.SessionContext;

public class ClientPickerDialog extends JDialog {

    private final SessionContext session;
    private final ClientController controller;

    // --- COULEURS DESIGN ---
    private final Color HEADER_COLOR = new Color(44, 185, 152); // Vert Diprella
    private final Color BG_COLOR = Color.WHITE;

    private boolean selected = false;
    private Client picked = null;
    private Long pickedId = null;

    private JTextField txtRecherche;
    private JTable table;
    private DefaultTableModel tableModel;

    public ClientPickerDialog(Window owner, SessionContext session) {
        super(owner, "Choisir un client", ModalityType.APPLICATION_MODAL);
        this.session = session;
        this.controller = ControllerRegistry.get().clients();

        setUndecorated(true); // Design moderne sans bordure Windows
        initUi();
        refresh();
    }

    // --- API PUBLIQUE ---
    public boolean isSelected() { return selected; }
    public Client getPicked() { return picked; }
    public Long getPickedId() { return pickedId; }

    // --- UI MODERNE ---
    private void initUi() {
        setSize(800, 500);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        // Bordure fine grise
        getRootPane().setBorder(new LineBorder(new Color(200, 200, 200), 1));

        // 1. HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("SÉLECTIONNER UN CLIENT");
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

        // 2. CORPS
        JPanel body = new JPanel(new BorderLayout(0, 15));
        body.setBackground(BG_COLOR);
        body.setBorder(new EmptyBorder(20, 20, 5, 20));

        // Barre de recherche
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBar.setBackground(BG_COLOR);

        txtRecherche = new JTextField(25);
        styleInput(txtRecherche);

        JButton btnRechercher = createButton("Rechercher", HEADER_COLOR);
        JButton btnActualiser = createButton("Actualiser", new Color(149, 165, 166));

        searchBar.add(new JLabel("Recherche:"));
        searchBar.add(txtRecherche);
        searchBar.add(btnRechercher);
        searchBar.add(btnActualiser);

        body.add(searchBar, BorderLayout.NORTH);

        // Tableau
        tableModel = new DefaultTableModel(new Object[]{"ID", "NOM", "TÉLÉPHONE", "EMAIL", "VILLE"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);

        // Cacher ID
        TableColumn idCol = table.getColumnModel().getColumn(0);
        idCol.setMinWidth(0); idCol.setMaxWidth(0); idCol.setPreferredWidth(0);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        body.add(scroll, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        // 3. FOOTER
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footer.setBackground(BG_COLOR);
        footer.setBorder(new EmptyBorder(15, 20, 20, 20));

        JButton btnAnnuler = createButton("Annuler", new Color(149, 165, 166));
        JButton btnChoisir = createButton("Valider Sélection", HEADER_COLOR);
        btnChoisir.setPreferredSize(new Dimension(160, 40));

        footer.add(btnAnnuler);
        footer.add(btnChoisir);
        add(footer, BorderLayout.SOUTH);

        // --- EVENTS ---
        btnRechercher.addActionListener(e -> refresh());
        btnActualiser.addActionListener(e -> { txtRecherche.setText(""); refresh(); });

        txtRecherche.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) refresh(); }
        });

        btnAnnuler.addActionListener(e -> dispose());
        btnChoisir.addActionListener(e -> onChoisir());

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) onChoisir(); }
        });
    }

    // --- LOGIQUE METIER ---

    private void refresh() {
        Long repId = session.getReparateurId();
        if (repId == null) return;

        String q = txtRecherche.getText() == null ? "" : txtRecherche.getText().trim();

        try {
            controller.rechercher(this, q, repId, this::fillTable);
        } catch (Exception ex) {
            // IMPORTANT: ne pas masquer l'erreur avec du mock
            UiDialogs.handle(this, ex);
            tableModel.setRowCount(0); // optionnel: vider la table
        }
    }

    private void fillTable(List<Client> list) {
        tableModel.setRowCount(0);
        for (Client c : list) {
            tableModel.addRow(new Object[]{
                    c.getId(), safe(c.getNom()), safe(c.getTelephone()), safe(c.getEmail()), safe(c.getVille())
            });
        }
    }

    private void onChoisir() {
        int row = table.getSelectedRow();
        if (row < 0) { UiDialogs.warn(this, "Veuillez sélectionner un client."); return; }

        Long id = selectedId();
        if (id == null) return;

        Client c = new Client();
        try {
            // On construit un objet "léger" pour affichage, et on stocke l'ID dans pickedId
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

    // --- HELPERS DESIGN ---

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
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBackground(Color.WHITE);
                l.setForeground(new Color(150, 150, 150));
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(240, 240, 240)));
                return l;
            }
        });
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 38));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
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

    private static String safe(String s) { return s == null ? "" : s; }
}