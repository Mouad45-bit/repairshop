package com.maven.repairshop.service.impl;

import com.maven.repairshop.dao.EmpruntDao;
import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.Emprunt;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.model.enums.StatutEmprunt;
import com.maven.repairshop.model.enums.TypeEmprunt;
import com.maven.repairshop.service.EmpruntService;
import com.maven.repairshop.service.exceptions.NotFoundException;
import com.maven.repairshop.service.exceptions.ValidationException;
import com.maven.repairshop.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class EmpruntServiceImpl implements EmpruntService {

    private final EmpruntDao empruntDao = new EmpruntDao();

    @Override
    public Emprunt creer(Long reparateurId, TypeEmprunt type, String personne, double montant, String motif) {

        // 1) Validation
        if (reparateurId == null) throw new ValidationException("Réparateur obligatoire.");
        if (type == null) throw new ValidationException("Type emprunt/prêt obligatoire.");
        if (personne == null || personne.isBlank()) throw new ValidationException("Nom de la personne obligatoire.");
        if (montant <= 0) throw new ValidationException("Le montant doit être > 0.");

        // 2) Charger le réparateur (en session)
        Reparateur rep = HibernateTx.callInTx(session -> session.get(Reparateur.class, reparateurId));
        if (rep == null) throw new NotFoundException("Réparateur introuvable: " + reparateurId);

        // 3) Construire l’objet
        Emprunt e = new Emprunt();
        e.setType(type);
        e.setStatut(StatutEmprunt.EN_COURS);
        e.setNomPersonne(personne.trim());
        e.setMontant(montant);
        e.setMotif(motif == null ? "" : motif.trim());
        e.setReparateur(rep);

        // 4) Persist
        empruntDao.save(e);
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

        HibernateTx.runInTx(session -> {
            Emprunt e = session.get(Emprunt.class, empruntId);
            if (e == null) throw new NotFoundException("Emprunt introuvable: " + empruntId);

            // Règle simple : si REMBOURSE, on ne revient pas en arrière
            if (e.getStatut() == StatutEmprunt.REMBOURSE && st != StatutEmprunt.REMBOURSE) {
                throw new ValidationException("Un emprunt remboursé ne peut pas redevenir 'EN_COURS'.");
            }

            e.setStatut(st);
            session.merge(e);
        });
    }

    @Override
    public void supprimer(Long empruntId) {
        if (empruntId == null) throw new ValidationException("Id emprunt obligatoire.");

        HibernateTx.runInTx(session -> {
            Emprunt e = session.get(Emprunt.class, empruntId);
            if (e == null) throw new NotFoundException("Emprunt introuvable: " + empruntId);

            // Règle possible : interdire suppression si remboursé (à toi de décider)
            // if (e.getStatut() == StatutEmprunt.REMBOURSE) throw new ValidationException("Suppression interdite : déjà remboursé.");

            session.remove(e);
        });
    }

    @Override
    public List<Emprunt> lister(Long reparateurId) {
        if (reparateurId == null) throw new ValidationException("Réparateur obligatoire.");
        return empruntDao.findByReparateur(reparateurId);
    }
}