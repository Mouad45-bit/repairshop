package com.maven.repairshop.dao;

import com.maven.repairshop.dao.base.BaseDao;
import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.Emprunt;
import com.maven.repairshop.model.enums.StatutEmprunt;

import java.util.List;

public class EmpruntDao extends BaseDao<Emprunt> {

    public EmpruntDao() {
        super(Emprunt.class);
    }

    public List<Emprunt> findByReparateur(Long reparateurId) {
        return HibernateTx.callInTx(session ->
                session.createQuery(
                                "from Emprunt e where e.reparateur.id = :rid order by e.dateEmprunt desc",
                                Emprunt.class
                        )
                        .setParameter("rid", reparateurId)
                        .getResultList()
        );
    }

    public List<Emprunt> findByReparateurAndStatut(Long reparateurId, StatutEmprunt statut) {
        return HibernateTx.callInTx(session ->
                session.createQuery(
                                "from Emprunt e where e.reparateur.id = :rid and e.statut = :st order by e.dateEmprunt desc",
                                Emprunt.class
                        )
                        .setParameter("rid", reparateurId)
                        .setParameter("st", statut)
                        .getResultList()
        );
    }
}