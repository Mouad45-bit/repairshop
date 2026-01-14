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

    // ---------- legacy (on garde, mais sans sécurité forte) ----------
    @Override
    public Client creerClient(String nom, String telephone, String email, String adresse, String ville, Long reparateurId) {
        return creerClient(nom, telephone, email, adresse, ville, reparateurId, reparateurId);
    }

    @Override
    public void modifierClient(Long clientId, String nom, String telephone, String email, String adresse, String ville) {
        modifierClient(clientId, nom, telephone, email, adresse, ville, null);
    }

    @Override
    public void supprimerClient(Long clientId) {
        supprimerClient(clientId, null);
    }

    @Override
    public Client trouverParId(Long clientId) {
        return trouverParId(clientId, null);
    }

    @Override
    public List<Client> rechercher(String query, Long reparateurId) {
        return rechercher(query, reparateurId, null);
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