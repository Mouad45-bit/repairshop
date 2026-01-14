package com.maven.repairshop.service;

import com.maven.repairshop.model.Appareil;
import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.model.enums.StatutReparation;

import java.util.List;

public interface ReparationService {
    Reparation creerReparation(Long clientId, Long reparateurId);
    Reparation creerReparation(
            Long clientId,
            Long reparateurId,
            List<Appareil> appareils,
            String commentaireClient,
            Double avanceOptionnelle,
            Long userId
    );
    void validerReparation(Long reparationId, Long userId);
    void changerStatut(Long reparationId, StatutReparation nouveauStatut);
    void changerStatut(Long reparationId, StatutReparation nouveauStatut, Long userId);
    void ajouterCommentaireTechnique(Long reparationId, String texte, Long userId);
    Reparation trouverParId(Long reparationId);
    Reparation trouverParId(Long reparationId, Long userId);
    List<Reparation> rechercher(String query, Long reparateurId, StatutReparation filtreStatut);
}