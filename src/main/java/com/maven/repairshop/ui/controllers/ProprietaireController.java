package com.maven.repairshop.ui.controllers;

import com.maven.repairshop.dao.BoutiqueDao;
import com.maven.repairshop.dao.UtilisateurDao;
import com.maven.repairshop.model.Boutique;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.service.ProprietaireService;
import com.maven.repairshop.service.impl.ProprietaireServiceImpl;

import java.util.List;

public final class ProprietaireController {

    private final ProprietaireService proprietaireService = new ProprietaireServiceImpl();
    private final BoutiqueDao boutiqueDao = new BoutiqueDao();
    private final UtilisateurDao utilisateurDao = new UtilisateurDao();

    public Boutique getBoutiqueByProprietaire(Long proprietaireId) {
        return boutiqueDao.findByProprietaire(proprietaireId);
    }

    public Boutique creerBoutique(Long proprietaireId, String nom, String adresse, String telephone) {
        return proprietaireService.creerBoutique(proprietaireId, nom, adresse, telephone);
    }

    public List<Reparateur> listerReparateurs(Long boutiqueId) {
        return utilisateurDao.findReparateursByBoutique(boutiqueId);
    }

    public Reparateur creerReparateur(Long boutiqueId, String nom, String login, String rawPassword, Long userId) {
        return proprietaireService.creerReparateur(boutiqueId, nom, login, rawPassword, userId);
    }
}