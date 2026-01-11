package com.maven.repairshop.service;

import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.model.enums.StatutReparation;
import java.util.List;

public interface ReparationService {
    Reparation creerReparation(Long clientId, Long reparateurId);
    void changerStatut(Long reparationId, StatutReparation nouveauStatut);
    Reparation trouverParId(Long reparationId);
    List<Reparation> rechercher(String query, Long reparateurId, StatutReparation filtreStatut);
}