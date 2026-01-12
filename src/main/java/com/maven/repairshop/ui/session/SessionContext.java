package com.maven.repairshop.ui.session;

import com.maven.repairshop.model.Boutique;
import com.maven.repairshop.model.Proprietaire;
import com.maven.repairshop.model.Reparateur;
import com.maven.repairshop.model.Utilisateur;

/**
 * Contexte de session côté UI (Swing).
 * - Ne contient pas de logique Hibernate.
 * - Sert à appliquer les règles d'accès (rôle + boutique).
 */
public final class SessionContext {

    public enum Role {
        PROPRIETAIRE,
        REPARATEUR
    }

    private final Long userId;
    private final String login;
    private final Role role;

    private final Long boutiqueId;

    // Selon le rôle, un seul des deux est non-null
    private final Long proprietaireId;
    private final Long reparateurId;

    private SessionContext(Long userId, String login, Role role,
                           Long boutiqueId, Long proprietaireId, Long reparateurId) {
        this.userId = userId;
        this.login = login;
        this.role = role;
        this.boutiqueId = boutiqueId;
        this.proprietaireId = proprietaireId;
        this.reparateurId = reparateurId;
    }

    /** Crée le contexte session à partir d'un Utilisateur (retourné par AuthService). */
    public static SessionContext fromUser(Utilisateur u) {
        if (u == null) throw new IllegalArgumentException("Utilisateur null");

        Boutique b = u.getBoutique();
        Long boutiqueId = (b != null ? b.getId() : null);

        if (u instanceof Proprietaire) {
            Proprietaire p = (Proprietaire) u;
            return new SessionContext(
                    p.getId(),
                    p.getLogin(),
                    Role.PROPRIETAIRE,
                    boutiqueId,
                    p.getId(),
                    null
            );
        }

        if (u instanceof Reparateur) {
            Reparateur r = (Reparateur) u;
            return new SessionContext(
                    r.getId(),
                    r.getLogin(),
                    Role.REPARATEUR,
                    boutiqueId,
                    null,
                    r.getId()
            );
        }

        // Si plus tard tu ajoutes d'autres sous-types, tu verras l'erreur directement.
        throw new IllegalStateException("Type Utilisateur non supporté : " + u.getClass().getName());
    }

    public Long getUserId() { return userId; }
    public String getLogin() { return login; }
    public Role getRole() { return role; }

    public Long getBoutiqueId() { return boutiqueId; }

    public Long getProprietaireId() { return proprietaireId; }
    public Long getReparateurId() { return reparateurId; }

    public boolean isProprietaire() { return role == Role.PROPRIETAIRE; }
    public boolean isReparateur() { return role == Role.REPARATEUR; }

    @Override
    public String toString() {
        return "SessionContext{userId=" + userId +
                ", login='" + login + '\'' +
                ", role=" + role +
                ", boutiqueId=" + boutiqueId +
                ", proprietaireId=" + proprietaireId +
                ", reparateurId=" + reparateurId +
                '}';
    }
}