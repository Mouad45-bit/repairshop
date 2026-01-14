package com.maven.repairshop;

import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.*;
import com.maven.repairshop.model.enums.TypePaiement;
import com.maven.repairshop.service.PaiementService;
import com.maven.repairshop.service.StatistiquesService;
import com.maven.repairshop.service.impl.PaiementServiceImpl;
import com.maven.repairshop.service.impl.StatistiquesServiceImpl;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class MainStatsTest {

    private record Seed(Long boutiqueA, Long ownerA, Long repA, Long boutiqueB, Long ownerB, Long repB, Long repA_reparationId) {}

    public static void main(String[] args) {
        Seed s = seed();

        StatistiquesService stats = new StatistiquesServiceImpl();
        PaiementService paiementService = new PaiementServiceImpl();

        LocalDateTime from = LocalDateTime.now().minusDays(2);
        LocalDateTime to = LocalDateTime.now().plusDays(1);

        // Boutique A : on a 1 réparation + paiements
        long nbA = stats.nbReparationsParPeriode(s.boutiqueA, from, to, s.ownerA);
        System.out.println("nb réparations boutiqueA=" + nbA);

        Map<String, Long> tauxA = stats.termineesVsEnCours(s.boutiqueA, from, to, s.ownerA);
        System.out.println("terminees vs en cours A=" + tauxA);

        double caA = stats.chiffreAffairesParPeriode(s.boutiqueA, from, to, s.ownerA);
        System.out.println("CA boutiqueA=" + caA);

        // sécurité : ownerB ne doit pas lire stats de boutiqueA
        try {
            stats.nbReparationsParPeriode(s.boutiqueA, from, to, s.ownerB);
            System.out.println("ERREUR: accès inter-boutique devait être refusé");
        } catch (Exception ex) {
            System.out.println("OK refus inter-boutique: " + ex.getMessage());
        }

        // payer sur la réparation A
        paiementService.enregistrerPaiement(s.repA_reparationId, 50, TypePaiement.AVANCE, s.ownerA);

        double caA2 = stats.chiffreAffairesParPeriode(s.boutiqueA, from, to, s.ownerA);
        System.out.println("CA boutiqueA après paiement=" + caA2);
    }

    private static Seed seed() {
        return HibernateTx.callInTx(session -> {

            // Boutique A
            Boutique a = new Boutique();
            a.setNom("Boutique A");
            session.persist(a);

            Proprietaire ownerA = new Proprietaire();
            ownerA.setNom("OwnerA");
            ownerA.setLogin("ownerA_" + UUID.randomUUID());
            ownerA.setPassword("pass");
            ownerA.setBoutique(a);
            session.persist(ownerA);

            Reparateur repA = new Reparateur();
            repA.setNom("RepA");
            repA.setLogin("repA_" + UUID.randomUUID());
            repA.setPassword("pass");
            repA.setBoutique(a);
            session.persist(repA);

            Client cA = new Client();
            cA.setNom("Client A");
            cA.setReparateur(repA);
            session.persist(cA);

            Reparation rA = new Reparation();
            rA.setClient(cA);
            rA.setCodeUnique("R-" + UUID.randomUUID().toString().substring(0, 10));
            session.persist(rA);

            // Boutique B
            Boutique b = new Boutique();
            b.setNom("Boutique B");
            session.persist(b);

            Proprietaire ownerB = new Proprietaire();
            ownerB.setNom("OwnerB");
            ownerB.setLogin("ownerB_" + UUID.randomUUID());
            ownerB.setPassword("pass");
            ownerB.setBoutique(b);
            session.persist(ownerB);

            Reparateur repB = new Reparateur();
            repB.setNom("RepB");
            repB.setLogin("repB_" + UUID.randomUUID());
            repB.setPassword("pass");
            repB.setBoutique(b);
            session.persist(repB);

            Client cB = new Client();
            cB.setNom("Client B");
            cB.setReparateur(repB);
            session.persist(cB);

            Reparation rB = new Reparation();
            rB.setClient(cB);
            rB.setCodeUnique("R-" + UUID.randomUUID().toString().substring(0, 10));
            session.persist(rB);

            return new Seed(a.getId(), ownerA.getId(), repA.getId(),
                    b.getId(), ownerB.getId(), repB.getId(),
                    rA.getId());
        });
    }
}