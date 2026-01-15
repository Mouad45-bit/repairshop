package com.maven.repairshop.ui.controllers;

import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.model.enums.StatutReparation;
import com.maven.repairshop.service.ReparationService;
import com.maven.repairshop.ui.util.ServiceRegistry;

import java.util.List;

public final class ReparationController {

    private final ReparationService service = ServiceRegistry.get().reparations();

    public List<Reparation> rechercher(String query, Long reparateurId, StatutReparation statut) {
        return service.rechercher(query, reparateurId, statut);
    }

    public Reparation trouverParId(Long id) {
        return service.trouverParId(id);
    }

    public void changerStatut(Long reparationId, StatutReparation statut) {
        service.changerStatut(reparationId, statut);
    }

    public Reparation creerReparation(Long clientId, Long reparateurId) {
        return service.creerReparation(clientId, reparateurId);
    }
}