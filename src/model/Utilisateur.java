package model;

public class Utilisateur {
    private Long id;
    private String login;       // Son identifiant de connexion
    private String password;    // Son mot de passe
    private Role role;          // Est-il Patron ou Employé ?
    private Boutique boutique;  // Dans quelle boutique il travaille ?

    // Le Constructeur
    public Utilisateur(Long id, String login, String password, Role role, Boutique boutique) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.role = role;
        this.boutique = boutique;
    }

    // Les Getters (Lecture seule)
    public String getLogin() {
        return login;
    }

    public Role getRole() {
        return role;
    }

    public Boutique getBoutique() {
        return boutique;
    }
    
    // Pour l'affichage
    @Override
    public String toString() {
        // On affiche le login et son rôle.
        // Si il a une boutique, on affiche le nom de la boutique.
        String nomBoutique = "Aucune";
        if (this.boutique != null) {
            nomBoutique = this.boutique.getNom();
        }
        return "Utilisateur [" + login + "], Role: " + role + ", Travaille chez: " + nomBoutique;
    }
}