package model;

public class Boutique {
    // --- 1. Les attributs (Les informations de l'objet) ---
    private Long id;      // L'identifiant unique (ex: Boutique n°1)
    private String nom;   // Le nom (ex: "Boutique Centre-Ville")

    // --- 2. Le Constructeur (Pour créer l'objet) ---
    public Boutique(Long id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    // --- 3. Les Getters (Pour lire les infos, car elles sont 'private') ---
    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }
    
    // Une méthode pratique pour afficher joliment l'objet
    @Override
    public String toString() {
        return "Boutique: " + nom + " (ID: " + id + ")";
    }
}