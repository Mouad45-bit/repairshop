package com.maven.repairshop.service.dto;

import com.maven.repairshop.model.enums.StatutReparation;

import java.time.LocalDateTime;

public class SuiviDTO {
    private final StatutReparation statut;
    private final LocalDateTime dateDernierStatut;
    private final double resteAPayer;

    public SuiviDTO(StatutReparation statut, LocalDateTime dateDernierStatut, double resteAPayer) {
        this.statut = statut;
        this.dateDernierStatut = dateDernierStatut;
        this.resteAPayer = resteAPayer;
    }

    public StatutReparation getStatut() { return statut; }
    public LocalDateTime getDateDernierStatut() { return dateDernierStatut; }
    public double getResteAPayer() { return resteAPayer; }
}