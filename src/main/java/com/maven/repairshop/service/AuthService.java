package com.maven.repairshop.service;

import com.maven.repairshop.model.Utilisateur;

public interface AuthService {
    Utilisateur login(String login, String password);
}