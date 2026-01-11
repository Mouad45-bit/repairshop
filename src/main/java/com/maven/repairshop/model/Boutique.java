package com.maven.repairshop.model;

import com.maven.repairshop.model.base.BaseEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="boutiques")
public class Boutique extends BaseEntity {

    @Column(nullable=false)
    private String nom;

    private String adresse;
    private String telephone;

    @ManyToOne
    @JoinColumn(name="proprietaire_id", nullable=false)
    private Proprietaire proprietaire;

    @OneToMany(mappedBy = "boutique")
    private Set<Utilisateur> utilisateurs = new HashSet<>();

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public Proprietaire getProprietaire() { return proprietaire; }
    public void setProprietaire(Proprietaire proprietaire) { this.proprietaire = proprietaire; }

    public Set<Utilisateur> getUtilisateurs() { return utilisateurs; }
}
