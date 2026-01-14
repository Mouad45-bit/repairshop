package com.maven.repairshop.dao;

import com.maven.repairshop.dao.base.BaseDao;
import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.Boutique;

public class BoutiqueDao extends BaseDao<Boutique> {

    public BoutiqueDao() {
        super(Boutique.class);
    }

    public Boutique findById(Long id) {
        return super.findById(id);
    }

    public Boutique findByProprietaire(Long proprietaireId) {
        return HibernateTx.callInTx(session ->
                session.createQuery(
                                "from Boutique b where b.proprietaire.id = :pid",
                                Boutique.class
                        )
                        .setParameter("pid", proprietaireId)
                        .uniqueResult()
        );
    }
}