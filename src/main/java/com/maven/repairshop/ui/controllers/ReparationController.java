package com.maven.repairshop.ui.controllers;

import java.awt.Component;
import java.util.List;

import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.model.enums.StatutReparation;
import com.maven.repairshop.service.ReparationService;
import com.maven.repairshop.ui.util.UiServices;
import com.maven.repairshop.ui.util.UiAsync;

public class ReparationController {

    private final ReparationService service;

    public ReparationController() {
        // UI only : on récupère l'interface via UiServices (mock aujourd’hui)
        this.service = UiServices.get().reparations();
    }

    /** Liste + recherche + filtre statut */
    public void rechercher(Component parent,
                           String query,
                           Long reparateurId,
                           StatutReparation statut,
                           UiAsync.Success<List<Reparation>> onSuccess) {

        UiAsync.run(parent, "Chargement des réparations...",
                () -> service.rechercher(query, reparateurId, statut),
                onSuccess);
    }

    /** Changement statut */
    public void changerStatut(Component parent,
                              Long reparationId,
                              StatutReparation nouveauStatut,
                              UiAsync.Success<Void> onSuccess) {

        UiAsync.run(parent, "Mise à jour du statut...",
                () -> {
                    service.changerStatut(reparationId, nouveauStatut);
                    return null;
                },
                onSuccess);
    }

    /** Option utile : récupérer une réparation par id (pour Détail, etc.) */
    public void trouverParId(Component parent,
                             Long reparationId,
                             UiAsync.Success<Reparation> onSuccess) {

        UiAsync.run(parent, "Chargement...",
                () -> service.trouverParId(reparationId),
                onSuccess);
    }
}