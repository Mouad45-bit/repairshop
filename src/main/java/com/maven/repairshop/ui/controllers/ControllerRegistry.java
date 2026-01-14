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
 *
 * IMPORTANT (merge-friendly):
 * - Lazy init: on ne construit les controllers que lorsqu’ils sont demandés.
 *   => évite de casser l'app au démarrage si un module backend n’est pas encore mergé.
 */
public final class ControllerRegistry {

    private static final ControllerRegistry INSTANCE = new ControllerRegistry();

    private ReparationController reparationController;
    private SuiviController suiviController;
    private EmpruntController empruntController;
    private ClientController clientController;

    private ControllerRegistry() {
        // Lazy init: ne rien instancier ici
    }

    public static ControllerRegistry get() {
        return INSTANCE;
    }

    public synchronized ReparationController reparations() {
        if (reparationController == null) {
            ReparationService reparationService = ServiceRegistry.get().reparations();
            this.reparationController = new ReparationController(reparationService);
        }
        return reparationController;
    }

    public synchronized SuiviController suivi() {
        if (suiviController == null) {
            ReparationService reparationService = ServiceRegistry.get().reparations();
            this.suiviController = new SuiviController(reparationService);
        }
        return suiviController;
    }

    public synchronized EmpruntController emprunts() {
        if (empruntController == null) {
            EmpruntService empruntService = ServiceRegistry.get().emprunts();
            this.empruntController = new EmpruntController(empruntService);
        }
        return empruntController;
    }

    public synchronized ClientController clients() {
        if (clientController == null) {
            ClientService clientService = ServiceRegistry.get().clients();
            this.clientController = new ClientController(clientService);
        }
        return clientController;
    }
}