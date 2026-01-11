package com.maven.repairshop;

import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.model.enums.TypeEmprunt;
import com.maven.repairshop.service.EmpruntService;
import com.maven.repairshop.service.impl.EmpruntServiceImpl;
import com.maven.repairshop.dao.base.HibernateTx;

public class MainEmpruntTest {
    public static void main(String[] args) {

        // 1) créer un réparateur minimal en base (si tu n’as pas encore AuthService)
        Long reparateurId = HibernateTx.callInTx(session -> {
            Reparateur r = new Reparateur();
            r.setNom("Test Reparateur");
            r.setLogin("rep1");
            r.setPassword("pass");
            session.persist(r);
            return r.getId();
        });

        // 2) test service
        EmpruntService service = new EmpruntServiceImpl();
        var e = service.creer(reparateurId, TypeEmprunt.EMPRUNT, "Khalid", 500, "Achat pièces");

        System.out.println("Emprunt créé id=" + e.getId() + " montant=" + e.getMontant());
    }
}