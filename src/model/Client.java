package model;

public class Client {
    private String nom;
    private String telephone;
    private String codeUnique;   // Le code que le client utilise pour suivre sa réparation
    private Utilisateur createur; // Le Réparateur qui a saisi ce client

    public Client(String nom, String telephone, String codeUnique, Utilisateur createur) {
        this.nom = nom;
        this.telephone = telephone;
        this.codeUnique = codeUnique;
        this.createur = createur;
    }

    public String getNom() { return nom; }
    public String getCodeUnique() { return codeUnique; }
    public Utilisateur getCreateur() { return createur; }

    @Override
    public String toString() {
        return "Client: " + nom + " (Code: " + codeUnique + ")";
    }
}