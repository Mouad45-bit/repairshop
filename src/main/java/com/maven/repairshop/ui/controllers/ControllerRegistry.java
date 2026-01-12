package com.maven.repairshop.ui.controllers;

import com.maven.repairshop.service.ClientService;
import com.maven.repairshop.service.EmpruntService;
import com.maven.repairshop.service.ReparationService;
import com.maven.repairshop.ui.util.UiServices;

/**
 * Registre UI des controllers.
 * IMPORTANT: dépend uniquement de UiServices (contracts + mocks).
 * Aucune dépendance Hibernate/DAO ici.
 *
 * Plus tard: vos collègues pourront faire UiServices.overrideXxxService(...)
 * pour brancher les vraies impl backend sans changer l'UI.
 */
public final class ControllerRegistry {

    private static final ControllerRegistry INSTANCE = new ControllerRegistry();

    private final ReparationController reparationController;
    private final EmpruntController empruntController;
    private final ClientController clientController;

    private ControllerRegistry() {
        // services (contracts) depuis UiServices
        ReparationService reparationService = UiServices.get().reparations();
        EmpruntService empruntService = UiServices.get().emprunts();
        ClientService clientService = UiServices.get().clients();

        // controllers UI
        this.reparationController = new ReparationController(reparationService);
        this.empruntController = new EmpruntController(empruntService);
        this.clientController = new ClientController(clientService);
    }

    public static ControllerRegistry get() {
        return INSTANCE;
    }

    public ReparationController reparations() {
        return reparationController;
    }

    public EmpruntController emprunts() {
        return empruntController;
    }

    public ClientController clients() {
        return clientController;
    }
}
