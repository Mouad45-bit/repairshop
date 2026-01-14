package com.maven.repairshop.service.impl;

import com.maven.repairshop.dao.BoutiqueDao;
import com.maven.repairshop.dao.UtilisateurDao;
import com.maven.repairshop.model.Boutique;
import com.maven.repairshop.model.Proprietaire;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.model.Utilisateur;
import com.maven.repairshop.service.ProprietaireService;
import com.maven.repairshop.service.exceptions.NotFoundException;
import com.maven.repairshop.service.exceptions.ValidationException;
import com.maven.repairshop.util.PasswordUtil;

public class ProprietaireServiceImpl implements ProprietaireService {

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();
    private final BoutiqueDao boutiqueDao = new BoutiqueDao();

    @Override
    public Boutique creerBoutique(Long proprietaireId, String nom, String adresse, String telephone) {
        if (proprietaireId == null) throw new ValidationException("Propriétaire obligatoire.");
        if (nom == null || nom.isBlank()) throw new ValidationException("Nom de boutique obligatoire.");

        Utilisateur u = utilisateurDao.findById(proprietaireId);
        if (u == null) throw new NotFoundException("Propriétaire introuvable: " + proprietaireId);

        if (!(u instanceof Proprietaire)) {
            throw new ValidationException("Seul un propriétaire peut créer une boutique.");
        }

        Boutique b = new Boutique();
        b.setNom(nom);
        b.setAdresse(adresse);
        b.setTelephone(telephone);
        b.setProprietaire((Proprietaire) u);

        boutiqueDao.save(b);
        return b;
    }

    @Override
    public Reparateur creerReparateur(Long boutiqueId, String nom, String login, String rawPassword) {
        if (boutiqueId == null) throw new ValidationException("Boutique obligatoire.");
        if (nom == null || nom.isBlank()) throw new ValidationException("Nom du réparateur obligatoire.");
        if (login == null || login.isBlank()) throw new ValidationException("Login obligatoire.");
        if (rawPassword == null || rawPassword.isBlank()) throw new ValidationException("Mot de passe obligatoire.");

        Boutique b = boutiqueDao.findById(boutiqueId);
        if (b == null) throw new NotFoundException("Boutique introuvable: " + boutiqueId);

        if (utilisateurDao.existsByLogin(login)) {
            throw new ValidationException("Login déjà utilisé: " + login);
        }

        Reparateur r = new Reparateur();
        r.setNom(nom);
        r.setLogin(login);
        r.setPassword(PasswordUtil.hash(rawPassword));
        r.setBoutique(b); // obligatoire

        utilisateurDao.save(r); // Hibernate persist
        return r;
    }
}