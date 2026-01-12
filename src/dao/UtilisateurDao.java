package dao;

import model.Utilisateur;
import model.Role;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDao {
    private static List<Utilisateur> utilisateursDb = new ArrayList<>();
    private static Long idCompteur = 1L; // Pour donner un ID unique (1, 2, 3...)

    public void save(Utilisateur user) {
        // On recrée l'utilisateur pour lui donner un ID automatique
        Utilisateur nouveau = new Utilisateur(
            idCompteur++, 
            user.getLogin(), 
            user.getPassword(), 
            user.getRole(), 
            user.getBoutique()
        );
        utilisateursDb.add(nouveau);
    }

    // Chercher par Login (ex: pour se connecter)
    public Utilisateur findByLogin(String login) {
        for (Utilisateur u : utilisateursDb) {
            if (u.getLogin().equals(login)) {
                return u;
            }
        }
        return null;
    }
    
    // Vérifier si un login existe déjà (pour éviter les doublons)
    public boolean existsByLogin(String login) {
        return findByLogin(login) != null;
    }
}