package com.maven.repairshop.service;

import com.maven.repairshop.model.Client;
import java.util.List;

public interface ClientService {
    Client creerClient(String nom, String telephone, String email, String adresse, String ville, Long reparateurId);
    void modifierClient(Long clientId, String nom, String telephone, String email, String adresse, String ville);
    void supprimerClient(Long clientId);
    Client trouverParId(Long clientId);
    List<Client> rechercher(String query, Long reparateurId);
    Client creerClient(String nom, String telephone, String email, String adresse, String ville, Long reparateurId, Long userId);
    void modifierClient(Long clientId, String nom, String telephone, String email, String adresse, String ville, Long userId);
    void supprimerClient(Long clientId, Long userId);
    Client trouverParId(Long clientId, Long userId);
    List<Client> rechercher(String query, Long reparateurId, Long userId);
}