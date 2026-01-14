package com.maven.repairshop.service;

import com.maven.repairshop.model.Boutique;
import com.maven.repairshop.model.Reparateur;

public interface ProprietaireService {

    Boutique creerBoutique(Long proprietaireId, String nom, String adresse, String telephone);

    /**
     * @deprecated Utiliser la version sécurisée avec userId (propriétaire connecté).
     * Cette méthode existe pour compatibilité legacy (ne pas casser l’existant).
     */
    @Deprecated
    Reparateur creerReparateur(Long boutiqueId, String nom, String login, String rawPassword);

    /**
     * Version sécurisée : vérifie que userId est un Propriétaire
     * et qu’il est propriétaire de la boutique ciblée.
     */
    Reparateur creerReparateur(Long boutiqueId, String nom, String login, String rawPassword, Long userId);
}