package com.maven.repairshop.service.impl;

import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.Appareil;
import com.maven.repairshop.model.Cause;
import com.maven.repairshop.model.Paiement;
import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.service.SuiviService;
import com.maven.repairshop.service.dto.SuiviDTO;
import com.maven.repairshop.service.exceptions.NotFoundException;
import com.maven.repairshop.service.exceptions.ValidationException;

public class SuiviServiceImpl implements SuiviService {

    @Override
    public SuiviDTO suivreParCode(String codeUnique) {
        if (codeUnique == null || codeUnique.isBlank()) throw new ValidationException("Code unique obligatoire.");

        return HibernateTx.callInTx(session -> {
            Reparation rep = session.createQuery(
                            "select distinct r " +
                                    "from Reparation r " +
                                    "join fetch r.client c " +
                                    "join fetch c.reparateur rr " +
                                    "left join fetch rr.boutique b " +
                                    "left join fetch r.appareils a " +
                                    "left join fetch a.causes ca " +
                                    "left join fetch r.paiements p " +
                                    "where r.codeUnique = :code",
                            Reparation.class
                    )
                    .setParameter("code", codeUnique.trim())
                    .uniqueResult();

            if (rep == null) throw new NotFoundException("Code invalide.");

            double total = calculerTotal(rep);
            double paid = sommePaiements(rep);
            double reste = Math.max(0d, total - paid);

            return new SuiviDTO(rep.getStatut(), rep.getDateDernierStatut(), reste);
        });
    }

    private double calculerTotal(Reparation rep) {
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

    private double sommePaiements(Reparation rep) {
        double s = 0d;
        if (rep.getPaiements() == null) return 0d;
        for (Paiement p : rep.getPaiements()) {
            if (p == null) continue;
            s += p.getMontant();
        }
        return s;
    }
}