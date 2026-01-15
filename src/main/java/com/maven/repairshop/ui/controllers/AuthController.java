package com.maven.repairshop.ui.controllers;

import com.maven.repairshop.model.Utilisateur;
import com.maven.repairshop.service.AuthService;
import com.maven.repairshop.ui.util.ServiceRegistry;

public final class AuthController {

    private final AuthService authService = ServiceRegistry.get().auth();

    public Utilisateur login(String login, String password) {
        return authService.login(login, password);
    }
}