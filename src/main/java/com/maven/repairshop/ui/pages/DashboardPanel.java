package com.maven.repairshop.ui.pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants; // <--- C'est cette ligne qui manquait !

import com.maven.repairshop.ui.session.SessionContext;

/**
 * DashboardPanel - Design Moderne "Card Style".
 * Utilise du Custom Painting pour les ombres et les coins arrondis.
 */
public class DashboardPanel extends JPanel {

    private final SessionContext session;

    // Couleurs
    private final Color BG_COLOR = new Color(240, 242, 245); // Doit matcher le MainFrame
    private final Color TEXT_TITLE = new Color(100, 100, 100);
    private final Color TEXT_VALUE = new Color(44, 185, 152); // Vert Diprella

    private JLabel lblReparations;
    private JLabel lblClients;
    private JLabel lblCaisse;
    private JLabel lblEmprunts;

    public DashboardPanel(SessionContext session) {
        this.session = session;
        initUi();
    }

    private void initUi() {
        // Le fond du panneau global
        setBackground(BG_COLOR);
        setLayout(new BorderLayout(20, 20)); // Espace autour
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- EN-TETE ---
        JLabel title = new JLabel("Vue d'ensemble");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(50, 50, 50));
        add(title, BorderLayout.NORTH);

        // --- GRILLE DE CARTES ---
        // GridLayout avec espace (hgap, vgap) de 20px
        JPanel grid = new JPanel(new GridLayout(2, 2, 20, 20));
        grid.setBackground(BG_COLOR); // Important pour la transparence

        // On crée les cartes avec des icônes (emoji pour l'instant, ou unicode)
        lblReparations = new JLabel("12"); // Valeur démo
        grid.add(createCard("Réparations en cours", "🛠", lblReparations));

        lblClients = new JLabel("45");
        grid.add(createCard("Clients enregistrés", "👥", lblClients));

        lblCaisse = new JLabel("1 250 €");
        grid.add(createCard("Caisse du jour", "💰", lblCaisse));

        lblEmprunts = new JLabel("3");
        grid.add(createCard("Prêts actifs", "handshake", lblEmprunts)); 

        add(grid, BorderLayout.CENTER);
    }

    private JPanel createCard(String title, String icon, JLabel valueLabel) {
        ModernCard card = new ModernCard();
        card.setLayout(new BorderLayout());
        
        // Haut de la carte (Icone + Titre)
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24)); // Police Emoji supportée
        lblIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        
        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTitle.setForeground(TEXT_TITLE);
        
        top.add(lblIcon, BorderLayout.WEST);
        top.add(lblTitle, BorderLayout.CENTER);
        
        // Centre de la carte (La grosse valeur)
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 40));
        valueLabel.setForeground(TEXT_VALUE);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT); 
        
        // Assemblage
        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        // Petit texte en bas (Fake stat)
        JLabel lblSub = new JLabel("Mise à jour à l'instant");
        lblSub.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblSub.setForeground(Color.GRAY);
        card.add(lblSub, BorderLayout.SOUTH);

        return card;
    }

    // --- LA MAGIE DU DESIGN (CLASSE INTERNE) ---
    /**
     * Un JPanel personnalisé qui dessine une ombre et des coins arrondis.
     */
    private static class ModernCard extends JPanel {
        
        public ModernCard() {
            setOpaque(false); // Important: on dessine nous-mêmes le fond
            setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25)); // Padding interne
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            // Active l'anti-aliasing pour que les courbes soient lisses
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int arc = 25; // Rayon des coins arrondis
            int shadowSize = 5;

            // 1. Dessiner l'ombre
            g2.setColor(new Color(200, 200, 200, 50)); 
            g2.fillRoundRect(shadowSize, shadowSize, width - shadowSize * 2, height - shadowSize, arc, arc);

            // 2. Dessiner le fond blanc par-dessus
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, width - shadowSize, height - shadowSize, arc, arc);
            
            super.paintChildren(g); 
        }
    }
}