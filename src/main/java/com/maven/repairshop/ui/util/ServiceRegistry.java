package com.maven.repairshop.ui.util;

import com.maven.repairshop.dao.EmpruntDao;
import com.maven.repairshop.service.EmpruntService;
import com.maven.repairshop.service.impl.EmpruntServiceImpl;
import com.maven.repairshop.util.HibernateUtil;

/**
 * Registre central des services pour l'UI Swing.
 * Objectif : l'UI n'instancie pas Hibernate/DAO directement.
 *
 * Pour l'instant, on câble seulement Emprunt (car c'est le seul service implémenté).
 * Les autres (Client/Reparation/Paiement/Auth...) seront ajoutés plus tard
 * sans casser les écrans.
 */
public final class ServiceRegistry {

    private static ServiceRegistry INSTANCE;

    // ---- DAO ----
    private final EmpruntDao empruntDao;

    // ---- Services ----
    private final EmpruntService empruntService;

    private ServiceRegistry() {
        // IMPORTANT : toucher HibernateUtil ici force l'init SessionFactory au lancement UI
        HibernateUtil.getSessionFactory();

        this.empruntDao = new EmpruntDao();
        this.empruntService = new EmpruntServiceImpl(empruntDao);

        // Plus tard :
        // this.clientDao = new ClientDao();
        // this.clientService = new ClientServiceImpl(clientDao, ...);
        // etc.
    }

    /** Singleton simple (une seule instance pour toute l'application Swing). */
    public static synchronized ServiceRegistry get() {
        if (INSTANCE == null) {
            INSTANCE = new ServiceRegistry();
        }
        return INSTANCE;
    }

    // ---- Getters services ----
    public EmpruntService empruntService() {
        return empruntService;
    }

    /**
     * A appeler à la fermeture de l'application (MainFrame windowClosing).
     * Cela évite que la JVM reste ouverte (SessionFactory non fermée).
     */
    public void shutdown() {
        try {
            HibernateUtil.getSessionFactory().close();
        } catch (Exception ignored) {
            // pas bloquant pour fermer l'UI
        }
    }
}