package model;

public class Utilisateur {
    private Long id;
    private String login;
    private String password;    
    private Role role;
    private Boutique boutique; 

    public Utilisateur(Long id, String login, String password, Role role, Boutique boutique) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.role = role;
        this.boutique = boutique;
    }

    // --- LES GETTERS (Ce qu'il manquait) ---
    
    public Long getId() { 
        return id; 
    }

    public String getLogin() {
        return login;
    }

    // C'est celle-ci qui manquait pour corriger ton erreur !
    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public Boutique getBoutique() {
        return boutique;
    }
    
    @Override
    public String toString() {
        String nomBoutique = "Aucune";
        if (this.boutique != null) {
            nomBoutique = this.boutique.getNom();
        }
        return "Utilisateur [" + login + "], Role: " + role + ", Travaille chez: " + nomBoutique;
    }
}