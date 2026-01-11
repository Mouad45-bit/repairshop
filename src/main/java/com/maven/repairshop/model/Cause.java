package com.maven.repairshop.model;

import com.maven.repairshop.model.base.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name="causes")
public class Cause extends BaseEntity {

    @Column(nullable=false)
    private String typeCause;

    @Column(columnDefinition = "TEXT")
    private String description;

    private double coutAvance;
    private double coutRestant;

    @ManyToOne
    @JoinColumn(name="reparation_id", nullable=false)
    private Reparation reparation;

    public String getTypeCause() { return typeCause; }
    public void setTypeCause(String typeCause) { this.typeCause = typeCause; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getCoutAvance() { return coutAvance; }
    public void setCoutAvance(double coutAvance) { this.coutAvance = coutAvance; }

    public double getCoutRestant() { return coutRestant; }
    public void setCoutRestant(double coutRestant) { this.coutRestant = coutRestant; }

    public Reparation getReparation() { return reparation; }
    public void setReparation(Reparation reparation) { this.reparation = reparation; }
}