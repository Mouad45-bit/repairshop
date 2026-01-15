package com.maven.repairshop.ui.controllers;

import com.maven.repairshop.model.Appareil;
import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.model.enums.StatutReparation;
import com.maven.repairshop.service.ReparationService;
import com.maven.repairshop.ui.util.ServiceRegistry;

import java.util.List;

public final class ReparationController {

    private final ReparationService service = ServiceRegistry.get().reparations();

    public List<Reparation> rechercher(String query, Long reparateurId, StatutReparation statut, Long userId) {
        return service.rechercher(query, reparateurId, statut, userId);
    }

    public Reparation creerReparation(
            Long clientId,
            Long reparateurId,
            List<Appareil> appareils,
            String commentaireClient,
            Double avanceOptionnelle,
            Long userId
    ) {
        return service.creerReparation(clientId, reparateurId, appareils, commentaireClient, avanceOptionnelle, userId);
    }
}