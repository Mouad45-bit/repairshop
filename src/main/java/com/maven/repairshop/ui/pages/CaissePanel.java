package com.maven.repairshop.ui.pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.maven.repairshop.model.Emprunt;
import com.maven.repairshop.model.enums.TypeEmprunt;
import com.maven.repairshop.ui.controllers.ControllerRegistry;
import com.maven.repairshop.ui.controllers.EmpruntController;
import com.maven.repairshop.ui.controllers.UiDialogs;
import com.maven.repairshop.ui.session.SessionContext;

public class CaissePanel extends JPanel {

    private final SessionContext session;
    private final EmpruntController empruntCtrl = ControllerRegistry.get().emprunts();

    // --- COULEURS FINANCE ---
    private final Color COLOR_INCOME = new Color(39, 174, 96);   
    private final Color COLOR_EXPENSE = new Color(192, 57, 43);  
    private final Color COLOR_BALANCE = new Color(41, 128, 185); 
    private final Color BG_APP = new Color(240, 242, 245);
    private final Color CARD_SHADOW = new Color(200, 200, 200, 80);
    private final Color MAIN_COLOR = new Color(44, 185, 152);

    private JTextField txtDateDebut;
    private JTextField txtDateFin;

    private JTable table;
    private DefaultTableModel model;
    private JLabel lblEntrees, lblSorties, lblSolde;

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public CaissePanel(SessionContext session) {
        this.session = session;
        initUi();
        refresh();
    }

    private void initUi() {
        setLayout(new BorderLayout(25, 25));
        setBackground(BG_APP);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // HEADER
        JPanel topSection = new JPanel(new BorderLayout(0, 20));
        topSection.setOpaque(false);

        JLabel lblTitle = new JLabel("Journal de Caisse");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(40, 40, 40));
        topSection.add(lblTitle, BorderLayout.NORTH);

        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setPreferredSize(new Dimension(0, 100));

        lblEntrees = new JLabel("0.00 DH");
        lblSorties = new JLabel("0.00 DH");
        lblSolde = new JLabel("0.00 DH");

        kpiPanel.add(createKpiCard("TOTAL ENTRÉES", lblEntrees, COLOR_INCOME, "⬆"));
        kpiPanel.add(createKpiCard("TOTAL SORTIES", lblSorties, COLOR_EXPENSE, "⬇"));
        kpiPanel.add(createKpiCard("SOLDE PÉRIODE", lblSolde, COLOR_BALANCE, "∑"));

        topSection.add(kpiPanel, BorderLayout.CENTER);
        add(topSection, BorderLayout.NORTH);

        // TABLEAU
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setOpaque(false);

        txtDateDebut = new JTextField(10); styleInput(txtDateDebut);
        txtDateFin = new JTextField(10); styleInput(txtDateFin);

        JButton btnApply = createButton("Appliquer Filtres", MAIN_COLOR);
        JButton btnReset = createButton("Réinitialiser", new Color(149, 165, 166));

        filterPanel.add(new JLabel("Du:"));
        filterPanel.add(txtDateDebut);
        filterPanel.add(new JLabel("Au:"));
        filterPanel.add(txtDateFin);
        filterPanel.add(btnApply);
        filterPanel.add(btnReset);

        centerPanel.add(filterPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[] { "DATE", "TYPE", "CATÉGORIE", "DESCRIPTION", "MONTANT" }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        styleTable(table);
        table.getColumnModel().getColumn(1).setCellRenderer(new FinanceBadgeRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new MontantRenderer());

        ShadowPanel tableContainer = new ShadowPanel();
        tableContainer.setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tableContainer.add(scroll, BorderLayout.CENTER);

        centerPanel.add(tableContainer, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // EVENTS
        btnApply.addActionListener(e -> refresh());
        btnReset.addActionListener(e -> { txtDateDebut.setText(""); txtDateFin.setText(""); refresh(); });
    }

    private void refresh() {
        Long repId = session.getReparateurId();
        if (repId == null) return;
        LocalDate from = parseDateOrNull(txtDateDebut.getText());
        LocalDate to = parseDateOrNull(txtDateFin.getText());
        try {
            empruntCtrl.lister(this, repId, list -> fillFromEmprunts(list, from, to));
        } catch (Exception ex) { UiDialogs.handle(this, ex); }
    }

    private void fillFromEmprunts(List<Emprunt> list, LocalDate from, LocalDate to) {
        model.setRowCount(0);
        double tIn = 0, tOut = 0;
        for (Emprunt e : list) {
            LocalDateTime dt = e.getDateEmprunt();
            LocalDate d = (dt != null) ? dt.toLocalDate() : null;
            if (d != null) {
                if (from != null && d.isBefore(from)) continue;
                if (to != null && d.isAfter(to)) continue;
            }
            boolean isIn = (e.getType() == TypeEmprunt.EMPRUNT);
            String type = isIn ? "ENTREE" : "SORTIE";
            String dateStr = (dt != null) ? dt.format(DT) : "";
            double m = Math.abs(e.getMontant());
            
            model.addRow(new Object[] { dateStr, type, e.getType(), safe(e.getNomPersonne()), (isIn ? "+" : "-") + formatDh(m) });
            if (isIn) tIn += m; else tOut += m;
        }
        lblEntrees.setText(formatDh(tIn)); lblSorties.setText(formatDh(tOut)); lblSolde.setText(formatDh(tIn - tOut));
    }

    // --- UTILS ---
    private JPanel createKpiCard(String t, JLabel v, Color c, String i) {
        JPanel p = new JPanel(new BorderLayout(10, 5)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setColor(c); g2.fillRoundRect(0,0,8,getHeight(),20,20); g2.fillRect(5,0,5,getHeight());
            }
        };
        p.setOpaque(false); p.setBorder(new EmptyBorder(15, 25, 15, 15));
        JLabel lt = new JLabel(t); lt.setFont(new Font("Segoe UI", Font.BOLD, 12)); lt.setForeground(Color.GRAY);
        v.setFont(new Font("Segoe UI", Font.BOLD, 28)); v.setForeground(new Color(50,50,50));
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false); top.add(lt, BorderLayout.WEST);
        p.add(top, BorderLayout.NORTH); p.add(v, BorderLayout.CENTER);
        return p;
    }
    
    private void styleTable(JTable t) {
        t.setRowHeight(45); t.setShowVerticalLines(false); t.setShowHorizontalLines(true);
        t.setGridColor(new Color(240,240,240)); t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setSelectionBackground(new Color(235,248,245)); t.setSelectionForeground(Color.BLACK);
        ((DefaultTableCellRenderer)t.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
    }
    
    class ShadowPanel extends JPanel {
        public ShadowPanel() { setOpaque(false); setBorder(new EmptyBorder(5,5,10,5)); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE); g2.fillRoundRect(5,5,getWidth()-10,getHeight()-15,15,15);
            g2.setColor(CARD_SHADOW); g2.drawRoundRect(5,5,getWidth()-10,getHeight()-15,15,15);
            super.paintChildren(g);
        }
    }
    
    class FinanceBadgeRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            String val = (v!=null)?v.toString():""; 
            Color bg = val.contains("ENTREE") ? new Color(213,245,227) : new Color(250,219,216);
            Color fg = val.contains("ENTREE") ? COLOR_INCOME : COLOR_EXPENSE;
            JLabel l = new JLabel(val); l.setOpaque(true); l.setBackground(bg); l.setForeground(fg);
            l.setHorizontalAlignment(CENTER); l.setFont(new Font("Segoe UI", Font.BOLD, 11));
            return l;
        }
    }
    
    class MontantRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            Component cmp = super.getTableCellRendererComponent(t,v,s,f,r,c);
            String val = (v!=null)?v.toString():"";
            cmp.setForeground(val.contains("+") ? COLOR_INCOME : (val.contains("-") ? COLOR_EXPENSE : Color.BLACK));
            return cmp;
        }
    }

    private JButton createButton(String t, Color c) {
        JButton b = new JButton(t); b.setBackground(c); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13)); b.setFocusPainted(false); b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(8,15,8,15)); return b;
    }
    private void styleInput(JTextField t) { t.setBorder(BorderFactory.createLineBorder(new Color(220,220,220))); }
    private String formatDh(double v) { return String.format("%.2f DH", v); }
    private String safe(String s) { return s==null?"":s; }
    private LocalDate parseDateOrNull(String s) { try { return LocalDate.parse(s); } catch(Exception e) { return null; } }
}