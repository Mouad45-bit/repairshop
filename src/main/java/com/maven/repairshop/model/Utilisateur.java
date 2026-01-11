package com.maven.repairshop.model;

import com.maven.repairshop.model.base.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "utilisateurs")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
public abstract class Utilisateur extends BaseEntity {

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String password;

    @ManyToOne
    @JoinColumn(name="boutique_id")
    private Boutique boutique;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Boutique getBoutique() { return boutique; }
    public void setBoutique(Boutique boutique) { this.boutique = boutique; }
}
