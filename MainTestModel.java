import model.*; 
import java.time.LocalDate;

public class MainTestModel {
    public static void main(String[] args) {
        System.out.println("=== TEST FINAL DU MODELE ===\n");

        // 1. Création de la base (Boutique + Réparateur)
        Boutique boutique = new Boutique(1L, "Repar'Express");
        Utilisateur reparateur = new Utilisateur(10L, "karim", "1234", Role.REPARATEUR, boutique);

        // 2. Un Client arrive
        Client client = new Client("Mme Alami", "0611223344", "CODE-999", reparateur);

        // 3. On crée une Réparation
        Reparation rep = new Reparation(100L, "Ecran iPhone X", 1500.0, client);
        System.out.println("1. Nouvelle réparation : " + rep);

        // 4. Le client paie une AVANCE
        Paiement avance = new Paiement(1L, 500.0, "AVANCE", rep);
        System.out.println("2. Paiement reçu : " + avance);

        // 5. Le client a besoin d'un téléphone de prêt (Emprunt)
        Emprunt emprunt = new Emprunt(50L, "Samsung S7 (Prêt)", client.getNom(), reparateur);
        System.out.println("3. Emprunt créé : " + emprunt);

        // --- VERIFICATIONS DE SECURITE (Règles du prof) ---
        System.out.println("\n--- VERIFICATIONS AUTOMATIQUES ---");

        // Règle 1 : Retrouver la boutique depuis le paiement
        // Paiement -> Reparation -> Client -> Reparateur -> Boutique
        String boutiquePaiement = avance.getReparation().getClient().getCreateur().getBoutique().getNom();
        System.out.println("Boutique du paiement : " + boutiquePaiement);

        // Règle 2 : Retrouver la boutique depuis l'emprunt
        // Emprunt -> Reparateur -> Boutique
        String boutiqueEmprunt = emprunt.getReparateur().getBoutique().getNom();
        System.out.println("Boutique de l'emprunt : " + boutiqueEmprunt);
        
        // Règle 3 : Gestion du retour emprunt
        emprunt.marquerCommeRendu();
        System.out.println("Etat emprunt après retour : " + emprunt);

        System.out.println("\n=== TOUT EST CORRECT ===");
    }
}