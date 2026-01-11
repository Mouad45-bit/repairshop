package com.maven.repairshop.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("PROPRIETAIRE")
public class Proprietaire extends Utilisateur {
    // plus tard : méthodes métier via service (pas ici)
}
