package com.maven.repairshop.service;

import java.time.LocalDateTime;
import java.util.Map;

public interface StatistiquesService {

    long nbReparationsParPeriode(Long boutiqueId, LocalDateTime from, LocalDateTime to, Long userId);

    Map<String, Long> termineesVsEnCours(Long boutiqueId, LocalDateTime from, LocalDateTime to, Long userId);

    double chiffreAffairesParPeriode(Long boutiqueId, LocalDateTime from, LocalDateTime to, Long userId);
}