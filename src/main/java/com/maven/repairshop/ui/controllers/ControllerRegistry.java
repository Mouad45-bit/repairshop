package com.maven.repairshop.ui.controllers;

import com.maven.repairshop.service.ClientService;
import com.maven.repairshop.service.EmpruntService;
import com.maven.repairshop.service.ReparationService;
import com.maven.repairshop.ui.util.ServiceRegistry;

/**
 * Registre UI des controllers.
 *
 * Règles:
 * - Ne dépend que des interfaces service (contracts backend).
 * - Les impl concrètes sont fournies par ServiceRegistry (chargement backend).
 */
public final class ControllerRegistry {

    private static final ControllerRegistry INSTANCE = new ControllerRegistry();

    private final ReparationController reparationController;
    private final EmpruntController empruntController;
    private final ClientController clientController;

    private ControllerRegistry() {
        // services (contracts) depuis ServiceRegistry (backend)
        ReparationService reparationService = ServiceRegistry.get().reparations();
        EmpruntService empruntService = ServiceRegistry.get().emprunts();
        ClientService clientService = ServiceRegistry.get().clients();

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