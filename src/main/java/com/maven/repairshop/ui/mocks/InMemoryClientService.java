package com.maven.repairshop.ui.mocks;

import com.maven.repairshop.model.Client;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.model.base.BaseEntity;
import com.maven.repairshop.service.ClientService;
import com.maven.repairshop.service.exceptions.NotFoundException;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryClientService implements ClientService {

    private final Map<Long, Client> store = new LinkedHashMap<>();
    private final AtomicLong seqClient = new AtomicLong(1);

    public InMemoryClientService() {
        seed();
    }

    @Override
    public Client creerClient(String nom, String telephone, String email, String adresse, String ville, Long reparateurId) {
        Client c = new Client();
        Long id = seqClient.getAndIncrement();
        setId(c, id);

        c.setNom(nom);
        c.setTelephone(telephone);
        c.setEmail(email);
        c.setAdresse(adresse);
        c.setVille(ville);

        // Attacher réparateur (utile pour filtrer)
        Reparateur r = new Reparateur();
        setId(r, reparateurId != null ? reparateurId : 1L);
        r.setNom("Réparateur " + r.getId());
        r.setLogin("rep" + r.getId());
        r.setPassword("mock");
        c.setReparateur(r);

        store.put(id, c);
        return c;
    }

    @Override
    public void modifierClient(Long clientId, String nom, String telephone, String email, String adresse, String ville) {
        Client c = store.get(clientId);
        if (c == null) throw new NotFoundException("Client introuvable: id=" + clientId);

        c.setNom(nom);
        c.setTelephone(telephone);
        c.setEmail(email);
        c.setAdresse(adresse);
        c.setVille(ville);
    }

    @Override
    public void supprimerClient(Long clientId) {
        if (!store.containsKey(clientId)) throw new NotFoundException("Client introuvable: id=" + clientId);
        store.remove(clientId);
    }

    @Override
    public Client trouverParId(Long clientId) {
        Client c = store.get(clientId);
        if (c == null) throw new NotFoundException("Client introuvable: id=" + clientId);
        return c;
    }

    @Override
    public List<Client> rechercher(String query, Long reparateurId) {
        String q = query == null ? "" : query.trim().toLowerCase();

        return store.values().stream()
                // filtre réparateur
                .filter(c -> {
                    if (reparateurId == null) return true;
                    if (c.getReparateur() == null) return false;
                    return Objects.equals(c.getReparateur().getId(), reparateurId);
                })
                // filtre query (nom / tel / email / ville)
                .filter(c -> {
                    if (q.isEmpty()) return true;
                    String nom = safe(c.getNom()).toLowerCase();
                    String tel = safe(c.getTelephone()).toLowerCase();
                    String email = safe(c.getEmail()).toLowerCase();
                    String ville = safe(c.getVille()).toLowerCase();
                    return nom.contains(q) || tel.contains(q) || email.contains(q) || ville.contains(q);
                })
                .collect(Collectors.toList());
    }

    // -------- seed --------

    private void seed() {
        creerClient("Amine K.", "06 00 00 00 01", "amine@mail.com", "Rue 1", "Casablanca", 1L);
        creerClient("Sara B.",  "06 00 00 00 02", "sara@mail.com",  "Rue 2", "Rabat",      1L);
        creerClient("Nadia L.", "06 00 00 00 03", "nadia@mail.com", "Rue 3", "Casablanca", 1L);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static void setId(Object entity, Long id) {
        try {
            Field f = BaseEntity.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de setter l'id (mock InMemoryClientService)", e);
        }
    }
}