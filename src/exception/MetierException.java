package exception;

// C'est notre propre erreur personnalisée.
// On l'utilisera pour dire : "Erreur métier : Solde insuffisant", "Mot de passe faux", etc.
public class MetierException extends Exception {
    public MetierException(String message) {
        super(message);
    }
}