package service;

import dao.UtilisateurDao;
import model.Utilisateur;
import exception.MetierException;

public class AuthService {
    // Le service a besoin du DAO pour chercher les infos
    private UtilisateurDao utilisateurDao = new UtilisateurDao();

    public Utilisateur login(String login, String password) throws MetierException {
        // 1. Vérification basique
        if (login == null || password == null) {
            throw new MetierException("Le login et le mot de passe sont obligatoires.");
        }

        // 2. On cherche l'utilisateur
        Utilisateur user = utilisateurDao.findByLogin(login);
        
        if (user == null) {
            throw new MetierException("Utilisateur inconnu.");
        }

        // 3. On vérifie le mot de passe
        // Note: Dans la vraie vie, on utilise un hash (BCrypt). Ici on compare le texte.
        if (!user.getPassword().equals(password)) {
            throw new MetierException("Mot de passe incorrect.");
        }

        // 4. Si tout est bon, on retourne l'utilisateur connecté
        System.out.println("CONNEXION REUSSIE : Bienvenue " + user.getLogin());
        return user;
    }
}