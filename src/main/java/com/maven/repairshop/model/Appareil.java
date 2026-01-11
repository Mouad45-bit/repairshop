package com.maven.repairshop.model;

import com.maven.repairshop.model.base.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name="appareils")
public class Appareil extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String imei;

    @Column(nullable=false)
    private String typeAppareil; // téléphone, PC...

    private String description;

    @ManyToOne
    @JoinColumn(name="reparation_id", nullable=false)
    private Reparation reparation;

    public String getImei() { return imei; }
    public void setImei(String imei) { this.imei = imei; }

    public String getTypeAppareil() { return typeAppareil; }
    public void setTypeAppareil(String typeAppareil) { this.typeAppareil = typeAppareil; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Reparation getReparation() { return reparation; }
    public void setReparation(Reparation reparation) { this.reparation = reparation; }
}