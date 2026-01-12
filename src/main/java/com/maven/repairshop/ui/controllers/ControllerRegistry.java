package com.maven.repairshop.ui.controllers;

import com.maven.repairshop.service.EmpruntService;
import com.maven.repairshop.ui.util.ServiceRegistry;

public final class ControllerRegistry {

    private static final ControllerRegistry INSTANCE = new ControllerRegistry();

    private final EmpruntController empruntController;

    private ControllerRegistry() {
        // Pré-requis : ServiceRegistry.get().empruntService() existe
        EmpruntService empruntService = ServiceRegistry.get().empruntService();
        this.empruntController = new EmpruntController(empruntService);
    }

    public static ControllerRegistry get() {
        return INSTANCE;
    }

    public EmpruntController emprunts() {
        return empruntController;
    }
}