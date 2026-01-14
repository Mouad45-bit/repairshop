package com.maven.repairshop.ui.util;

import com.maven.repairshop.service.ClientService;
import com.maven.repairshop.service.EmpruntService;
import com.maven.repairshop.service.ReparationService;
import com.maven.repairshop.util.HibernateUtil;

/**
 * Registre central des services pour l'UI Swing.
 *
 * Règles:
 * - L'UI dépend uniquement des interfaces de service (contracts backend).
 * - Aucun mock ici.
 * - Les impl concrètes seront mergées plus tard par les collaborateurs backend.
 *
 * Technique:
 * - Chargement lazy via réflexion pour éviter des dépendances compile-time
 *   vers des classes d'impl qui n'existent pas encore au moment du merge UI.
 *
 * Convention attendue côté backend (modifiable ici si besoin):
 * - com.maven.repairshop.service.impl.EmpruntServiceImpl
 * - com.maven.repairshop.service.impl.ClientServiceImpl
 * - com.maven.repairshop.service.impl.ReparationServiceImpl
 */
public final class ServiceRegistry {

    private static final ServiceRegistry INSTANCE = new ServiceRegistry();

    private static final String IMPL_EMPRUNT = "com.maven.repairshop.service.impl.EmpruntServiceImpl";
    private static final String IMPL_CLIENT = "com.maven.repairshop.service.impl.ClientServiceImpl";
    private static final String IMPL_REPARATION = "com.maven.repairshop.service.impl.ReparationServiceImpl";

    private EmpruntService empruntService;
    private ClientService clientService;
    private ReparationService reparationService;

    private ServiceRegistry() {}

    public static ServiceRegistry get() {
        return INSTANCE;
    }

    public synchronized EmpruntService emprunts() {
        if (empruntService == null) {
            empruntService = newInstance(IMPL_EMPRUNT, EmpruntService.class);
        }
        return empruntService;
    }

    public synchronized ClientService clients() {
        if (clientService == null) {
            clientService = newInstance(IMPL_CLIENT, ClientService.class);
        }
        return clientService;
    }

    public synchronized ReparationService reparations() {
        if (reparationService == null) {
            reparationService = newInstance(IMPL_REPARATION, ReparationService.class);
        }
        return reparationService;
    }

    // --- Helpers frontend-only: permettre à l'UI de désactiver un module sans crash ---

    public boolean isEmpruntsAvailable() {
        return isAvailable(IMPL_EMPRUNT);
    }

    public boolean isClientsAvailable() {
        return isAvailable(IMPL_CLIENT);
    }

    public boolean isReparationsAvailable() {
        return isAvailable(IMPL_REPARATION);
    }

    private static boolean isAvailable(String implClassName) {
        try {
            Class.forName(implClassName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static <T> T newInstance(String implClassName, Class<T> type) {
        try {
            Class<?> clazz = Class.forName(implClassName);
            Object obj = clazz.getDeclaredConstructor().newInstance();
            return type.cast(obj);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Module backend non disponible.\n"
                            + "Impl introuvable: " + implClassName + "\n"
                            + "Action: merge la branche backend correspondante puis relance l'application.",
                    e
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Impossible d'instancier le service: " + implClassName
                            + " (" + e.getClass().getSimpleName() + ")",
                    e
            );
        }
    }

    /**
     * À appeler à la fermeture de l'application (windowClosing).
     * Permet de fermer proprement la SessionFactory Hibernate si elle a été utilisée.
     */
    public void shutdown() {
        try {
            HibernateUtil.getSessionFactory().close();
        } catch (Exception ignored) {
            // pas bloquant pour fermer l'UI
        }
    }
}