import model.*;
import service.*;
import exception.MetierException;
import java.util.List;

public class MainApp {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   DEMARRAGE DE L'APPLICATION GESTION SAV");
        System.out.println("==========================================\n");

        try {
            // --- 1. INITIALISATION DES SERVICES ---
            // On prépare tous nos outils de travail
            AuthService authService = new AuthService();
            ProprietaireService proprietaireService = new ProprietaireService();
            ReparationService reparationService = new ReparationService();
            FinanceService financeService = new FinanceService();
            EmpruntService empruntService = new EmpruntService();

            // --- 2. CONFIGURATION INITIALE (Le Patron) ---
            System.out.println("--- ETAPE 1 : Configuration (Propriétaire) ---");
            
            // On crée un patron fictif pour démarrer
            Utilisateur patron = new Utilisateur(1L, "admin", "admin123", Role.PROPRIETAIRE, null);
            
            // Le patron crée la boutique "Repar'Phone Rabat"
            proprietaireService.creerBoutique(patron, "Repar'Phone Rabat");
            
            // Le patron embauche un technicien (ID boutique 1 simulé)
            // Note : login = "tech1", mdp = "pass"
            proprietaireService.creerReparateur(patron, "tech1", "pass", 1L);

            // --- 3. CONNEXION DU TECHNICIEN ---
            System.out.println("\n--- ETAPE 2 : Connexion ---");
            Utilisateur userConnecte = authService.login("tech1", "pass");

            // --- 4. ACCUEIL CLIENT & REPARATION ---
            System.out.println("\n--- ETAPE 3 : Nouvelle Réparation ---");
            Client client = new Client("M. Alami", "0612345678", "CODE-999", userConnecte);
            
            // Le technicien crée le dossier
            reparationService.creerDossier(userConnecte, client, "Ecran iPhone 13 cassé", 1200.0);
            
            // On récupère le dossier pour travailler dessus
            List<Reparation> mesDossiers = reparationService.getReparationsDeMaBoutique(userConnecte);
            Reparation dossierEnCours = mesDossiers.get(0); 
            System.out.println("Dossier sélectionné : " + dossierEnCours.getDescription());

            // --- 5. GESTION (Avancement et Paiement) ---
            System.out.println("\n--- ETAPE 4 : Travail et Paiement ---");
            
            // Changement de statut -> EN COURS
            reparationService.changerStatut(userConnecte, dossierEnCours.getId(), Statut.EN_COURS);
            
            // Le client paie une avance de 500 DH
            financeService.encaisser(dossierEnCours, 500.0, "AVANCE");

            // --- 6. EMPRUNT (Prêt de matériel) ---
            System.out.println("\n--- ETAPE 5 : Prêt de matériel ---");
            empruntService.preterMateriel(userConnecte, "Samsung A10 (Prêt)", client.getNom());

            // --- 7. BILAN FINANCIER ---
            System.out.println("\n--- ETAPE 6 : Bilan ---");
            double chiffreAffaires = financeService.getChiffreAffaires(userConnecte);
            System.out.println("Chiffre d'affaires de la boutique : " + chiffreAffaires + " DH");

            System.out.println("\n=== TEST REUSSI : TOUT FONCTIONNE CORRECTEMENT ===");

        } catch (MetierException e) {
            System.err.println("\n!!! ERREUR METIER !!!");
            System.err.println("Le gendarme a bloqué l'action : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}