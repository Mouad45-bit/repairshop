package com.maven.repairshop.service.impl;

import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.Boutique;
import com.maven.repairshop.model.Proprietaire;
import com.maven.repairshop.model.Utilisateur;
import com.maven.repairshop.model.enums.StatutReparation;
import com.maven.repairshop.service.StatistiquesService;
import com.maven.repairshop.service.exceptions.NotFoundException;
import com.maven.repairshop.service.exceptions.ValidationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class StatistiquesServiceImpl implements StatistiquesService {

    @Override
    public long nbReparationsParPeriode(Long boutiqueId, LocalDateTime from, LocalDateTime to, Long userId) {
        checkInputs(boutiqueId, from, to, userId);

        return HibernateTx.callInTx(session -> {
            Utilisateur user = requireUser(session, userId);

            // règle métier: stats boutique = propriétaire seulement
            requireProprietaire(user);

            // séparation boutique
            assertSameBoutiqueId(user.getBoutique(), boutiqueId);

            Long n = session.createQuery(
                            "select count(r) " +
                                    "from Reparation r " +
                                    "where r.client.reparateur.boutique.id = :bid " +
                                    "and r.dateCreation between :f and :t",
                            Long.class
                    )
                    .setParameter("bid", boutiqueId)
                    .setParameter("f", from)
                    .setParameter("t", to)
                    .uniqueResult();

            return n == null ? 0L : n;
        });
    }

    @Override
    public Map<String, Long> termineesVsEnCours(Long boutiqueId, LocalDateTime from, LocalDateTime to, Long userId) {
        checkInputs(boutiqueId, from, to, userId);

        return HibernateTx.callInTx(session -> {
            Utilisateur user = requireUser(session, userId);

            // règle métier: stats boutique = propriétaire seulement
            requireProprietaire(user);

            // séparation boutique
            assertSameBoutiqueId(user.getBoutique(), boutiqueId);

            Long terminees = session.createQuery(
                            "select count(r) from Reparation r " +
                                    "where r.client.reparateur.boutique.id = :bid " +
                                    "and r.dateCreation between :f and :t " +
                                    "and r.statut in (:s1, :s2)",
                            Long.class
                    )
                    .setParameter("bid", boutiqueId)
                    .setParameter("f", from)
                    .setParameter("t", to)
                    .setParameter("s1", StatutReparation.TERMINEE)
                    .setParameter("s2", StatutReparation.LIVREE)
                    .uniqueResult();

            Long enCours = session.createQuery(
                            "select count(r) from Reparation r " +
                                    "where r.client.reparateur.boutique.id = :bid " +
                                    "and r.dateCreation between :f and :t " +
                                    "and r.statut in (:s1, :s2, :s3)",
                            Long.class
                    )
                    .setParameter("bid", boutiqueId)
                    .setParameter("f", from)
                    .setParameter("t", to)
                    .setParameter("s1", StatutReparation.ENREGISTREE)
                    .setParameter("s2", StatutReparation.EN_COURS)
                    .setParameter("s3", StatutReparation.EN_ATTENTE_PIECES)
                    .uniqueResult();

            Map<String, Long> m = new HashMap<>();
            m.put("TERMINEES", terminees == null ? 0L : terminees);
            m.put("EN_COURS", enCours == null ? 0L : enCours);
            return m;
        });
    }

    @Override
    public double chiffreAffairesParPeriode(Long boutiqueId, LocalDateTime from, LocalDateTime to, Long userId) {
        checkInputs(boutiqueId, from, to, userId);

        return HibernateTx.callInTx(session -> {
            Utilisateur user = requireUser(session, userId);

            // règle métier: stats boutique = propriétaire seulement
            requireProprietaire(user);

            // séparation boutique
            assertSameBoutiqueId(user.getBoutique(), boutiqueId);

            Double sum = session.createQuery(
                            "select coalesce(sum(p.montant), 0) " +
                                    "from Paiement p " +
                                    "where p.reparation.client.reparateur.boutique.id = :bid " +
                                    "and p.datePaiement between :f and :t",
                            Double.class
                    )
                    .setParameter("bid", boutiqueId)
                    .setParameter("f", from)
                    .setParameter("t", to)
                    .uniqueResult();

            return sum == null ? 0d : sum;
        });
    }

    // -------------------- Helpers --------------------

    private void checkInputs(Long boutiqueId, LocalDateTime from, LocalDateTime to, Long userId) {
        if (boutiqueId == null) throw new ValidationException("Boutique obligatoire.");
        if (from == null || to == null) throw new ValidationException("Période obligatoire.");
        if (to.isBefore(from)) throw new ValidationException("Période invalide (to < from).");
        if (userId == null) throw new ValidationException("Utilisateur connecté obligatoire.");
    }

    private Utilisateur requireUser(org.hibernate.Session session, Long userId) {
        Utilisateur user = session.get(Utilisateur.class, userId);
        if (user == null) throw new NotFoundException("Utilisateur introuvable: " + userId);
        return user;
    }

    private void requireProprietaire(Utilisateur user) {
        if (!(user instanceof Proprietaire)) {
            throw new ValidationException("Accès refusé: statistiques réservées au propriétaire.");
        }
    }

    private void assertSameBoutiqueId(Boutique userBoutique, Long boutiqueId) {
        Long ub = userBoutique == null ? null : userBoutique.getId();
        if (ub == null || !ub.equals(boutiqueId)) {
            throw new ValidationException("Accès refusé (boutique différente).");
        }
    }
}