package com.maven.repairshop;

import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.*;
import com.maven.repairshop.model.enums.StatutReparation;
import com.maven.repairshop.model.enums.TypePaiement;
import com.maven.repairshop.service.PaiementService;
import com.maven.repairshop.service.ReparationService;
import com.maven.repairshop.service.SuiviService;
import com.maven.repairshop.service.dto.SuiviDTO;
import com.maven.repairshop.service.exceptions.ValidationException;
import com.maven.repairshop.service.impl.PaiementServiceImpl;
import com.maven.repairshop.service.impl.ReparationServiceImpl;
import com.maven.repairshop.service.impl.SuiviServiceImpl;

import java.util.List;
import java.util.UUID;

public class MainReparationTest {

    private record Seed(Long ownerId, Long repId, Long clientId, Long reparationId, String codeUnique) {}

    public static void main(String[] args) {

        ReparationService reparationService = new ReparationServiceImpl();
        PaiementService paiementService = new PaiementServiceImpl();
        SuiviService suiviService = new SuiviServiceImpl();

        Seed seed = seedData();

        // construire 1 appareil + 1 cause (total 300 = 100 avance + 200 restant)
        Appareil a = new Appareil();
        a.setImei("IMEI-" + UUID.randomUUID().toString().substring(0, 8));
        a.setTypeAppareil("Téléphone");
        a.setDescription("iPhone test");

        Cause c = new Cause();
        c.setTypeCause("Écran");
        c.setDescription("Remplacement écran");
        c.setCoutAvance(100);
        c.setCoutRestant(200);
        a.addCause(c);

        // créer avec avance optionnelle 100
        Reparation rep = reparationService.creerReparation(
                seed.clientId,
                seed.repId,
                List.of(a),
                "Commentaire client (non stocké si pas de champ)",
                100.0,
                seed.repId
        );

        System.out.println("Créée: statut=" + rep.getStatut() + ", code=" + rep.getCodeUnique());

        // valider ENREGISTREE -> EN_COURS
        reparationService.validerReparation(rep.getId(), seed.repId);
        System.out.println("Après validation: " + reparationService.trouverParId(rep.getId(), seed.repId).getStatut());

        // commentaire technique autorisé EN_COURS
        reparationService.ajouterCommentaireTechnique(rep.getId(), "Diagnostic: écran cassé.", seed.repId);

        // passer à TERMINEE
        reparationService.changerStatut(rep.getId(), StatutReparation.TERMINEE, seed.repId);
        System.out.println("Après TERMINEE: " + reparationService.trouverParId(rep.getId(), seed.repId).getStatut());

        // transition refusée (TERMINEE -> EN_COURS)
        try {
            reparationService.changerStatut(rep.getId(), StatutReparation.EN_COURS, seed.repId);
            System.out.println("ERREUR: transition devait être refusée");
        } catch (ValidationException ex) {
            System.out.println("OK refus transition: " + ex.getMessage());
        }

        // LIVREE refusée car reste à payer 200
        try {
            reparationService.changerStatut(rep.getId(), StatutReparation.LIVREE, seed.repId);
            System.out.println("ERREUR: LIVREE devait être refusée (reste à payer)");
        } catch (ValidationException ex) {
            System.out.println("OK refus LIVREE: " + ex.getMessage());
        }

        // propriétaire encaisse le solde 200
        paiementService.enregistrerPaiement(rep.getId(), 200, TypePaiement.RESTE, seed.ownerId);
        System.out.println("Reste=" + paiementService.resteAPayer(rep.getId(), seed.ownerId));

        // maintenant LIVREE ok
        reparationService.changerStatut(rep.getId(), StatutReparation.LIVREE, seed.ownerId);
        System.out.println("Après LIVREE: " + reparationService.trouverParId(rep.getId(), seed.ownerId).getStatut());

        // suivi client par code
        SuiviDTO suivi = suiviService.suivreParCode(rep.getCodeUnique());
        System.out.println("SuiviDTO: statut=" + suivi.getStatut()
                + ", date=" + suivi.getDateDernierStatut()
                + ", reste=" + suivi.getResteAPayer());
    }

    private static Seed seedData() {
        return HibernateTx.callInTx(session -> {

            Boutique b = new Boutique();
            b.setNom("Boutique A");
            session.persist(b);

            Proprietaire owner = new Proprietaire();
            owner.setNom("Owner");
            owner.setLogin("owner_" + UUID.randomUUID());
            owner.setPassword("pass");
            owner.setBoutique(b);
            session.persist(owner);

            Reparateur rep = new Reparateur();
            rep.setNom("Rep");
            rep.setLogin("rep_" + UUID.randomUUID());
            rep.setPassword("pass");
            rep.setBoutique(b);
            session.persist(rep);

            Client c = new Client();
            c.setNom("Client A");
            c.setReparateur(rep);
            session.persist(c);

            return new Seed(owner.getId(), rep.getId(), c.getId(), null, null);
        });
    }
}