package com.maven.repairshop.ui.mocks;

import com.maven.repairshop.model.Client;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.model.base.BaseEntity;
import com.maven.repairshop.model.enums.StatutReparation;
import com.maven.repairshop.service.ReparationService;
import com.maven.repairshop.service.exceptions.NotFoundException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryReparationService implements ReparationService {

    private final Map<Long, Reparation> store = new LinkedHashMap<>();
    private final Map<Long, Client> clients = new LinkedHashMap<>();
    private final AtomicLong seqReparation = new AtomicLong(1);
    private final AtomicLong seqClient = new AtomicLong(1);

    public InMemoryReparationService() {
        seed();
    }

    // ---------------- Contract methods ----------------

    @Override
    public Reparation creerReparation(Long clientId, Long reparateurId) {
        Client c = clients.get(clientId);
        if (c == null) {
            // UI-only : si le client n'existe pas dans le mock, on le crée vite fait
            c = new Client();
            setId(c, clientId != null ? clientId : seqClient.getAndIncrement());
            c.setNom("Client " + c.getId());

            Reparateur r = new Reparateur();
            setId(r, reparateurId != null ? reparateurId : 1L);
            r.setNom("Réparateur " + r.getId());
            r.setLogin("rep" + r.getId());
            r.setPassword("mock");
            c.setReparateur(r);

            clients.put(c.getId(), c);
        }

        Reparation rep = new Reparation();
        Long id = seqReparation.getAndIncrement();
        setId(rep, id);

        rep.setClient(c);
        rep.setCodeUnique(codeFromId(id));
        rep.setStatut(StatutReparation.ENREGISTREE);
        rep.setDateDernierStatut(LocalDateTime.now());

        store.put(id, rep);
        return rep;
    }

    @Override
    public void changerStatut(Long reparationId, StatutReparation nouveauStatut) {
        Reparation rep = store.get(reparationId);
        if (rep == null) throw new NotFoundException("Réparation introuvable: id=" + reparationId);

        if (nouveauStatut == null) return;

        // UI mock : on applique le changement (les règles métier finales seront côté backend)
        rep.setStatut(nouveauStatut);
        rep.setDateDernierStatut(LocalDateTime.now());
    }

    @Override
    public Reparation trouverParId(Long reparationId) {
        Reparation rep = store.get(reparationId);
        if (rep == null) throw new NotFoundException("Réparation introuvable: id=" + reparationId);
        return rep;
    }

    @Override
    public List<Reparation> rechercher(String query, Long reparateurId, StatutReparation filtreStatut) {
        String q = query == null ? "" : query.trim().toLowerCase();

        return store.values().stream()
                // filtre reparateur (via client.reparateur)
                .filter(r -> {
                    if (reparateurId == null) return true;
                    Client c = r.getClient();
                    if (c == null || c.getReparateur() == null) return false;
                    return Objects.equals(c.getReparateur().getId(), reparateurId);
                })
                // filtre statut
                .filter(r -> filtreStatut == null || r.getStatut() == filtreStatut)
                // recherche query (codeUnique ou nom client)
                .filter(r -> {
                    if (q.isEmpty()) return true;
                    String code = safe(r.getCodeUnique()).toLowerCase();
                    String nomClient = (r.getClient() != null) ? safe(r.getClient().getNom()).toLowerCase() : "";
                    return code.contains(q) || nomClient.contains(q);
                })
                // tri récent -> ancien
                .sorted(Comparator.comparing(Reparation::getDateCreation).reversed())
                .collect(Collectors.toList());
    }

    // ---------------- Seed data ----------------

    private void seed() {
        Reparateur rep1 = new Reparateur();
        setId(rep1, 1L);
        rep1.setNom("Réparateur 1");
        rep1.setLogin("rep1");
        rep1.setPassword("mock");

        Client c1 = mkClient("Amine K.", rep1);
        Client c2 = mkClient("Sara B.", rep1);
        Client c3 = mkClient("Nadia L.", rep1);

        mkReparation(c1, StatutReparation.EN_COURS, -3);
        mkReparation(c1, StatutReparation.EN_ATTENTE_PIECES, -2);
        mkReparation(c2, StatutReparation.ENREGISTREE, -1);
        mkReparation(c2, StatutReparation.TERMINEE, -4);
        mkReparation(c3, StatutReparation.ANNULEE, -6);
        mkReparation(c3, StatutReparation.LIVREE, -8);
    }

    private Client mkClient(String nom, Reparateur reparateur) {
        Client c = new Client();
        Long id = seqClient.getAndIncrement();
        setId(c, id);
        c.setNom(nom);
        c.setTelephone("06 00 00 00 0" + id);
        c.setVille("Casablanca");
        c.setReparateur(reparateur);
        clients.put(id, c);
        return c;
    }

    private Reparation mkReparation(Client client, StatutReparation statut, int daysOffset) {
        Reparation r = new Reparation();
        Long id = seqReparation.getAndIncrement();
        setId(r, id);
        r.setClient(client);
        r.setCodeUnique(codeFromId(id));
        r.setStatut(statut);
        r.setDateDernierStatut(LocalDateTime.now().plusDays(daysOffset));
        store.put(id, r);
        return r;
    }

    // ---------------- Helpers ----------------

    private static String codeFromId(Long id) {
        return "REP-" + String.format("%06d", id);
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
            throw new RuntimeException("Impossible de setter l'id (mock InMemory)", e);
        }
    }
}