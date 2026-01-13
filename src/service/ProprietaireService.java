package service;

import dao.BoutiqueDao;
import dao.UtilisateurDao;
import model.*;
import exception.MetierException;

public class ProprietaireService {
    
    private BoutiqueDao boutiqueDao = new BoutiqueDao();
    private UtilisateurDao utilisateurDao = new UtilisateurDao();

    // Méthode pour créer une boutique
    public void creerBoutique(Utilisateur admin, String nomBoutique) throws MetierException {
        // Règle de sécurité : Vérifier que c'est bien un PROPRIETAIRE
        if (admin.getRole() != Role.PROPRIETAIRE) {
            throw new MetierException("DROIT REFUSÉ : Seul un propriétaire peut créer une boutique.");
        }

        // Création et sauvegarde
        // On met un ID null ou fake ici, le DAO ou la BDD s'en occupera
        Boutique nouvelleBoutique = new Boutique(System.currentTimeMillis(), nomBoutique);
        boutiqueDao.save(nouvelleBoutique);
        System.out.println("Succès : Boutique '" + nomBoutique + "' créée.");
    }

    // Méthode pour embaucher un réparateur
    public void creerReparateur(Utilisateur admin, String login, String password, Long boutiqueId) throws MetierException {
        // 1. Règle de sécurité
        if (admin.getRole() != Role.PROPRIETAIRE) {
            throw new MetierException("DROIT REFUSÉ : Seul un propriétaire peut créer un employé.");
        }

        // 2. Vérifier si le login est déjà pris
        if (utilisateurDao.existsByLogin(login)) {
            throw new MetierException("Erreur : Ce login est déjà utilisé.");
        }

        // 3. Vérifier que la boutique existe
        Boutique boutique = boutiqueDao.findById(boutiqueId);
        if (boutique == null) {
            throw new MetierException("Erreur : La boutique demandée n'existe pas.");
        }

        // 4. Création
        Utilisateur reparateur = new Utilisateur(null, login, password, Role.REPARATEUR, boutique);
        utilisateurDao.save(reparateur);
        System.out.println("Succès : Réparateur " + login + " embauché chez " + boutique.getNom());
    }
}