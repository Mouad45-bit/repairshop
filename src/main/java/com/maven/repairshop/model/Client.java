package com.maven.repairshop.model;

import com.maven.repairshop.model.base.BaseEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="clients")
public class Client extends BaseEntity {

    @Column(nullable=false)
    private String nom;

    private String telephone;
    private String email;
    private String adresse;
    private String ville;
    private String imagePath;

    @ManyToOne
    @JoinColumn(name="reparateur_id", nullable=false)
    private Reparateur reparateur;

    @OneToMany(mappedBy="client")
    private Set<Reparation> reparations = new HashSet<>();

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Reparateur getReparateur() { return reparateur; }
    public void setReparateur(Reparateur reparateur) { this.reparateur = reparateur; }

    public Set<Reparation> getReparations() { return reparations; }
}