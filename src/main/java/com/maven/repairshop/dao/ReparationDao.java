package com.maven.repairshop.dao;

import com.maven.repairshop.dao.base.BaseDao;
import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.model.enums.StatutReparation;

import java.util.List;

public class ReparationDao extends BaseDao<Reparation> {

    public ReparationDao() {
        super(Reparation.class);
    }

    public Reparation findByCodeUnique(String code) {
        return HibernateTx.callInTx(session ->
                session.createQuery(
                                "from Reparation r where r.codeUnique = :c",
                                Reparation.class
                        )
                        .setParameter("c", code)
                        .uniqueResult()
        );
    }

    public List<Reparation> findByReparateur(Long repId) {
        return HibernateTx.callInTx(session ->
                session.createQuery(
                                "select r from Reparation r " +
                                        "where r.client.reparateur.id = :rid " +
                                        "order by r.dateCreation desc",
                                Reparation.class
                        )
                        .setParameter("rid", repId)
                        .getResultList()
        );
    }

    public List<Reparation> findByReparateurAndStatut(Long repId, StatutReparation s) {
        return HibernateTx.callInTx(session ->
                session.createQuery(
                                "select r from Reparation r " +
                                        "where r.client.reparateur.id = :rid and r.statut = :st " +
                                        "order by r.dateCreation desc",
                                Reparation.class
                        )
                        .setParameter("rid", repId)
                        .setParameter("st", s)
                        .getResultList()
        );
    }

    public List<Reparation> searchByClientName(Long repId, String q) {
        String qq = q == null ? "" : q.trim().toLowerCase();
        return HibernateTx.callInTx(session ->
                session.createQuery(
                                "select r from Reparation r " +
                                        "where r.client.reparateur.id = :rid " +
                                        "and lower(r.client.nom) like :q " +
                                        "order by r.dateCreation desc",
                                Reparation.class
                        )
                        .setParameter("rid", repId)
                        .setParameter("q", "%" + qq + "%")
                        .getResultList()
        );
    }
}