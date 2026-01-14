package com.maven.repairshop;

import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.*;
import com.maven.repairshop.model.enums.TypePaiement;
import com.maven.repairshop.service.CaisseService;
import com.maven.repairshop.service.PaiementService;
import com.maven.repairshop.service.exceptions.ValidationException;
import com.maven.repairshop.service.impl.CaisseServiceImpl;
import com.maven.repairshop.service.impl.PaiementServiceImpl;

import java.time.LocalDateTime;
import java.util.UUID;

public class MainPaiementTest {

    public static void main(String[] args) {

        Seed seed = HibernateTx.callInTx(session -> {

            // owner d'abord (boutique nullable ici)
            Proprietaire owner = new Proprietaire();
            owner.setNom("Owner");
            owner.setLogin("owner1_" + UUID.randomUUID());
            owner.setPassword("pass");
            session.persist(owner);

            // boutique (proprietaire obligatoire)
            Boutique b = new Boutique();
            b.setNom("Boutique Test");
            b.setProprietaire(owner);
            session.persist(b);

            // rattacher owner + rep à boutique
            owner.setBoutique(b);
            session.merge(owner);

            Reparateur rep = new Reparateur();
            rep.setNom("Rep");
            rep.setLogin("rep1_" + UUID.randomUUID());
            rep.setPassword("pass");
            rep.setBoutique(b);
            session.persist(rep);

            Client c = new Client();
            c.setNom("Client A");
            c.setReparateur(rep);
            session.persist(c);

            Reparation r = new Reparation();
            r.setClient(c);
            r.setCodeUnique("R-" + UUID.randomUUID().toString().substring(0, 10));
            session.persist(r);

            Appareil a = new Appareil();
            a.setImei("IMEI-" + UUID.randomUUID().toString().substring(0, 8));
            a.setTypeAppareil("Téléphone");
            a.setDescription("Test");
            r.addAppareil(a);
            session.persist(a);

            Cause cause = new Cause();
            cause.setTypeCause("Écran");
            cause.setDescription("Remplacement écran");
            cause.setCoutAvance(100);
            cause.setCoutRestant(200);
            a.addCause(cause);
            session.persist(cause);

            return new Seed(b.getId(), owner.getId(), rep.getId(), r.getId());
        });

        PaiementService paiementService = new PaiementServiceImpl();
        CaisseService caisseService = new CaisseServiceImpl();

        System.out.println("Reste initial=" + paiementService.resteAPayer(seed.reparationId, seed.repId)); // 300

        paiementService.enregistrerPaiement(seed.reparationId, 100, TypePaiement.AVANCE, seed.repId);
        System.out.println("Reste après avance=" + paiementService.resteAPayer(seed.reparationId, seed.repId)); // 200

        try {
            paiementService.enregistrerPaiement(seed.reparationId, 500, TypePaiement.RESTE, seed.repId);
            System.out.println("ERREUR: on devait refuser dépassement");
        } catch (ValidationException ex) {
            System.out.println("OK refus dépassement: " + ex.getMessage());
        }

        // propriétaire peut encaisser le solde
        paiementService.enregistrerPaiement(seed.reparationId, 200, TypePaiement.RESTE, seed.ownerId);
        System.out.println("Reste après solde=" + paiementService.resteAPayer(seed.reparationId, seed.ownerId)); // 0

        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now().plusDays(1);

        System.out.println("Caisse réparateur=" + caisseService.caisseReparateur(seed.repId, from, to, seed.repId)); // 300
        System.out.println("Caisse boutique=" + caisseService.caisseBoutique(seed.boutiqueId, from, to, seed.ownerId)); // 300
    }

    private record Seed(Long boutiqueId, Long ownerId, Long repId, Long reparationId) {}
}