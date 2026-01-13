package com.maven.repairshop.ui.controllers;

import java.awt.Component;
import java.util.List;
import java.util.function.Consumer;

import com.maven.repairshop.model.Reparation;
import com.maven.repairshop.service.ReparationService;
import com.maven.repairshop.service.exceptions.NotFoundException;

/**
 * UI-only : Suivi par codeUnique.
 * On se base sur le contrat ReparationService (rechercher).
 */
public class SuiviController {

    private final ReparationService service;

    public SuiviController(ReparationService service) {
        this.service = service;
    }

    /**
     * Cherche une réparation par codeUnique (case-insensitive).
     * @param codeUnique ex: REP-000123
     */
    public void suivre(Component parent,
                       String codeUnique,
                       Long reparateurId,
                       Consumer<Reparation> onSuccess) {

        UiAsync.run(parent,
                () -> findByCodeUnique(codeUnique, reparateurId),
                onSuccess);
    }

    private Reparation findByCodeUnique(String codeUnique, Long reparateurId) {
        String code = (codeUnique == null) ? "" : codeUnique.trim();
        if (code.isEmpty()) {
            throw new IllegalArgumentException("Veuillez saisir un code de suivi.");
        }

        // Contrat: rechercher(query, reparateurId, statut)
        // Statut null => "Tous"
        List<Reparation> list = service.rechercher(code, reparateurId, null);

        // Match strict sur codeUnique (meilleure précision)
        for (Reparation r : list) {
            if (r != null && r.getCodeUnique() != null && r.getCodeUnique().equalsIgnoreCase(code)) {
                return r;
            }
        }

        throw new NotFoundException("Aucune réparation trouvée pour le code: " + code);
    }
}