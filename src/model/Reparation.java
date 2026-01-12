package model;

import java.time.LocalDate; // Pour gérer la date

public class Reparation {
    private Long id;
    private String description;  // Ex: "Ecran cassé iPhone X"
    private LocalDate dateDepot; // Date d'aujourd'hui
    private Statut statut;       // En cours, Terminé...
    private double prix;         // Prix estimé
    private Client client;       // A qui appartient cette réparation ?

    public Reparation(Long id, String description, double prix, Client client) {
        this.id = id;
        this.description = description;
        this.prix = prix;
        this.client = client;
        // Par défaut, quand on crée une réparation, elle est EN_ATTENTE et à la date d'aujourd'hui
        this.statut = Statut.EN_ATTENTE;
        this.dateDepot = LocalDate.now();
    }

    // Getters
    public Long getId() { return id; }
    public String getDescription() { return description; }
    public Statut getStatut() { return statut; }
    public double getPrix() { return prix; }
    public Client getClient() { return client; }

    // Setter (car le statut va changer au fil du temps)
    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Reparation [" + statut + "] : " + description + " pour " + client.getNom();
    }
}