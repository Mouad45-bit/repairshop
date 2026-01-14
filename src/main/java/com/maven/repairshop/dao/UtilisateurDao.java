package com.maven.repairshop.dao;

import com.maven.repairshop.dao.base.BaseDao;
import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.model.Utilisateur;

import java.util.List;

public class UtilisateurDao extends BaseDao<Utilisateur> {

    public UtilisateurDao() {
        super(Utilisateur.class);
    }

    public Utilisateur findByLogin(String login) {
        return HibernateTx.callInTx(session ->
                session.createQuery(
                                "from Utilisateur u where u.login = :login",
                                Utilisateur.class
                        )
                        .setParameter("login", login)
                        .uniqueResult()
        );
    }

    public boolean existsByLogin(String login) {
        return findByLogin(login) != null;
    }

    public List<Reparateur> findReparateursByBoutique(Long boutiqueId) {
        return HibernateTx.callInTx(session ->
                session.createQuery(
                                "from Reparateur r where r.boutique.id = :bid order by r.nom asc",
                                Reparateur.class
                        )
                        .setParameter("bid", boutiqueId)
                        .getResultList()
        );
    }
}