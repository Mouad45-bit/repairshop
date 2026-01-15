package com.maven.repairshop.ui.util;

import com.maven.repairshop.service.*;
import com.maven.repairshop.service.impl.*;

public final class ServiceRegistry {

    private static final ServiceRegistry INSTANCE = new ServiceRegistry();

    private final AuthService authService = new AuthServiceImpl();
    private final ReparationService reparationService = new ReparationServiceImpl();
    private final ClientService clientService = new ClientServiceImpl();
    private final EmpruntService empruntService = new EmpruntServiceImpl();
    private final PaiementService paiementService = new PaiementServiceImpl();
    private final CaisseService caisseService = new CaisseServiceImpl();
    private final ProprietaireService proprietaireService = new ProprietaireServiceImpl();
    private final StatistiquesService statistiquesService = new StatistiquesServiceImpl();
    private final SuiviService suiviService = new SuiviServiceImpl();

    private ServiceRegistry() {}

    public static ServiceRegistry get() {
        return INSTANCE;
    }

    public AuthService auth() { return authService; }
    public ReparationService reparations() { return reparationService; }
    public ClientService clients() { return clientService; }
    public EmpruntService emprunts() { return empruntService; }
    public PaiementService paiements() { return paiementService; }
    public CaisseService caisse() { return caisseService; }
    public ProprietaireService proprietaire() { return proprietaireService; }
    public StatistiquesService stats() { return statistiquesService; }
    public SuiviService suivi() { return suiviService; }
}