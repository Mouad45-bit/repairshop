package dao;

import model.Emprunt;
import java.util.ArrayList;
import java.util.List;

public class EmpruntDao {
    private static List<Emprunt> empruntsDb = new ArrayList<>();

    public void save(Emprunt emprunt) {
        empruntsDb.add(emprunt);
    }

    public List<Emprunt> findAll() {
        return empruntsDb;
    }
}