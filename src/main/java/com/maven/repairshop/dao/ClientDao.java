package com.maven.repairshop.dao;

import com.maven.repairshop.dao.base.BaseDao;
import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.Client;

import java.util.List;

public class ClientDao extends BaseDao<Client> {

    public ClientDao() {
        super(Client.class);
    }

    public List<Client> findByReparateur(Long reparateurId) {
        return HibernateTx.callInTx(session ->
                session.createQuery(
                                "from Client c where c.reparateur.id = :rid order by c.nom asc",
                                Client.class
                        )
                        .setParameter("rid", reparateurId)
                        .getResultList()
        );
    }

    public List<Client> searchByNom(Long reparateurId, String q) {
        String qq = q == null ? "" : q.trim().toLowerCase();
        return HibernateTx.callInTx(session ->
                session.createQuery(
                                "from Client c " +
                                        "where c.reparateur.id = :rid " +
                                        "and lower(c.nom) like :q " +
                                        "order by c.nom asc",
                                Client.class
                        )
                        .setParameter("rid", reparateurId)
                        .setParameter("q", "%" + qq + "%")
                        .getResultList()
        );
    }

    public long countReparationsByClient(Long clientId) {
        return HibernateTx.callInTx(session -> {
            Long n = session.createQuery(
                            "select count(r) from Reparation r where r.client.id = :cid",
                            Long.class
                    )
                    .setParameter("cid", clientId)
                    .uniqueResult();
            return n == null ? 0L : n;
        });
    }
}