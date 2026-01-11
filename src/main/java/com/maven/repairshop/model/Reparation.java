package com.maven.repairshop.model;

import com.maven.repairshop.model.base.BaseEntity;
import com.maven.repairshop.model.enums.StatutReparation;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="reparations")
public class Reparation extends BaseEntity {

    @Column(nullable=false, unique=true)
    private String codeUnique;

    @Column(nullable=false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private StatutReparation statut = StatutReparation.ENREGISTREE;

    @Column(nullable=false)
    private LocalDateTime dateDernierStatut = LocalDateTime.now();

    @Column(columnDefinition="TEXT")
    private String commentaireTechnique;

    @ManyToOne
    @JoinColumn(name="client_id", nullable=false)
    private Client client;

    @OneToMany(mappedBy="reparation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Paiement> paiements = new ArrayList<>();

    @OneToMany(mappedBy="reparation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appareil> appareils = new ArrayList<>();

    public String getCodeUnique() { return codeUnique; }
    public void setCodeUnique(String codeUnique) { this.codeUnique = codeUnique; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public StatutReparation getStatut() { return statut; }
    public void setStatut(StatutReparation statut) { this.statut = statut; }

    public LocalDateTime getDateDernierStatut() { return dateDernierStatut; }
    public void setDateDernierStatut(LocalDateTime dateDernierStatut) { this.dateDernierStatut = dateDernierStatut; }

    public String getCommentaireTechnique() { return commentaireTechnique; }
    public void setCommentaireTechnique() { this.commentaireTechnique = commentaireTechnique; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public List<Paiement> getPaiements() { return paiements; }
    public List<Appareil> getAppareils() { return appareils; }

    // helpers (très important pour éviter les bugs)
    public void addPaiement(Paiement p) { paiements.add(p); p.setReparation(this); }
    public void addAppareil(Appareil a) { appareils.add(a); a.setReparation(this); }
}