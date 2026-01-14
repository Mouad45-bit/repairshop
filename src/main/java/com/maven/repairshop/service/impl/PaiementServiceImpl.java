package com.maven.repairshop.service.impl;

import com.maven.repairshop.dao.PaiementDao;
import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.*;
import com.maven.repairshop.model.enums.StatutReparation;
import com.maven.repairshop.model.enums.TypePaiement;
import com.maven.repairshop.service.PaiementService;
import com.maven.repairshop.service.exceptions.NotFoundException;
import com.maven.repairshop.service.exceptions.ValidationException;

import java.util.List;

public class PaiementServiceImpl implements PaiementService {

    private final PaiementDao paiementDao = new PaiementDao();

    @Override
    public Paiement enregistrerPaiement(Long reparationId, double montant, TypePaiement type, Long userId) {

        if (reparationId == null) throw new ValidationException("Réparation obligatoire.");
        if (userId == null) throw new ValidationException("Utilisateur connecté obligatoire.");
        if (type == null) throw new ValidationException("Type paiement obligatoire.");
        if (montant <= 0) throw new ValidationException("Le montant doit être > 0.");

        return HibernateTx.callInTx(session -> {

            Utilisateur user = session.get(Utilisateur.class, userId);
            if (user == null) throw new NotFoundException("Utilisateur introuvable: " + userId);

            // Charger réparation + relations nécessaires (pour total + boutique check)
            Reparation rep = session.createQuery(
                            "select distinct r " +
                                    "from Reparation r " +
                                    "join fetch r.client c " +
                                    "join fetch c.reparateur rr " +
                                    "left join fetch rr.boutique b " +
                                    "left join fetch r.appareils a " +
                                    "left join fetch a.causes ca " +
                                    "where r.id = :id",
                            Reparation.class
                    )
                    .setParameter("id", reparationId)
                    .uniqueResult();

            if (rep == null) throw new NotFoundException("Réparation introuvable: " + reparationId);

            // ✅ Sécurité boutique sans boutique_id dans Réparation :
            Boutique userBoutique = user.getBoutique();
            Boutique repBoutique = rep.getClient().getReparateur().getBoutique();
            Long ub = userBoutique == null ? null : userBoutique.getId();
            Long rb = repBoutique == null ? null : repBoutique.getId();
            if (ub == null || rb == null || !ub.equals(rb)) {
                throw new ValidationException("Accès refusé (boutique différente).");
            }

            if (rep.getStatut() == StatutReparation.ANNULEE) {
                throw new ValidationException("Paiement interdit : réparation annulée.");
            }

            double total = calculerTotalAReparer(rep);

            Double dejaPaye = session.createQuery(
                            "select coalesce(sum(p.montant), 0) from Paiement p where p.reparation.id = :id",
                            Double.class
                    )
                    .setParameter("id", reparationId)
                    .uniqueResult();

            double paid = dejaPaye == null ? 0d : dejaPaye;
            double reste = total - paid;

            if (reste <= 0) throw new ValidationException("Aucun reste à payer.");
            if (montant > reste + 0.000001) {
                throw new ValidationException("Dépassement : déjà payé=" + paid + ", total=" + total + ", reste=" + reste);
            }

            // RESTE = SOLDE
            Paiement p = new Paiement();
            p.setMontant(montant);
            p.setTypePaiement(type);
            p.setReparation(rep);
            session.persist(p);

            return p;
        });
    }

    @Override
    public double resteAPayer(Long reparationId, Long userId) {
        if (reparationId == null) throw new ValidationException("Réparation obligatoire.");
        if (userId == null) throw new ValidationException("Utilisateur connecté obligatoire.");

        return HibernateTx.callInTx(session -> {

            Utilisateur user = session.get(Utilisateur.class, userId);
            if (user == null) throw new NotFoundException("Utilisateur introuvable: " + userId);

            Reparation rep = session.createQuery(
                            "select distinct r " +
                                    "from Reparation r " +
                                    "join fetch r.client c " +
                                    "join fetch c.reparateur rr " +
                                    "left join fetch rr.boutique b " +
                                    "left join fetch r.appareils a " +
                                    "left join fetch a.causes ca " +
                                    "where r.id = :id",
                            Reparation.class
                    )
                    .setParameter("id", reparationId)
                    .uniqueResult();

            if (rep == null) throw new NotFoundException("Réparation introuvable: " + reparationId);

            Boutique userBoutique = user.getBoutique();
            Boutique repBoutique = rep.getClient().getReparateur().getBoutique();
            Long ub = userBoutique == null ? null : userBoutique.getId();
            Long rb = repBoutique == null ? null : repBoutique.getId();
            if (ub == null || rb == null || !ub.equals(rb)) {
                throw new ValidationException("Accès refusé (boutique différente).");
            }

            double total = calculerTotalAReparer(rep);

            Double dejaPaye = session.createQuery(
                            "select coalesce(sum(p.montant), 0) from Paiement p where p.reparation.id = :id",
                            Double.class
                    )
                    .setParameter("id", reparationId)
                    .uniqueResult();

            double paid = dejaPaye == null ? 0d : dejaPaye;
            return Math.max(0d, total - paid);
        });
    }

    @Override
    public List<Paiement> listerPaiements(Long reparationId, Long userId) {
        // check boutique + accès
        resteAPayer(reparationId, userId);
        return paiementDao.findByReparation(reparationId);
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
}

