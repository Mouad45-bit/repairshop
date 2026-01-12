package com.maven.repairshop.ui.mocks;

import com.maven.repairshop.model.Emprunt;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.model.base.BaseEntity;
import com.maven.repairshop.model.enums.StatutEmprunt;
import com.maven.repairshop.model.enums.TypeEmprunt;
import com.maven.repairshop.service.EmpruntService;
import com.maven.repairshop.service.exceptions.NotFoundException;
import com.maven.repairshop.service.exceptions.ValidationException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryEmpruntService implements EmpruntService {

    private final Map<Long, Emprunt> store = new LinkedHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    public InMemoryEmpruntService() {
        seed();
    }

    @Override
    public Emprunt creer(Long reparateurId, TypeEmprunt type, String personne, double montant, String motif) {
        if (reparateurId == null) throw new ValidationException("Réparateur obligatoire.");
        if (type == null) throw new ValidationException("Type obligatoire.");
        if (personne == null || personne.isBlank()) throw new ValidationException("Nom personne obligatoire.");
        if (montant <= 0) throw new ValidationException("Montant doit être > 0.");

        Emprunt e = new Emprunt();
        Long id = seq.getAndIncrement();
        setId(e, id);

        e.setType(type);
        e.setStatut(StatutEmprunt.EN_COURS);
        e.setNomPersonne(personne.trim());
        e.setMontant(montant);
        e.setMotif(motif == null ? "" : motif.trim());

        Reparateur rep = new Reparateur();
        setId(rep, reparateurId);
        rep.setNom("Réparateur " + reparateurId);
        rep.setLogin("rep" + reparateurId);
        rep.setPassword("mock");
        e.setReparateur(rep);

        store.put(id, e);
        return e;
    }

    @Override
    public void changerStatut(Long empruntId, String nouveauStatut) {
        if (empruntId == null) throw new ValidationException("Id emprunt obligatoire.");
        if (nouveauStatut == null || nouveauStatut.isBlank())
            throw new ValidationException("Statut obligatoire.");

        StatutEmprunt st;
        try {
            st = StatutEmprunt.valueOf(nouveauStatut.trim());
        } catch (Exception ex) {
            throw new ValidationException("Statut invalide: " + nouveauStatut);
        }

        Emprunt e = store.get(empruntId);
        if (e == null) throw new NotFoundException("Emprunt introuvable: " + empruntId);

        if (e.getStatut() == StatutEmprunt.REMBOURSE && st != StatutEmprunt.REMBOURSE) {
            throw new ValidationException("Un emprunt remboursé ne peut pas redevenir EN_COURS.");
        }

        e.setStatut(st);
    }

    @Override
    public void supprimer(Long empruntId) {
        if (empruntId == null) throw new ValidationException("Id emprunt obligatoire.");
        if (!store.containsKey(empruntId)) throw new NotFoundException("Emprunt introuvable: " + empruntId);
        store.remove(empruntId);
    }

    @Override
    public List<Emprunt> lister(Long reparateurId) {
        if (reparateurId == null) throw new ValidationException("Réparateur obligatoire.");

        return store.values().stream()
                .filter(e -> e.getReparateur() != null && Objects.equals(e.getReparateur().getId(), reparateurId))
                .sorted(Comparator.comparing(Emprunt::getDateEmprunt).reversed())
                .collect(Collectors.toList());
    }

    // -------- Seed --------

    private void seed() {
        // rep 1
        Emprunt a = creer(1L, TypeEmprunt.EMPRUNT, "Amine K.", 300, "Coup de main");
        setDate(a, LocalDateTime.now().minusDays(2));

        Emprunt b = creer(1L, TypeEmprunt.PRET, "Sara B.", 120, "Prêt court");
        setDate(b, LocalDateTime.now().minusDays(1));

        Emprunt c = creer(1L, TypeEmprunt.EMPRUNT, "Nadia L.", 500, "Urgence");
        changerStatut(c.getId(), StatutEmprunt.PARTIELLEMENT_REMBOURSE.name());
        setDate(c, LocalDateTime.now().minusDays(5));

        Emprunt d = creer(1L, TypeEmprunt.PRET, "Yassine", 200, "Prêt ami");
        changerStatut(d.getId(), StatutEmprunt.REMBOURSE.name());
        setDate(d, LocalDateTime.now().minusDays(8));
    }

    // -------- Helpers --------

    private static void setId(Object entity, Long id) {
        try {
            Field f = BaseEntity.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de setter l'id (mock InMemoryEmpruntService)", e);
        }
    }

    private static void setDate(Emprunt e, LocalDateTime dt) {
        try {
            Field f = Emprunt.class.getDeclaredField("dateEmprunt");
            f.setAccessible(true);
            f.set(e, dt);
        } catch (Exception ignored) {
            // pas bloquant (UI-only)
        }
    }
}
