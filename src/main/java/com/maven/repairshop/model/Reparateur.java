package com.maven.repairshop.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("REPARATEUR")
public class Reparateur extends Utilisateur {

    @OneToMany(mappedBy = "reparateur")
    private Set<Client> clients = new HashSet<>();

    @OneToMany(mappedBy = "reparateur")
    private Set<Emprunt> emprunts = new HashSet<>();

    public Set<Client> getClients() { return clients; }
    public Set<Emprunt> getEmprunts() { return emprunts; }
}
