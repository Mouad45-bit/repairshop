package com.maven.repairshop.dao;

import com.maven.repairshop.dao.base.BaseDao;
import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.Paiement;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DAO dédié aux paiements (avance/solde).
 *
 * NOTE: Les règles métier + sécurité (boutique/roles) restent dans les Services.
 */
public class PaiementDao extends BaseDao<Paiement> {

    public PaiementDao() {
        super(Paiement.class);
    }

    public List<Paiement> findByReparation(Long reparationId) {
        return HibernateTx.callInTx(session ->
                session.createQuery(
                                "from Paiement p where p.reparation.id = :rid order by p.datePaiement asc",
                                Paiement.class
                        )
                        .setParameter("rid", reparationId)
                        .getResultList()
        );
    }

    public double sumByReparation(Long reparationId) {
        return HibernateTx.callInTx(session -> {
            Double sum = session.createQuery(
                            "select coalesce(sum(p.montant), 0) from Paiement p where p.reparation.id = :rid",
                            Double.class
                    )
                    .setParameter("rid", reparationId)
                    .getSingleResult();
            return sum == null ? 0d : sum;
        });
    }

    /** Caisse brute réparateur (paiements), sur période */
    public double sumByReparateurAndPeriod(Long reparateurId, LocalDateTime from, LocalDateTime to) {
        return HibernateTx.callInTx(session -> {
            Double sum = session.createQuery(
                            "select coalesce(sum(p.montant), 0) " +
                                    "from Paiement p " +
                                    "join p.reparation r " +
                                    "join r.client c " +
                                    "join c.reparateur rep " +
                                    "where rep.id = :repId and p.datePaiement >= :from and p.datePaiement <= :to",
                            Double.class
                    )
                    .setParameter("repId", reparateurId)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getSingleResult();
            return sum == null ? 0d : sum;
        });
    }

    /** Caisse brute boutique (paiements), sur période */
    public double sumByBoutiqueAndPeriod(Long boutiqueId, LocalDateTime from, LocalDateTime to) {
        return HibernateTx.callInTx(session -> {
            Double sum = session.createQuery(
                            "select coalesce(sum(p.montant), 0) " +
                                    "from Paiement p " +
                                    "join p.reparation r " +
                                    "join r.client c " +
                                    "join c.reparateur rep " +
                                    "join rep.boutique b " +
                                    "where b.id = :bid and p.datePaiement >= :from and p.datePaiement <= :to",
                            Double.class
                    )
                    .setParameter("bid", boutiqueId)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getSingleResult();
            return sum == null ? 0d : sum;
        });
    }
}
