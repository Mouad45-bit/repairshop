package dao;

import model.Boutique;
import java.util.ArrayList;
import java.util.List;

public class BoutiqueDao {
    private static List<Boutique> boutiquesDb = new ArrayList<>();

    public void save(Boutique boutique) {
        boutiquesDb.add(boutique);
    }

    public Boutique findById(Long id) {
        for (Boutique b : boutiquesDb) {
            if (b.getId().equals(id)) {
                return b;
            }
        }
        return null;
    }
}