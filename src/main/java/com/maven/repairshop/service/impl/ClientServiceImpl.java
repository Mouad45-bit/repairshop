package com.maven.repairshop.service.impl;

import com.maven.repairshop.dao.ClientDao;
import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.*;
import com.maven.repairshop.service.ClientService;
import com.maven.repairshop.service.exceptions.NotFoundException;
import com.maven.repairshop.service.exceptions.ValidationException;

import java.util.List;

public class ClientServiceImpl implements ClientService {

    private final ClientDao clientDao = new ClientDao();

    // ---------- legacy (on garde, fallback pour ne pas casser l’UI) ----------
    @Override
    @Deprecated
    public Client creerClient(String nom, String telephone, String email, String adresse, String ville, Long reparateurId) {
        // fallback legacy : on suppose que l'acteur est le réparateur cible
        return creerClient(nom, telephone, email, adresse, ville, reparateurId, reparateurId);
    }

    @Override
    @Deprecated
    public void modifierClient(Long clientId, String nom, String telephone, String email, String adresse, String ville) {
        // fallback legacy : on agit comme si l'acteur était le réparateur du client
        if (clientId == null) throw new ValidationException("Client obligatoire.");
        if (nom == null || nom.isBlank()) throw new ValidationException("Nom obligatoire.");

        Long fallbackUserId = HibernateTx.callInTx(session -> {
            Client c = loadClientWithReparateurBoutique(session, clientId);
            if (c == null) throw new NotFoundException("Client introuvable: " + clientId);
            if (c.getReparateur() == null || c.getReparateur().getId() == null) {
                throw new ValidationException("Client invalide: aucun réparateur.");
            }
            return c.getReparateur().getId();
        });

        // NOTE: ce fallback ne garantit pas la sécurité complète (pas d'user connecté),
        // mais évite de casser l’existant.
        modifierClient(clientId, nom, telephone, email, adresse, ville, fallbackUserId);
    }

    @Override
    @Deprecated
    public void supprimerClient(Long clientId) {
        // fallback legacy : on agit comme si l'acteur était le réparateur du client
        if (clientId == null) throw new ValidationException("Client obligatoire.");

        Long fallbackUserId = HibernateTx.callInTx(session -> {
            Client c = loadClientWithReparateurBoutique(session, clientId);
            if (c == null) throw new NotFoundException("Client introuvable: " + clientId);
            if (c.getReparateur() == null || c.getReparateur().getId() == null) {
                throw new ValidationException("Client invalide: aucun réparateur.");
            }
            return c.getReparateur().getId();
        });

        supprimerClient(clientId, fallbackUserId);
    }

    @Override
    @Deprecated
    public Client trouverParId(Long clientId) {
        // fallback legacy : on renvoie le client chargé (sans contrôle user),
        // mais on tente de rester cohérent en passant par l’acteur = réparateur du client.
        if (clientId == null) throw new ValidationException("Client obligatoire.");

        Long fallbackUserId = HibernateTx.callInTx(session -> {
            Client c = loadClientWithReparateurBoutique(session, clientId);
            if (c == null) throw new NotFoundException("Client introuvable: " + clientId);
            if (c.getReparateur() == null || c.getReparateur().getId() == null) {
                throw new ValidationException("Client invalide: aucun réparateur.");
            }
            return c.getReparateur().getId();
        });

        return trouverParId(clientId, fallbackUserId);
    }

    @Override
    @Deprecated
    public List<Client> rechercher(String query, Long reparateurId) {
        // fallback legacy : on suppose acteur = réparateurId (comme avant)
        if (reparateurId == null) throw new ValidationException("Réparateur obligatoire.");
        return rechercher(query, reparateurId, reparateurId);
    }

    // --------------------- sécurisé ---------------------

    @Override
    public Client creerClient(String nom, String telephone, String email, String adresse, String ville, Long reparateurId, Long userId) {
        if (nom == null || nom.isBlank()) throw new ValidationException("Nom obligatoire.");
        if (reparateurId == null) throw new ValidationException("Réparateur obligatoire.");
        if (userId == null) throw new ValidationException("Utilisateur connecté obligatoire.");

        return HibernateTx.callInTx(session -> {
            Utilisateur user = session.get(Utilisateur.class, userId);
            if (user == null) throw new NotFoundException("Utilisateur introuvable: " + userId);

            Reparateur rep = session.createQuery(
                            "select r from Reparateur r left join fetch r.boutique b where r.id = :id",
                            Reparateur.class
                    )
                    .setParameter("id", reparateurId)
                    .uniqueResult();
            if (rep == null) throw new NotFoundException("Réparateur introuvable: " + reparateurId);

            // sécurité boutique
            assertSameBoutique(user.getBoutique(), rep.getBoutique());

            // rôle : réparateur ne peut créer que pour lui-même
            if (user instanceof Reparateur && !user.getId().equals(reparateurId)) {
                throw new ValidationException("Accès refusé : un réparateur ne crée que ses clients.");
            }

            Client c = new Client();
            c.setNom(nom.trim());
            c.setTelephone(telephone);
            c.setEmail(email);
            c.setAdresse(adresse);
            c.setVille(ville);
            c.setReparateur(rep);

            session.persist(c);
            return c;
        });
    }

    @Override
    public void modifierClient(Long clientId, String nom, String telephone, String email, String adresse, String ville, Long userId) {
        if (clientId == null) throw new ValidationException("Client obligatoire.");
        if (nom == null || nom.isBlank()) throw new ValidationException("Nom obligatoire.");
        if (userId == null) throw new ValidationException("Utilisateur connecté obligatoire.");

        HibernateTx.runInTx(session -> {
            Utilisateur user = session.get(Utilisateur.class, userId);
            if (user == null) throw new NotFoundException("Utilisateur introuvable: " + userId);

            Client c = loadClientWithReparateurBoutique(session, clientId);
            if (c == null) throw new NotFoundException("Client introuvable: " + clientId);

            // sécurité boutique
            assertSameBoutique(user.getBoutique(), c.getReparateur().getBoutique());

            // rôle : réparateur => seulement ses clients
            if (user instanceof Reparateur && !user.getId().equals(c.getReparateur().getId())) {
                throw new ValidationException("Accès refusé : ce client n'est pas à vous.");
            }

            c.setNom(nom.trim());
            c.setTelephone(telephone);
            c.setEmail(email);
            c.setAdresse(adresse);
            c.setVille(ville);

            session.merge(c);
        });
    }

    @Override
    public void supprimerClient(Long clientId, Long userId) {
        if (clientId == null) throw new ValidationException("Client obligatoire.");
        if (userId == null) throw new ValidationException("Utilisateur connecté obligatoire.");

        // règle métier : interdite si réparations
        long nb = clientDao.countReparationsByClient(clientId);
        if (nb > 0) throw new ValidationException("Suppression interdite : client a des réparations.");

        HibernateTx.runInTx(session -> {
            Utilisateur user = session.get(Utilisateur.class, userId);
            if (user == null) throw new NotFoundException("Utilisateur introuvable: " + userId);

            Client c = loadClientWithReparateurBoutique(session, clientId);
            if (c == null) throw new NotFoundException("Client introuvable: " + clientId);

            // sécurité boutique
            assertSameBoutique(user.getBoutique(), c.getReparateur().getBoutique());

            // rôle : réparateur => seulement ses clients
            if (user instanceof Reparateur && !user.getId().equals(c.getReparateur().getId())) {
                throw new ValidationException("Accès refusé : ce client n'est pas à vous.");
            }

            session.remove(c);
        });
    }

    @Override
    public Client trouverParId(Long clientId, Long userId) {
        if (clientId == null) throw new ValidationException("Client obligatoire.");
        if (userId == null) throw new ValidationException("Utilisateur connecté obligatoire.");

        return HibernateTx.callInTx(session -> {
            Utilisateur user = session.get(Utilisateur.class, userId);
            if (user == null) throw new NotFoundException("Utilisateur introuvable: " + userId);

            Client c = loadClientWithReparateurBoutique(session, clientId);
            if (c == null) throw new NotFoundException("Client introuvable: " + clientId);

            assertSameBoutique(user.getBoutique(), c.getReparateur().getBoutique());

            if (user instanceof Reparateur && !user.getId().equals(c.getReparateur().getId())) {
                throw new ValidationException("Accès refusé : ce client n'est pas à vous.");
            }
            return c;
        });
    }

    @Override
    public List<Client> rechercher(String query, Long reparateurId, Long userId) {
        if (reparateurId == null) throw new ValidationException("Réparateur obligatoire.");
        if (userId == null) throw new ValidationException("Utilisateur connecté obligatoire.");

        // check d'accès sur le réparateur cible
        HibernateTx.runInTx(session -> {
            Utilisateur user = session.get(Utilisateur.class, userId);
            if (user == null) throw new NotFoundException("Utilisateur introuvable: " + userId);

            Reparateur rep = session.get(Reparateur.class, reparateurId);
            if (rep == null) throw new NotFoundException("Réparateur introuvable: " + reparateurId);

            // sécurité boutique : user vs rep
            assertSameBoutique(user.getBoutique(), rep.getBoutique());

            // rôle : réparateur ne liste/recherche que pour lui-même
            if (user instanceof Reparateur && !user.getId().equals(reparateurId)) {
                throw new ValidationException("Accès refusé : un réparateur ne voit que ses clients.");
            }
        });

        if (query == null || query.isBlank()) return clientDao.findByReparateur(reparateurId);
        return clientDao.searchByNom(reparateurId, query);
    }

    // ----------------- helpers -----------------

    private Client loadClientWithReparateurBoutique(org.hibernate.Session session, Long clientId) {
        return session.createQuery(
                        "select c from Client c " +
                                "join fetch c.reparateur r " +
                                "left join fetch r.boutique b " +
                                "where c.id = :id",
                        Client.class
                )
                .setParameter("id", clientId)
                .uniqueResult();
    }

    private void assertSameBoutique(Boutique ub, Boutique tb) {
        Long u = ub == null ? null : ub.getId();
        Long t = tb == null ? null : tb.getId();
        if (u == null || t == null || !u.equals(t)) {
            throw new ValidationException("Accès refusé (boutique différente).");
        }
    }
}