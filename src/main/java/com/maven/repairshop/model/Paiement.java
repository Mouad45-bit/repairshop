package com.maven.repairshop.model;

import com.maven.repairshop.model.base.BaseEntity;
import com.maven.repairshop.model.enums.TypePaiement;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="paiements")
public class Paiement extends BaseEntity {

    @Column(nullable=false)
    private LocalDateTime datePaiement = LocalDateTime.now();

    @Column(nullable=false)
    private double montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private TypePaiement typePaiement;

    @ManyToOne
    @JoinColumn(name="reparation_id", nullable=false)
    private Reparation reparation;

    public LocalDateTime getDatePaiement() { return datePaiement; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public TypePaiement getTypePaiement() { return typePaiement; }
    public void setTypePaiement(TypePaiement typePaiement) { this.typePaiement = typePaiement; }

    public Reparation getReparation() { return reparation; }
    public void setReparation(Reparation reparation) { this.reparation = reparation; }
}