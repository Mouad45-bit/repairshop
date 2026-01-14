package com.maven.repairshop.service;

import com.maven.repairshop.model.Paiement;
import com.maven.repairshop.model.enums.TypePaiement;

import java.util.List;

public interface PaiementService {

    Paiement enregistrerPaiement(Long reparationId, double montant, TypePaiement type, Long userId);

    double resteAPayer(Long reparationId, Long userId);

    List<Paiement> listerPaiements(Long reparationId, Long userId);
}
