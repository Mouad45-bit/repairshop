package model;

public enum Statut {
    EN_ATTENTE,   // Quand le client dépose l'appareil
    EN_COURS,     // Le réparateur travaille dessus
    TERMINEE,     // C'est fini, prêt à être rendu
    LIVREE        // Le client a payé et récupéré l'appareil
}