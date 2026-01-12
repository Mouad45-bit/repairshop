package model;

import java.time.LocalDate;

public class Paiement {
    private Long id;
    private double montant;
    private LocalDate datePaiement;
    private String type;         // On écrira "AVANCE" ou "SOLDE" simplement
    private Reparation reparation; // Pour quelle réparation on paie ?

    public Paiement(Long id, double montant, String type, Reparation reparation) {
        this.id = id;
        this.montant = montant;
        this.type = type;
        this.reparation = reparation;
        this.datePaiement = LocalDate.now();
    }

    // Getters
    public double getMontant() { return montant; }
    public String getType() { return type; }
    public Reparation getReparation() { return reparation; }

    @Override
    public String toString() {
        return "Paiement [" + type + "] de " + montant + " DH pour dossier n°" + reparation.getId();
    }
}