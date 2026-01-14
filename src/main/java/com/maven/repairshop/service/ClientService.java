package com.maven.repairshop.service;

import com.maven.repairshop.model.Client;

import java.util.List;

public interface ClientService {

    /**
     * @deprecated Legacy : ne vérifie pas la boutique ni les droits (pas de userId).
     * Utiliser creerClient(..., userId).
     */
    @Deprecated
    Client creerClient(String nom, String telephone, String email, String adresse, String ville, Long reparateurId);

    /**
     * @deprecated Legacy : ne vérifie pas la boutique ni les droits (pas de userId).
     * Utiliser modifierClient(..., userId).
     */
    @Deprecated
    void modifierClient(Long clientId, String nom, String telephone, String email, String adresse, String ville);

    /**
     * @deprecated Legacy : ne vérifie pas la boutique ni les droits (pas de userId).
     * Utiliser supprimerClient(..., userId).
     */
    @Deprecated
    void supprimerClient(Long clientId);

    /**
     * @deprecated Legacy : ne vérifie pas la boutique ni les droits (pas de userId).
     * Utiliser trouverParId(..., userId).
     */
    @Deprecated
    Client trouverParId(Long clientId);

    /**
     * @deprecated Legacy : ne vérifie pas la boutique ni les droits (pas de userId).
     * Utiliser rechercher(..., userId).
     */
    @Deprecated
    List<Client> rechercher(String query, Long reparateurId);

    // -------------------- Versions sécurisées (recommandées) --------------------

    /**
     * Version sécurisée : vérifie que userId est dans la même boutique que le réparateur,
     * et applique les droits (PROPRIETAIRE / REPARATEUR).
     */
    Client creerClient(String nom, String telephone, String email, String adresse, String ville, Long reparateurId, Long userId);

    /**
     * Version sécurisée : vérifie boutique via client.reparateur.boutique et les droits via userId.
     */
    void modifierClient(Long clientId, String nom, String telephone, String email, String adresse, String ville, Long userId);

    /**
     * Version sécurisée : vérifie boutique + droits via userId.
     */
    void supprimerClient(Long clientId, Long userId);

    /**
     * Version sécurisée : vérifie boutique + droits via userId.
     */
    Client trouverParId(Long clientId, Long userId);

    /**
     * Version sécurisée :
     * - PROPRIETAIRE : peut lister/rechercher les clients d’un réparateur de sa boutique (reparateurId obligatoire)
     * - REPARATEUR : peut lister/rechercher ses clients (reparateurId peut être null ou égal à son id)
     */
    List<Client> rechercher(String query, Long reparateurId, Long userId);
}