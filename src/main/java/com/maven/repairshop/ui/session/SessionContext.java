package com.maven.repairshop.ui.session;

import com.maven.repairshop.model.Proprietaire;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.model.Utilisateur;

public final class SessionContext {

    private Utilisateur currentUser;

    public Utilisateur getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Utilisateur currentUser) {
        this.currentUser = currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public boolean isProprietaire() {
        return currentUser instanceof Proprietaire;
    }

    public boolean isReparateur() {
        return currentUser instanceof Reparateur;
    }

    public void clear() {
        this.currentUser = null;
    }
}