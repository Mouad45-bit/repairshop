package dao;

import model.Paiement;
import java.util.ArrayList;
import java.util.List;

public class PaiementDao {
    private static List<Paiement> paiementsDb = new ArrayList<>();

    public void save(Paiement paiement) {
        paiementsDb.add(paiement);
    }
    
    public List<Paiement> findAll() {
        return paiementsDb;
    }
}