package com.maven.repairshop.model;

import com.maven.repairshop.model.base.BaseEntity;
import com.maven.repairshop.model.enums.StatutEmprunt;
import com.maven.repairshop.model.enums.TypeEmprunt;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="emprunts")
public class Emprunt extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private TypeEmprunt type;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private StatutEmprunt statut = StatutEmprunt.EN_COURS;

    @Column(nullable=false)
    private String nomPersonne;

    @Column(nullable=false)
    private double montant;

    private String motif;

    @Column(nullable=false)
    private LocalDateTime dateEmprunt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name="reparateur_id", nullable=false)
    private Reparateur reparateur;

    public TypeEmprunt getType() { return type; }
    public void setType(TypeEmprunt type) { this.type = type; }

    public StatutEmprunt getStatut() { return statut; }
    public void setStatut(StatutEmprunt statut) { this.statut = statut; }

    public String getNomPersonne() { return nomPersonne; }
    public void setNomPersonne(String nomPersonne) { this.nomPersonne = nomPersonne; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public LocalDateTime getDateEmprunt() { return dateEmprunt; }

    public Reparateur getReparateur() { return reparateur; }
    public void setReparateur(Reparateur reparateur) { this.reparateur = reparateur; }
}