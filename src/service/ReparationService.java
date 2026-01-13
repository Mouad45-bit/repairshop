package service;

import dao.ReparationDao;
import model.*;
import exception.MetierException; // C'est cette ligne qui posait problème avant
import java.util.ArrayList;
import java.util.List;

public class ReparationService {
    
    private ReparationDao reparationDao = new ReparationDao();

    // 1. Créer un dossier de réparation
    public void creerDossier(Utilisateur reparateur, Client client, String description, double prix) throws MetierException {
        // Sécurité : Seul un membre de la boutique peut créer pour SA boutique
        if (reparateur.getBoutique() == null) {
            throw new MetierException("Erreur : Le réparateur n'est assigné à aucune boutique.");
        }

        // On génère un ID (simulé avec le temps système)
        Long id = System.currentTimeMillis(); 
        Reparation rep = new Reparation(id, description, prix, client);
        
        reparationDao.save(rep);
        System.out.println("Succès : Dossier créé pour " + client.getNom());
    }

    // 2. Changer le statut (ex: passer de EN_COURS à TERMINEE)
    public void changerStatut(Utilisateur utilisateurConnecte, Long reparationId, Statut nouveauStatut) throws MetierException {
        Reparation rep = reparationDao.findById(reparationId);
        
        if (rep == null) {
            throw new MetierException("Réparation introuvable.");
        }

        // --- REGLE D'OR : VÉRIFICATION BOUTIQUE ---
        verifierAccesBoutique(utilisateurConnecte, rep);

        // Si c'est bon, on change
        rep.setStatut(nouveauStatut);
        System.out.println("Statut mis à jour : " + nouveauStatut);
    }

    // 3. Lister uniquement les réparations de MA boutique
    public List<Reparation> getReparationsDeMaBoutique(Utilisateur utilisateurConnecte) {
        List<Reparation> toutes = reparationDao.findAll();
        List<Reparation> filtrées = new ArrayList<>();

        for (Reparation r : toutes) {
            // Le chemin pour trouver la boutique : Reparation -> Client -> Createur -> Boutique
            Boutique b = r.getClient().getCreateur().getBoutique();
            
            // Si la boutique de la réparation est la même que celle de l'utilisateur
            if (b.getId().equals(utilisateurConnecte.getBoutique().getId())) {
                filtrées.add(r);
            }
        }
        return filtrées;
    }

    // --- Méthode privée (interne) pour ne pas répéter le code ---
    private void verifierAccesBoutique(Utilisateur user, Reparation rep) throws MetierException {
        // On remonte jusqu'à la boutique de la réparation
        Boutique boutiqueDossier = rep.getClient().getCreateur().getBoutique();
        Boutique boutiqueUser = user.getBoutique();

        // Si les ID sont différents, INTERDIT !
        if (!boutiqueDossier.getId().equals(boutiqueUser.getId())) {
            throw new MetierException("ACCÈS REFUSÉ : Ce dossier appartient à une autre boutique !");
        }
    }
}