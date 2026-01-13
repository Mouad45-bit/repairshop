package service;

import dao.EmpruntDao;
import model.*;
import exception.MetierException;
import java.util.List;

public class EmpruntService {
    
    private EmpruntDao empruntDao = new EmpruntDao();

    public void preterMateriel(Utilisateur reparateur, String materiel, String nomClient) {
        // On crée l'emprunt lié au réparateur (donc à sa boutique)
        Emprunt emp = new Emprunt(System.currentTimeMillis(), materiel, nomClient, reparateur);
        empruntDao.save(emp);
        System.out.println("Succès : " + materiel + " prêté à " + nomClient);
    }

    public void retourMateriel(Long empruntId) throws MetierException {
        // On cherche l'emprunt (ici on simplifie, on suppose qu'on le trouve dans une liste globale du DAO)
        // Dans un vrai code, on ajouterait findById dans le Dao.
        // Pour faire simple, on va dire qu'on parcourt tout :
        List<Emprunt> tous = empruntDao.findAll();
        boolean trouve = false;

        for (Emprunt e : tous) {
            if (e.getId().equals(empruntId)) {
                e.marquerCommeRendu();
                trouve = true;
                System.out.println("Matériel rendu !");
                break;
            }
        }
        
        if (!trouve) {
            throw new MetierException("Emprunt introuvable.");
        }
    }
}