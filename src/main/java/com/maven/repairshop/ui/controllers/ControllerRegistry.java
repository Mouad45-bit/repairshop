package com.maven.repairshop.ui.controllers;

import com.maven.repairshop.service.ClientService;
import com.maven.repairshop.service.EmpruntService;
import com.maven.repairshop.ui.util.ServiceRegistry;

public final class ControllerRegistry {

    private static final ControllerRegistry INSTANCE = new ControllerRegistry();

    private final EmpruntController empruntController;
    private final ClientController clientController;

    private ControllerRegistry() {
        EmpruntService empruntService = ServiceRegistry.get().empruntService();
        ClientService clientService = ServiceRegistry.get().clientService();

        this.empruntController = new EmpruntController(empruntService);
        this.clientController = new ClientController(clientService);
    }

    public static ControllerRegistry get() {
        return INSTANCE;
    }

    public EmpruntController emprunts() {
        return empruntController;
    }

    public ClientController clients() {
        return clientController;
    }
}