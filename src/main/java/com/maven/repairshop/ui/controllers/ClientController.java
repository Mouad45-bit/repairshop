package com.maven.repairshop.ui.controllers;

import java.awt.Component;
import java.util.List;
import java.util.function.Consumer;

import com.maven.repairshop.model.Client;
import com.maven.repairshop.service.ClientService;

public class ClientController {

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    /**
     * Recherche clients (query + reparateurId) selon le contrat backend :
     * service.rechercher(String query, Long reparateurId)
     */
    public void rechercher(Component parent, String query, Long reparateurId, Consumer<List<Client>> onSuccess) {
        String q = (query == null) ? "" : query.trim();
        UiAsync.run(parent, () -> service.rechercher(q, reparateurId), onSuccess);
    }

    /**
     * Optionnel : "liste" = recherche avec query vide.
     * (Utile si un écran veut juste rafraîchir sans filtre)
     */
    public void lister(Component parent, Long reparateurId, Consumer<List<Client>> onSuccess) {
        UiAsync.run(parent, () -> service.rechercher("", reparateurId), onSuccess);
    }

    /**
     * Création selon contrat backend :
     * service.creerClient(nom, telephone, email, adresse, ville, reparateurId)
     */
    public void creer(
            Component parent,
            String nom, String telephone, String email, String adresse, String ville,
            Long reparateurId,
            Consumer<Client> onSuccess
    ) {
        UiAsync.run(parent,
                () -> service.creerClient(nom, telephone, email, adresse, ville, reparateurId),
                onSuccess
        );
    }

    /**
     * Modification selon contrat backend :
     * service.modifierClient(clientId, nom, telephone, email, adresse, ville)
     */
    public void modifier(
            Component parent,
            Long clientId,
            String nom, String telephone, String email, String adresse, String ville,
            Runnable onSuccess
    ) {
        UiAsync.runVoid(parent,
                () -> service.modifierClient(clientId, nom, telephone, email, adresse, ville),
                onSuccess
        );
    }

    /**
     * Suppression selon contrat backend :
     * service.supprimerClient(clientId)
     */
    public void supprimer(Component parent, Long clientId, Runnable onSuccess) {
        UiAsync.runVoid(parent, () -> service.supprimerClient(clientId), onSuccess);
    }

    /**
     * Trouver par id selon contrat backend :
     * service.trouverParId(clientId)
     */
    public void trouverParId(Component parent, Long clientId, Consumer<Client> onSuccess) {
        UiAsync.run(parent, () -> service.trouverParId(clientId), onSuccess);
    }
}