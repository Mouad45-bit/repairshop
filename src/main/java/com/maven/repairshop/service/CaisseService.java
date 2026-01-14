package com.maven.repairshop.service;

import java.time.LocalDateTime;

public interface CaisseService {

    double caisseReparateur(Long reparateurId, LocalDateTime from, LocalDateTime to, Long userId);

    double caisseBoutique(Long boutiqueId, LocalDateTime from, LocalDateTime to, Long userId);
}
