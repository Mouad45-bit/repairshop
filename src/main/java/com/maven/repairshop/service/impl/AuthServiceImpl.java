package com.maven.repairshop.service.impl;

import com.maven.repairshop.dao.UtilisateurDao;
import com.maven.repairshop.model.Utilisateur;
import com.maven.repairshop.service.AuthService;
import com.maven.repairshop.service.exceptions.NotFoundException;
import com.maven.repairshop.service.exceptions.ValidationException;
import com.maven.repairshop.util.PasswordUtil;

public class AuthServiceImpl implements AuthService {

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();

    @Override
    public Utilisateur login(String login, String password) {
        if (login == null || login.isBlank()) throw new ValidationException("Login obligatoire.");
        if (password == null || password.isBlank()) throw new ValidationException("Mot de passe obligatoire.");

        Utilisateur user = utilisateurDao.findByLogin(login);
        if (user == null) throw new NotFoundException("Utilisateur introuvable.");

        if (!PasswordUtil.verify(password, user.getPassword())) {
            throw new ValidationException("Identifiants invalides.");
        }
        return user;
    }
}