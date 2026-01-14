package com.maven.repairshop.service.impl;

import com.maven.repairshop.dao.ReparationDao;
import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.*;
import com.maven.repairshop.model.enums.StatutReparation;
import com.maven.repairshop.model.enums.TypePaiement;
import com.maven.repairshop.service.ReparationService;
import com.maven.repairshop.service.exceptions.NotFoundException;
import com.maven.repairshop.service.exceptions.ValidationException;

import java.time.LocalDateTime;
import java.util.*;

public class ReparationServiceImpl implements ReparationService {

    private final ReparationDao reparationDao = new ReparationDao();

    @Override
    public Reparation creerReparation(Long clientId, Long reparateurId) {
        // legacy: on garde la signature, mais elle ne permet pas de respecter le cahier (appareils obligatoires)
        return creerReparation(clientId, reparateurId, List.of(), null, null, reparateurId);
    }

    @Override
    public Reparation creerReparation(Long clientId,
                                      Long reparateurId,
                                      List<Appareil> appareils,
                                      String commentaireClient,
                                      Double avanceOptionnelle,
                                      Long userId) {

        if (clientId == null) throw new ValidationException("Client obligatoire.");
        if (reparateurId == null) throw new ValidationException("Réparateur obligatoire.");
        if (userId == null) throw new ValidationException("Utilisateur connecté obligatoire.");
        if (appareils == null || appareils.isEmpty()) throw new ValidationException("Au moins 1 appareil est obligatoire.");

        return HibernateTx.callInTx(session -> {

            Utilisateur user = session.get(Utilisateur.class, userId);
            if (user == null) throw new NotFoundException("Utilisateur introuvable: " + userId);

            Client client = session.createQuery(
                            "select c from Client c " +
                                    "join fetch c.reparateur r " +
                                    "left join fetch r.boutique b " +
                                    "where c.id = :id",
                            Client.class
                    )
                    .setParameter("id", clientId)
                    .uniqueResult();

            if (client == null) throw new NotFoundException("Client introuvable: " + clientId);

            // cohérence : le client appartient à ce réparateur
            if (client.getReparateur() == null || client.getReparateur().getId() == null
                    || !client.getReparateur().getId().equals(reparateurId)) {
                throw new ValidationException("Client non rattaché au réparateur indiqué.");
            }

            // Sécurité boutique sans boutique_id :
            assertSameBoutique(user.getBoutique(), client.getReparateur().getBoutique());

            // validations appareils + IMEI (input + DB)
            validateAndCheckImeis(session, appareils);

            Reparation rep = new Reparation();
            rep.setClient(client);
            rep.setCodeUnique(generateCodeUnique(session));
            rep.setStatut(StatutReparation.ENREGISTREE);
            rep.setDateDernierStatut(LocalDateTime.now());
            rep.setCommentaireTechnique(""); // cahier: vide au départ

            // ⚠ commentaireClient : ton modèle Reparation ne stocke pas ça actuellement.
            // On le reçoit pour rester “cahier”, mais on ne le persiste pas ici.

            // copier proprement les appareils/causes vers l'entité gérée
            for (Appareil inA : appareils) {
                Appareil a = new Appareil();
                a.setImei(inA.getImei());
                a.setTypeAppareil(inA.getTypeAppareil());
                a.setDescription(inA.getDescription());
                rep.addAppareil(a);

                if (inA.getCauses() == null || inA.getCauses().isEmpty()) {
                    throw new ValidationException("Chaque appareil doit avoir au moins 1 cause.");
                }
                for (Cause inC : inA.getCauses()) {
                    Cause c = new Cause();
                    c.setTypeCause(inC.getTypeCause());
                    c.setDescription(inC.getDescription());
                    c.setCoutAvance(inC.getCoutAvance());
                    c.setCoutRestant(inC.getCoutRestant());
                    a.addCause(c);
                }
            }

            // avance optionnelle (si fournie) : on la trace comme Paiement AVANCE
            if (avanceOptionnelle != null) {
                double av = avanceOptionnelle;
                if (av < 0) throw new ValidationException("Avance invalide.");
                if (av > 0) {
                    double total = calculerTotalAReparer(rep);
                    if (av > total) throw new ValidationException("Avance > total à payer.");
                    Paiement p = new Paiement();
                    p.setMontant(av);
                    p.setTypePaiement(TypePaiement.AVANCE);
                    rep.addPaiement(p);
                }
            }

            session.persist(rep);
            return rep;
        });
    }

    @Override
    public void validerReparation(Long reparationId, Long userId) {
        changerStatut(reparationId, StatutReparation.EN_COURS, userId);
    }

    @Override
    public void changerStatut(Long reparationId, StatutReparation nouveauStatut) {
        // legacy : on suppose action du réparateur propriétaire de la réparation (pas idéal, mais on ne casse pas)
        changerStatut(reparationId, nouveauStatut, null);
    }

    @Override
    public void changerStatut(Long reparationId, StatutReparation nouveauStatut, Long userId) {
        if (reparationId == null) throw new ValidationException("Réparation obligatoire.");
        if (nouveauStatut == null) throw new ValidationException("Nouveau statut obligatoire.");

        HibernateTx.runInTx(session -> {

            Reparation rep = loadReparation(session, reparationId);
            if (rep == null) throw new NotFoundException("Réparation introuvable: " + reparationId);

            Boutique repBoutique = rep.getClient().getReparateur().getBoutique();

            // actor = user connecté, sinon fallback réparateur de la réparation (legacy)
            Boutique actorBoutique;
            if (userId != null) {
                Utilisateur user = session.get(Utilisateur.class, userId);
                if (user == null) throw new NotFoundException("Utilisateur introuvable: " + userId);
                actorBoutique = user.getBoutique();
            } else {
                actorBoutique = repBoutique;
            }

            // sécurité boutique
            assertSameBoutique(actorBoutique, repBoutique);

            StatutReparation from = rep.getStatut();
            StatutReparation to = nouveauStatut;

            if (!isTransitionAllowed(from, to)) {
                throw new ValidationException("Transition refusée: " + from + " -> " + to);
            }

            // LIVREE interdit si reste à payer > 0
            if (to == StatutReparation.LIVREE) {
                double reste = computeResteAPayer(session, rep);
                if (reste > 0.000001) {
                    throw new ValidationException("Livraison refusée : reste à payer = " + reste);
                }
            }

            rep.setStatut(to);
            rep.setDateDernierStatut(LocalDateTime.now());
            session.merge(rep);
        });
    }

    @Override
    public void ajouterCommentaireTechnique(Long reparationId, String texte, Long userId) {
        if (reparationId == null) throw new ValidationException("Réparation obligatoire.");
        if (userId == null) throw new ValidationException("Utilisateur connecté obligatoire.");
        if (texte == null || texte.isBlank()) throw new ValidationException("Commentaire vide.");

        HibernateTx.runInTx(session -> {

            Utilisateur user = session.get(Utilisateur.class, userId);
            if (user == null) throw new NotFoundException("Utilisateur introuvable: " + userId);

            Reparation rep = loadReparation(session, reparationId);
            if (rep == null) throw new NotFoundException("Réparation introuvable: " + reparationId);

            // sécurité boutique
            assertSameBoutique(user.getBoutique(), rep.getClient().getReparateur().getBoutique());

            // autorisé en EN_COURS ou TERMINEE
            StatutReparation st = rep.getStatut();
            if (!(st == StatutReparation.EN_COURS || st == StatutReparation.TERMINEE)) {
                throw new ValidationException("Commentaire technique interdit au statut: " + st);
            }

            rep.setCommentaireTechnique(texte);
            rep.setDateDernierStatut(LocalDateTime.now());
            session.merge(rep);
        });
    }

    @Override
    public Reparation trouverParId(Long reparationId) {
        return HibernateTx.callInTx(session -> loadReparation(session, reparationId));
    }

    @Override
    public Reparation trouverParId(Long reparationId, Long userId) {
        if (userId == null) throw new ValidationException("Utilisateur connecté obligatoire.");
        return HibernateTx.callInTx(session -> {
            Utilisateur user = session.get(Utilisateur.class, userId);
            if (user == null) throw new NotFoundException("Utilisateur introuvable: " + userId);
            Reparation rep = loadReparation(session, reparationId);
            if (rep == null) throw new NotFoundException("Réparation introuvable: " + reparationId);
            assertSameBoutique(user.getBoutique(), rep.getClient().getReparateur().getBoutique());
            return rep;
        });
    }

    @Override
    public List<Reparation> rechercher(String query, Long reparateurId, StatutReparation filtreStatut) {
        if (reparateurId == null) throw new ValidationException("Réparateur obligatoire.");

        if (filtreStatut != null) {
            if (query == null || query.isBlank()) return reparationDao.findByReparateurAndStatut(reparateurId, filtreStatut);
            // filtreStatut + query : on fait simple côté service
            return HibernateTx.callInTx(session ->
                    session.createQuery(
                                    "select r from Reparation r " +
                                            "where r.client.reparateur.id = :rid " +
                                            "and r.statut = :st " +
                                            "and lower(r.client.nom) like :q " +
                                            "order by r.dateCreation desc",
                                    Reparation.class
                            )
                            .setParameter("rid", reparateurId)
                            .setParameter("st", filtreStatut)
                            .setParameter("q", "%" + query.trim().toLowerCase() + "%")
                            .getResultList()
            );
        }

        if (query == null || query.isBlank()) return reparationDao.findByReparateur(reparateurId);
        return reparationDao.searchByClientName(reparateurId, query);
    }

    // -------------------- Helpers --------------------

    private Reparation loadReparation(org.hibernate.Session session, Long id) {
        if (id == null) return null;
        return session.createQuery(
                        "select distinct r " +
                                "from Reparation r " +
                                "join fetch r.client c " +
                                "join fetch c.reparateur rr " +
                                "left join fetch rr.boutique b " +
                                "left join fetch r.appareils a " +
                                "left join fetch a.causes ca " +
                                "left join fetch r.paiements p " +
                                "where r.id = :id",
                        Reparation.class
                )
                .setParameter("id", id)
                .uniqueResult();
    }

    private void assertSameBoutique(Boutique userBoutique, Boutique targetBoutique) {
        Long ub = userBoutique == null ? null : userBoutique.getId();
        Long tb = targetBoutique == null ? null : targetBoutique.getId();
        if (ub == null || tb == null || !ub.equals(tb)) {
            throw new ValidationException("Accès refusé (boutique différente).");
        }
    }

    private boolean isTransitionAllowed(StatutReparation from, StatutReparation to) {
        if (from == null || to == null) return false;

        // terminaux
        if (from == StatutReparation.LIVREE || from == StatutReparation.ANNULEE) return false;

        return switch (from) {
            case ENREGISTREE -> (to == StatutReparation.EN_COURS || to == StatutReparation.ANNULEE);
            case EN_COURS -> (to == StatutReparation.EN_ATTENTE_PIECES
                    || to == StatutReparation.TERMINEE
                    || to == StatutReparation.ANNULEE);
            case EN_ATTENTE_PIECES -> (to == StatutReparation.EN_COURS
                    || to == StatutReparation.TERMINEE
                    || to == StatutReparation.ANNULEE);
            case TERMINEE -> (to == StatutReparation.LIVREE);
            default -> false;
        };
    }

    private String generateCodeUnique(org.hibernate.Session session) {
        for (int i = 0; i < 10; i++) {
            String code = "R-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
            Long count = session.createQuery(
                            "select count(r) from Reparation r where r.codeUnique = :c",
                            Long.class
                    )
                    .setParameter("c", code)
                    .uniqueResult();
            long n = count == null ? 0L : count;
            if (n == 0L) return code;
        }
        throw new ValidationException("Impossible de générer un code unique.");
    }

    private void validateAndCheckImeis(org.hibernate.Session session, List<Appareil> appareils) {
        Set<String> seen = new HashSet<>();
        for (Appareil a : appareils) {
            if (a == null) throw new ValidationException("Appareil invalide.");
            if (a.getImei() == null || a.getImei().isBlank()) throw new ValidationException("IMEI obligatoire.");
            String imei = a.getImei().trim();
            if (!seen.add(imei)) throw new ValidationException("IMEI dupliqué dans la saisie: " + imei);

            Long count = session.createQuery(
                            "select count(x) from Appareil x where x.imei = :i",
                            Long.class
                    )
                    .setParameter("i", imei)
                    .uniqueResult();
            long n = count == null ? 0L : count;
            if (n > 0L) throw new ValidationException("IMEI déjà existant en base: " + imei);
        }
    }

    private double calculerTotalAReparer(Reparation rep) {
        double total = 0d;
        if (rep.getAppareils() == null) return 0d;

        for (Appareil a : rep.getAppareils()) {
            if (a == null || a.getCauses() == null) continue;
            for (Cause c : a.getCauses()) {
                if (c == null) continue;
                total += (c.getCoutAvance() + c.getCoutRestant());
            }
        }
        return total;
    }

    private double computeResteAPayer(org.hibernate.Session session, Reparation rep) {
        double total = calculerTotalAReparer(rep);

        Double dejaPaye = session.createQuery(
                        "select coalesce(sum(p.montant), 0) from Paiement p where p.reparation.id = :id",
                        Double.class
                )
                .setParameter("id", rep.getId())
                .uniqueResult();

        double paid = dejaPaye == null ? 0d : dejaPaye;
        return Math.max(0d, total - paid);
    }
}