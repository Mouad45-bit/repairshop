package com.maven.repairshop.service;

import com.maven.repairshop.service.dto.SuiviDTO;

public interface SuiviService {
    SuiviDTO suivreParCode(String codeUnique);
}