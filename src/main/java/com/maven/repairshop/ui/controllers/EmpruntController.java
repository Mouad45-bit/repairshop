package com.maven.repairshop.ui.controllers;

import java.awt.Component;
import java.util.List;

import com.maven.repairshop.model.Emprunt;
import com.maven.repairshop.model.enums.TypeEmprunt;
import com.maven.repairshop.service.EmpruntService;

public class EmpruntController {

    private final EmpruntService service;

    public EmpruntController(EmpruntService service) {
        this.service = service;
    }

    public void lister(Component parent, Long reparateurId, java.util.function.Consumer<List<Emprunt>> onSuccess) {
        UiAsync.run(parent, () -> service.lister(reparateurId), onSuccess);
    }

    public void creer(
            Component parent,
            Long reparateurId,
            TypeEmprunt type,
            String personne,
            String montantStr,
            String motif,
            java.util.function.Consumer<Emprunt> onSuccess
    ) {
        UiAsync.run(parent, () -> {
            double montant = parseMontant(montantStr);
            return service.creer(reparateurId, type, personne, montant, motif);
        }, onSuccess);
    }

    public void changerStatut(Component parent, Long empruntId, String nouveauStatut, Runnable onSuccess) {
        UiAsync.runVoid(parent, () -> service.changerStatut(empruntId, nouveauStatut), onSuccess);
    }

    public void supprimer(Component parent, Long empruntId, Runnable onSuccess) {
        UiAsync.runVoid(parent, () -> service.supprimer(empruntId), onSuccess);
    }

    private double parseMontant(String montantStr) {
        if (montantStr == null) throw new IllegalArgumentException("Montant obligatoire.");
        String s = montantStr.trim().replace(",", ".");
        if (s.isEmpty()) throw new IllegalArgumentException("Montant obligatoire.");
        try {
            return Double.parseDouble(s);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Montant invalide.");
        }
    }
}