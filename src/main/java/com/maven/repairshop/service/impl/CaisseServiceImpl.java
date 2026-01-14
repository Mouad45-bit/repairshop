package com.maven.repairshop.service.impl;

import com.maven.repairshop.dao.PaiementDao;
import com.maven.repairshop.dao.base.HibernateTx;
import com.maven.repairshop.model.Boutique;
import com.maven.repairshop.model.Proprietaire;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.model.Utilisateur;
import com.maven.repairshop.service.CaisseService;
import com.maven.repairshop.service.exceptions.NotFoundException;
import com.maven.repairshop.service.exceptions.ValidationException;

import java.time.LocalDateTime;

public class CaisseServiceImpl implements CaisseService {

    private final PaiementDao paiementDao = new PaiementDao();

    @Override
    public double caisseReparateur(Long reparateurId, LocalDateTime from, LocalDateTime to, Long userId) {

        if (reparateurId == null) throw new ValidationException("Réparateur obligatoire.");
        if (userId == null) throw new ValidationException("Utilisateur connecté obligatoire.");

        // Check accès (même boutique + rôles)
        HibernateTx.runInTx(session -> {
            Utilisateur u = session.get(Utilisateur.class, userId);
            if (u == null) throw new NotFoundException("Utilisateur introuvable: " + userId);

            Reparateur rep = session.get(Reparateur.class, reparateurId);
            if (rep == null) throw new NotFoundException("Réparateur introuvable: " + reparateurId);

            if (u instanceof Reparateur) {
                if (!u.getId().equals(reparateurId)) {
                    throw new ValidationException("Accès refusé : un réparateur ne peut voir que sa caisse.");
                }
            } else if (u instanceof Proprietaire) {
                Boutique ub = u.getBoutique();
                Boutique rb = rep.getBoutique();
                Long ubid = ub == null ? null : ub.getId();
                Long rbid = rb == null ? null : rb.getId();
                if (ubid == null || rbid == null || !ubid.equals(rbid)) {
                    throw new ValidationException("Accès refusé (boutique différente).");
                }
            } else {
                throw new ValidationException("Accès refusé.");
            }
        });

        return paiementDao.sumByReparateurAndPeriod(reparateurId, from, to);
    }

    @Override
    public double caisseBoutique(Long boutiqueId, LocalDateTime from, LocalDateTime to, Long userId) {

        if (boutiqueId == null) throw new ValidationException("Boutique obligatoire.");
        if (userId == null) throw new ValidationException("Utilisateur connecté obligatoire.");

        HibernateTx.runInTx(session -> {
            Utilisateur u = session.get(Utilisateur.class, userId);
            if (u == null) throw new NotFoundException("Utilisateur introuvable: " + userId);

            if (!(u instanceof Proprietaire)) {
                throw new ValidationException("Accès refusé : réservé au propriétaire.");
            }

            Boutique ub = u.getBoutique();
            Long ubid = ub == null ? null : ub.getId();
            if (ubid == null || !ubid.equals(boutiqueId)) {
                throw new ValidationException("Accès refusé (boutique différente).");
            }
        });

        return paiementDao.sumByBoutiqueAndPeriod(boutiqueId, from, to);
    }
}
