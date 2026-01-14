package com.maven.repairshop.service;

import com.maven.repairshop.model.Appareil;
import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.model.enums.StatutReparation;

import java.util.List;

public interface ReparationService {

    /**
     * @deprecated Legacy : ne vérifie pas la boutique ni les droits (pas de userId).
     * Utiliser la version complète avec userId.
     */
    @Deprecated
    Reparation creerReparation(Long clientId, Long reparateurId);

    /**
     * Version sécurisée (recommandée) : vérifie rôle + boutique via userId.
     */
    Reparation creerReparation(
            Long clientId,
            Long reparateurId,
            List<Appareil> appareils,
            String commentaireClient,
            Double avanceOptionnelle,
            Long userId
    );

    /**
     * Validation de la réparation (généralement par réparateur/propriétaire de la boutique),
     * avec contrôle boutique/droits via userId.
     */
    void validerReparation(Long reparationId, Long userId);

    /**
     * @deprecated Legacy : ne vérifie pas la boutique ni les droits (pas de userId).
     * Utiliser changerStatut(..., userId).
     */
    @Deprecated
    void changerStatut(Long reparationId, StatutReparation nouveauStatut);

    /**
     * Version sécurisée : vérifie droits + boutique via userId.
     */
    void changerStatut(Long reparationId, StatutReparation nouveauStatut, Long userId);

    /**
     * Ajout d’un commentaire technique (réparateur/propriétaire), sécurisé par userId.
     */
    void ajouterCommentaireTechnique(Long reparationId, String texte, Long userId);

    /**
     * @deprecated Legacy : ne vérifie pas la boutique ni les droits (pas de userId).
     * Utiliser trouverParId(..., userId).
     */
    @Deprecated
    Reparation trouverParId(Long reparationId);

    /**
     * Version sécurisée : vérifie boutique/droits via userId.
     */
    Reparation trouverParId(Long reparationId, Long userId);

    /**
     * @deprecated Legacy : ne vérifie pas la boutique ni les droits (pas de userId).
     * Utiliser rechercher(..., userId).
     */
    @Deprecated
    List<Reparation> rechercher(String query, Long reparateurId, StatutReparation filtreStatut);

    /**
     * Version sécurisée : renvoie uniquement les réparations visibles par le user connecté.
     * - PROPRIETAIRE : voit toutes les réparations de sa boutique
     * - REPARATEUR : voit uniquement ses réparations
     */
    List<Reparation> rechercher(String query, Long reparateurId, StatutReparation filtreStatut, Long userId);
}