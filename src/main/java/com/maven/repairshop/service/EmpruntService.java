package com.maven.repairshop.service;

import com.maven.repairshop.model.Emprunt;
import com.maven.repairshop.model.enums.TypeEmprunt;
import java.util.List;

public interface EmpruntService {
    Emprunt creer(Long reparateurId, TypeEmprunt type, String personne, double montant, String motif);
    void changerStatut(Long empruntId, String nouveauStatut); // ou enum plus tard
    void supprimer(Long empruntId);
    List<Emprunt> lister(Long reparateurId);
}