package service;

import dao.PaiementDao;
import model.*;
import java.util.List;

public class FinanceService {
    
    private PaiementDao paiementDao = new PaiementDao();

    // Encaisser de l'argent
    public void encaisser(Reparation rep, double montant, String type) {
        Paiement p = new Paiement(System.currentTimeMillis(), montant, type, rep);
        paiementDao.save(p);
        System.out.println("Caisse : +" + montant + " DH (" + type + ")");
    }

    // Calculer le total (Chiffre d'affaires) de la boutique de l'utilisateur connecté
    public double getChiffreAffaires(Utilisateur utilisateurConnecte) {
        double total = 0.0;
        List<Paiement> tousLesPaiements = paiementDao.findAll();

        for (Paiement p : tousLesPaiements) {
            // On remonte la chaîne : Paiement -> Reparation -> Client -> Createur -> Boutique
            Boutique boutiquePaiement = p.getReparation().getClient().getCreateur().getBoutique();

            // Si le paiement vient de MA boutique, je l'ajoute
            if (boutiquePaiement.getId().equals(utilisateurConnecte.getBoutique().getId())) {
                total = total + p.getMontant();
            }
        }
        return total;
    }
}