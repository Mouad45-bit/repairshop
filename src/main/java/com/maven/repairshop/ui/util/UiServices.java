package com.maven.repairshop.ui.util;

import com.maven.repairshop.service.ReparationService;
import com.maven.repairshop.ui.mocks.InMemoryReparationService;

/**
 * Registre UI : expose uniquement des interfaces métier (contracts).
 * Aujourd'hui: on branche un mock InMemory.
 * Demain: vos collaborateurs remplaceront par de vraies impl (sans changer l'UI).
 */
public final class UiServices {

    private static UiServices INSTANCE;

    private ReparationService reparationService;

    private UiServices() {
        // UI-only : pas de HibernateUtil, pas de DAO ici
        this.reparationService = new InMemoryReparationService();
    }

    public static synchronized UiServices get() {
        if (INSTANCE == null) {
            INSTANCE = new UiServices();
        }
        return INSTANCE;
    }

    public ReparationService reparations() {
        return reparationService;
    }

    /**
     * Permet de remplacer le mock par une vraie impl plus tard,
     * sans modifier les pages/controllers UI.
     */
    public synchronized void overrideReparationService(ReparationService service) {
        if (service == null) throw new IllegalArgumentException("service == null");
        this.reparationService = service;
    }
}