package com.maven.repairshop.service.impl;

import com.maven.repairshop.dao.EmpruntDao;
import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.Boutique;
import com.maven.repairshop.model.Emprunt;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.model.Utilisateur;
import com.maven.repairshop.model.enums.StatutEmprunt;
import com.maven.repairshop.model.enums.TypeEmprunt;
import com.maven.repairshop.service.EmpruntService;
import com.maven.repairshop.service.exceptions.NotFoundException;
import com.maven.repairshop.service.exceptions.ValidationException;

import java.util.List;

public class EmpruntServiceImpl implements EmpruntService {

    private final EmpruntDao empruntDao = new EmpruntDao();

    // legacy
    @Override
    public Emprunt creer(Long reparateurId, TypeEmprunt type, String personne, double montant, String motif) {
        return creer(reparateurId, type, personne, montant, motif, reparateurId);
    }

    @Override
    public void changerStatut(Long empruntId, String nouveauStatut) {
        changerStatut(empruntId, nouveauStatut, null);
    }

    @Override
    public void supprimer(Long empruntId) {
        supprimer(empruntId, null);
    }

    @Override
    public List<Emprunt> lister(Long reparateurId) {
        return lister(reparateurId, reparateurId);
    }

    // sécurisé
    @Override
    public Emprunt creer(Long reparateurId, TypeEmprunt type, String personne, double montant, String motif, Long userId) {
        if (reparateurId == null) throw new ValidationException("Réparateur obligatoire.");
        if (type == null) throw new ValidationException("Type emprunt/prêt obligatoire.");
        if (personne == null || personne.isBlank()) throw new ValidationException("Nom de la personne obligatoire.");
        if (montant <= 0) throw new ValidationException("Le montant doit être > 0.");
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

            // boutique
            assertSameBoutique(user.getBoutique(), rep.getBoutique());

            // rôle : réparateur => seulement ses emprunts
            if (user instanceof Reparateur && !user.getId().equals(reparateurId)) {
                throw new ValidationException("Accès refusé : un réparateur ne gère que ses emprunts.");
            }

            Emprunt e = new Emprunt();
            e.setType(type);
            e.setNomPersonne(personne.trim());
            e.setMontant(montant);
            e.setMotif(motif);
            e.setReparateur(rep);
            e.setStatut(StatutEmprunt.EN_COURS);

            session.persist(e);
            return e;
        });
    }

    @Override
    public void changerStatut(Long empruntId, String nouveauStatut, Long userId) {
        if (empruntId == null) throw new ValidationException("Emprunt obligatoire.");
        if (nouveauStatut == null || nouveauStatut.isBlank()) throw new ValidationException("Statut obligatoire.");
        if (userId == null) throw new ValidationException("Utilisateur connecté obligatoire.");

        StatutEmprunt ns;
        try {
            ns = StatutEmprunt.valueOf(nouveauStatut.trim());
        } catch (Exception e) {
            throw new ValidationException("Statut invalide: " + nouveauStatut);
        }

        HibernateTx.runInTx(session -> {
            Utilisateur user = session.get(Utilisateur.class, userId);
            if (user == null) throw new NotFoundException("Utilisateur introuvable: " + userId);

            Emprunt emprunt = session.createQuery(
                            "select e from Emprunt e " +
                                    "join fetch e.reparateur r " +
                                    "left join fetch r.boutique b " +
                                    "where e.id = :id",
                            Emprunt.class
                    )
                    .setParameter("id", empruntId)
                    .uniqueResult();
            if (emprunt == null) throw new NotFoundException("Emprunt introuvable: " + empruntId);

            // boutique
            assertSameBoutique(user.getBoutique(), emprunt.getReparateur().getBoutique());

            // rôle
            if (user instanceof Reparateur && !user.getId().equals(emprunt.getReparateur().getId())) {
                throw new ValidationException("Accès refusé : emprunt d'un autre réparateur.");
            }

            StatutEmprunt from = emprunt.getStatut();

            // règles : impossible de revenir à EN_COURS + REMBOURSE terminal
            if (from == StatutEmprunt.REMBOURSE) {
                throw new ValidationException("Emprunt déjà remboursé (statut terminal).");
            }
            if (ns == StatutEmprunt.EN_COURS) {
                throw new ValidationException("Retour à EN_COURS interdit.");
            }

            // cahier : EN_COURS -> REMBOURSE (on tolère aussi PARTIELLEMENT_REMBOURSE -> REMBOURSE)
            if (from == StatutEmprunt.EN_COURS) {
                if (!(ns == StatutEmprunt.REMBOURSE || ns == StatutEmprunt.PARTIELLEMENT_REMBOURSE)) {
                    throw new ValidationException("Transition refusée: " + from + " -> " + ns);
                }
            } else if (from == StatutEmprunt.PARTIELLEMENT_REMBOURSE) {
                if (ns != StatutEmprunt.REMBOURSE) {
                    throw new ValidationException("Transition refusée: " + from + " -> " + ns);
                }
            }

            emprunt.setStatut(ns);
            session.merge(emprunt);
        });
    }

    @Override
    public void supprimer(Long empruntId, Long userId) {
        if (empruntId == null) throw new ValidationException("Emprunt obligatoire.");
        if (userId == null) throw new ValidationException("Utilisateur connecté obligatoire.");

        HibernateTx.runInTx(session -> {
            Utilisateur user = session.get(Utilisateur.class, userId);
            if (user == null) throw new NotFoundException("Utilisateur introuvable: " + userId);

            Emprunt emprunt = session.get(Emprunt.class, empruntId);
            if (emprunt == null) throw new NotFoundException("Emprunt introuvable: " + empruntId);

            // load boutique via reparateur
            Reparateur rep = session.createQuery(
                            "select r from Reparateur r left join fetch r.boutique b where r.id = :id",
                            Reparateur.class
                    )
                    .setParameter("id", emprunt.getReparateur().getId())
                    .uniqueResult();

            assertSameBoutique(user.getBoutique(), rep.getBoutique());

            if (user instanceof Reparateur && !user.getId().equals(rep.getId())) {
                throw new ValidationException("Accès refusé : emprunt d'un autre réparateur.");
            }

            session.remove(emprunt);
        });
    }

    @Override
    public List<Emprunt> lister(Long reparateurId, Long userId) {
        if (reparateurId == null) throw new ValidationException("Réparateur obligatoire.");
        if (userId == null) throw new ValidationException("Utilisateur connecté obligatoire.");

        // contrôle accès avant
        HibernateTx.runInTx(session -> {
            Utilisateur user = session.get(Utilisateur.class, userId);
            if (user == null) throw new NotFoundException("Utilisateur introuvable: " + userId);

            Reparateur rep = session.createQuery(
                            "select r from Reparateur r left join fetch r.boutique b where r.id = :id",
                            Reparateur.class
                    )
                    .setParameter("id", reparateurId)
                    .uniqueResult();
            if (rep == null) throw new NotFoundException("Réparateur introuvable: " + reparateurId);

            assertSameBoutique(user.getBoutique(), rep.getBoutique());

            if (user instanceof Reparateur && !user.getId().equals(reparateurId)) {
                throw new ValidationException("Accès refusé : un réparateur ne voit que ses emprunts.");
            }
        });

        return empruntDao.findByReparateur(reparateurId);
    }

    private void assertSameBoutique(Boutique ub, Boutique tb) {
        Long u = ub == null ? null : ub.getId();
        Long t = tb == null ? null : tb.getId();
        if (u == null || t == null || !u.equals(t)) {
            throw new ValidationException("Accès refusé (boutique différente).");
        }
    }
}