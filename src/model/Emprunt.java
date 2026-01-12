package model;

import java.time.LocalDate;

public class Emprunt {
    private Long id;
    private String materiel;      // Ex: "Chargeur iPhone", "Smartphone de prêt"
    private String nomEmprunteur; // Le nom du client qui emprunte
    private LocalDate dateEmprunt;
    private LocalDate dateRetour; // Null tant que ce n'est pas rendu
    private Utilisateur reparateur; // Celui qui a validé le prêt (pour savoir la boutique)

    public Emprunt(Long id, String materiel, String nomEmprunteur, Utilisateur reparateur) {
        this.id = id;
        this.materiel = materiel;
        this.nomEmprunteur = nomEmprunteur;
        this.reparateur = reparateur;
        this.dateEmprunt = LocalDate.now(); // Date d'aujourd'hui
    }

    // Méthode simple pour dire "C'est rendu !"
    public void marquerCommeRendu() {
        this.dateRetour = LocalDate.now();
    }

    public boolean estRendu() {
        return dateRetour != null;
    }

    // Getters
    public Long getId() { return id; }
    public String getMateriel() { return materiel; }
    public String getNomEmprunteur() { return nomEmprunteur; }
    public Utilisateur getReparateur() { return reparateur; }
    
    @Override
    public String toString() {
        String etat = (dateRetour == null) ? "EN COURS" : "RENDU le " + dateRetour;
        return "Emprunt [" + materiel + "] par " + nomEmprunteur + " (" + etat + ")";
    }
}