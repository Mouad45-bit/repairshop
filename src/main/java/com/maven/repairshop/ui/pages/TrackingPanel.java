package com.maven.repairshop.ui.pages;

import com.maven.repairshop.service.SuiviService;
import com.maven.repairshop.service.dto.SuiviDTO;
import com.maven.repairshop.service.exceptions.BusinessException;
import com.maven.repairshop.service.impl.SuiviServiceImpl;
import com.maven.repairshop.ui.util.UiDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class TrackingPanel extends JPanel {

    private final SuiviService suiviService = new SuiviServiceImpl();

    private JTextField txtCode;
    private JButton btnSearch;

    private JLabel lblStatut;
    private JLabel lblDerniereMaj;
    private JLabel lblReste;

    private JPanel resultCard;

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TrackingPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        JPanel top = new JPanel(new BorderLayout(10, 10));

        JLabel title = new JLabel("Suivi de réparation");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JLabel hint = new JLabel("Entrez votre code unique pour consulter le statut et le reste à payer.");
        hint.setForeground(new Color(90, 90, 90));

        JPanel left = new JPanel(new BorderLayout(0, 6));
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(hint, BorderLayout.SOUTH);

        top.add(left, BorderLayout.WEST);
        top.add(buildSearchBar(), BorderLayout.EAST);

        return top;
    }

    private JComponent buildSearchBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);

        txtCode = new JTextField(18);
        txtCode.setToolTipText("Ex: REP-AB12CD34");
        btnSearch = new JButton("Rechercher");

        btnSearch.addActionListener(e -> search());
        txtCode.addActionListener(e -> search()); // Enter

        p.add(new JLabel("Code:"));
        p.add(txtCode);
        p.add(btnSearch);
        return p;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setOpaque(false);

        resultCard = new JPanel(new GridBagLayout());
        resultCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(14, 14, 14, 14)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);
        c.anchor = GridBagConstraints.WEST;

        lblStatut = value("-");
        lblDerniereMaj = value("-");
        lblReste = value("-");

        addRow(resultCard, c, 0, "Statut actuel", lblStatut);
        addRow(resultCard, c, 1, "Dernière mise à jour", lblDerniereMaj);
        addRow(resultCard, c, 2, "Reste à payer", lblReste);

        JPanel empty = new JPanel(new BorderLayout());
        empty.setOpaque(false);

        JLabel tip = new JLabel("<html><b>Astuce:</b> le code unique est fourni par votre réparateur.</html>");
        tip.setForeground(new Color(90, 90, 90));
        empty.add(tip, BorderLayout.NORTH);

        body.add(empty, BorderLayout.NORTH);
        body.add(resultCard, BorderLayout.CENTER);

        return body;
    }

    private void addRow(JPanel card, GridBagConstraints c, int row, String label, JLabel value) {
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        JLabel l = new JLabel(label + " :");
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        card.add(l, c);

        c.gridx = 1; c.gridy = row; c.weightx = 1;
        card.add(value, c);
    }

    private JLabel value(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(new Color(60, 60, 60));
        l.setFont(l.getFont().deriveFont(Font.BOLD, 16f));
        return l;
    }

    private void search() {
        btnSearch.setEnabled(false);
        try {
            String code = txtCode.getText().trim();
            if (code.isEmpty()) {
                UiDialogs.error(this, "Veuillez saisir un code unique.");
                return;
            }

            SuiviDTO dto = suiviService.suivreParCode(code);

            // Affichage
            String statut = dto.getStatut() != null ? dto.getStatut().name() : "-";
            lblStatut.setText(statut);

            if (dto.getDateDernierStatut() != null) {
                lblDerniereMaj.setText(dto.getDateDernierStatut().format(DT));
            } else {
                lblDerniereMaj.setText("-");
            }

            lblReste.setText(String.format("%.2f", dto.getResteAPayer()));

        } catch (BusinessException ex) {
            UiDialogs.error(this, ex.getMessage());
            clearResult();
        } catch (Exception ex) {
            UiDialogs.error(this, "Erreur : " + ex.getMessage());
            clearResult();
        } finally {
            btnSearch.setEnabled(true);
        }
    }

    private void clearResult() {
        lblStatut.setText("-");
        lblDerniereMaj.setText("-");
        lblReste.setText("-");
    }
}