package com.maven.repairshop.service;

import com.maven.repairshop.model.Boutique;
import com.maven.repairshop.model.Reparateur;

public interface ProprietaireService {
    Boutique creerBoutique(Long proprietaireId, String nom, String adresse, String telephone);
    Reparateur creerReparateur(Long boutiqueId, String nom, String login, String rawPassword);
}