package com.maven.repairshop.service;

import com.maven.repairshop.model.Emprunt;
import com.maven.repairshop.model.enums.TypeEmprunt;

import java.util.List;

public interface EmpruntService {

    /**
     * @deprecated Legacy : ne vérifie pas la boutique ni les droits (pas de userId).
     * Utiliser creer(..., userId).
     */
    @Deprecated
    Emprunt creer(Long reparateurId, TypeEmprunt type, String personne, double montant, String motif);

    /**
     * @deprecated Legacy : ne vérifie pas la boutique ni les droits (pas de userId).
     * Utiliser changerStatut(..., userId).
     */
    @Deprecated
    void changerStatut(Long empruntId, String nouveauStatut);

    /**
     * @deprecated Legacy : ne vérifie pas la boutique ni les droits (pas de userId).
     * Utiliser supprimer(..., userId).
     */
    @Deprecated
    void supprimer(Long empruntId);

    /**
     * @deprecated Legacy : ne vérifie pas la boutique ni les droits (pas de userId).
     * Utiliser lister(..., userId).
     */
    @Deprecated
    List<Emprunt> lister(Long reparateurId);

    // -------------------- Versions sécurisées (recommandées) --------------------

    /**
     * Version sécurisée :
     * - REPARATEUR : ne peut créer que pour lui-même (reparateurId == userId)
     * - PROPRIETAIRE : peut créer pour un réparateur de sa boutique
     * Contrôle boutique via reparateur.boutique vs user.boutique
     */
    Emprunt creer(Long reparateurId, TypeEmprunt type, String personne, double montant, String motif, Long userId);

    /**
     * Version sécurisée :
     * - REPARATEUR : ne modifie que ses emprunts
     * - PROPRIETAIRE : peut modifier les emprunts de sa boutique
     * Contrôle boutique via emprunt.reparateur.boutique vs user.boutique
     */
    void changerStatut(Long empruntId, String nouveauStatut, Long userId);

    /**
     * Version sécurisée :
     * - REPARATEUR : ne supprime que ses emprunts
     * - PROPRIETAIRE : peut supprimer dans sa boutique
     */
    void supprimer(Long empruntId, Long userId);

    /**
     * Version sécurisée :
     * - REPARATEUR : liste ses emprunts (reparateurId null ou == userId)
     * - PROPRIETAIRE : peut lister par réparateur de sa boutique (reparateurId obligatoire)
     */
    List<Emprunt> lister(Long reparateurId, Long userId);
}