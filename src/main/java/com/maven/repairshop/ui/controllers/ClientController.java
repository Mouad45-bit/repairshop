package com.maven.repairshop.ui.controllers;

import java.awt.Component;
import java.util.List;

import com.maven.repairshop.model.Client;
import com.maven.repairshop.service.ClientService;

public class ClientController {

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    public void lister(Component parent, Long reparateurId, java.util.function.Consumer<List<Client>> onSuccess) {
        UiAsync.run(parent, () -> service.listerParReparateur(reparateurId), onSuccess);
    }

    public void rechercher(Component parent, Long reparateurId, String q, java.util.function.Consumer<List<Client>> onSuccess) {
        UiAsync.run(parent, () -> service.rechercher(reparateurId, q), onSuccess);
    }

    public void creer(Component parent, Long reparateurId,
                      String nom, String telephone, String email, String adresse, String ville, String imagePath,
                      java.util.function.Consumer<Client> onSuccess) {
        UiAsync.run(parent, () -> service.creer(reparateurId, nom, telephone, email, adresse, ville, imagePath), onSuccess);
    }

    public void modifier(Component parent, Long clientId,
                         String nom, String telephone, String email, String adresse, String ville, String imagePath,
                         Runnable onSuccess) {
        UiAsync.runVoid(parent, () -> service.modifier(clientId, nom, telephone, email, adresse, ville, imagePath), onSuccess);
    }

    public void supprimer(Component parent, Long clientId, Runnable onSuccess) {
        UiAsync.runVoid(parent, () -> service.supprimer(clientId), onSuccess);
    }
}