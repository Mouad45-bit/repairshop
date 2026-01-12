package dao;

import model.Client;
import java.util.ArrayList;
import java.util.List;

public class ClientDao {
    private static List<Client> clientsDb = new ArrayList<>();

    public void save(Client client) {
        clientsDb.add(client);
    }

    // Le client tape son code unique pour voir l'avancement
    public Client findByCodeUnique(String code) {
        for (Client c : clientsDb) {
            if (c.getCodeUnique().equals(code)) {
                return c;
            }
        }
        return null;
    }
}