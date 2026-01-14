package com.maven.repairshop;

import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.Proprietaire;
import com.maven.repairshop.service.AuthService;
import com.maven.repairshop.service.ProprietaireService;
import com.maven.repairshop.service.impl.AuthServiceImpl;
import com.maven.repairshop.service.impl.ProprietaireServiceImpl;
import com.maven.repairshop.util.PasswordUtil;

public class MainAuthTest {

    public static void main(String[] args) {

        // 1) créer un propriétaire en base (socle)
        Long proprietaireId = HibernateTx.callInTx(session -> {
            Proprietaire p = new Proprietaire();
            p.setNom("Owner Test");
            p.setLogin("owner1");
            p.setPassword(PasswordUtil.hash("ownerpass"));
            session.persist(p);
            return p.getId();
        });

        ProprietaireService proprietaireService = new ProprietaireServiceImpl();
        AuthService authService = new AuthServiceImpl();

        // 2) créer boutique
        var boutique = proprietaireService.creerBoutique(
                proprietaireId,
                "Boutique Alpha",
                "Rue 1",
                "0600000000"
        );
        System.out.println("Boutique créée id=" + boutique.getId() + " nom=" + boutique.getNom());

        // 3) créer réparateur
        var rep = proprietaireService.creerReparateur(
                boutique.getId(),
                "Reparateur 1",
                "rep1",
                "reppass"
        );
        System.out.println("Réparateur créé id=" + rep.getId() + " login=" + rep.getLogin());

        // 4) login OK
        var u1 = authService.login("rep1", "reppass");
        System.out.println("Login OK => " + u1.getClass().getSimpleName() + " id=" + u1.getId());

        // 5) login KO
        try {
            authService.login("rep1", "bad");
            System.out.println("ERREUR: aurait dû échouer");
        } catch (Exception e) {
            System.out.println("Login KO attendu => " + e.getClass().getSimpleName() + " : " + e.getMessage());
        }
    }
}