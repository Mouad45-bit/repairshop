package dao;

import model.Reparation;
import java.util.ArrayList;
import java.util.List;

public class ReparationDao {
    private static List<Reparation> reparationsDb = new ArrayList<>();
    
    // Sauvegarder (Ajouter)
    public void save(Reparation reparation) {
        reparationsDb.add(reparation);
    }

    // Trouver une réparation précise par son ID
    public Reparation findById(Long id) {
        for (Reparation r : reparationsDb) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        return null;
    }

    // Lister TOUTES les réparations (le Service filtrera celles de la boutique)
    public List<Reparation> findAll() {
        return reparationsDb;
    }
}