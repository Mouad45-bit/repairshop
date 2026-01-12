package com.maven.repairshop.ui.controllers;

import java.awt.Component;
import java.util.List;
import java.util.function.Consumer;

import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.model.enums.StatutReparation;
import com.maven.repairshop.service.ReparationService;
import com.maven.repairshop.ui.util.UiServices;

public class ReparationController {

    private final ReparationService service;

    public ReparationController() {
        this.service = UiServices.get().reparations();
    }

    /** Liste + recherche + filtre statut */
    public void rechercher(Component parent,
                           String query,
                           Long reparateurId,
                           StatutReparation statut,
                           Consumer<List<Reparation>> onSuccess) {

        UiAsync.run(parent,
                () -> service.rechercher(query, reparateurId, statut),
                onSuccess);
    }

    /** Changer statut */
    public void changerStatut(Component parent,
                              Long reparationId,
                              StatutReparation nouveauStatut,
                              Runnable onSuccess) {

        UiAsync.runVoid(parent,
                () -> service.changerStatut(reparationId, nouveauStatut),
                onSuccess);
    }

    /** Trouver par id */
    public void trouverParId(Component parent,
                             Long reparationId,
                             Consumer<Reparation> onSuccess) {

        UiAsync.run(parent,
                () -> service.trouverParId(reparationId),
                onSuccess);
    }

    /** Créer une réparation (contract) */
    public void creerReparation(Component parent,
                                Long clientId,
                                Long reparateurId,
                                Consumer<Reparation> onSuccess) {

        UiAsync.run(parent,
                () -> service.creerReparation(clientId, reparateurId),
                onSuccess);
    }
}