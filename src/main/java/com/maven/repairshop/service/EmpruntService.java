package com.maven.repairshop.service;

import com.maven.repairshop.model.Emprunt;
import com.maven.repairshop.model.enums.TypeEmprunt;

import java.util.List;

public interface EmpruntService {
    Emprunt creer(Long reparateurId, TypeEmprunt type, String personne, double montant, String motif);
    void changerStatut(Long empruntId, String nouveauStatut);
    void supprimer(Long empruntId);
    List<Emprunt> lister(Long reparateurId);
    Emprunt creer(Long reparateurId, TypeEmprunt type, String personne, double montant, String motif, Long userId);
    void changerStatut(Long empruntId, String nouveauStatut, Long userId);
    void supprimer(Long empruntId, Long userId);
    List<Emprunt> lister(Long reparateurId, Long userId);
}