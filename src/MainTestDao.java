import dao.*;   // Importe tous les DAOs
import model.*; // Importe tous les Modèles

public class MainTestDao {
    public static void main(String[] args) {
        System.out.println("=== TEST DES DAO (Stockage) ===\n");

        // 1. On prépare nos "Managers" (les DAOs)
        BoutiqueDao boutiqueDao = new BoutiqueDao();
        UtilisateurDao utilisateurDao = new UtilisateurDao();
        ClientDao clientDao = new ClientDao();

        // 2. On crée et sauvegarde une BOUTIQUE
        Boutique b1 = new Boutique(10L, "Repar'PC Casablanca");
        boutiqueDao.save(b1);
        System.out.println("[OK] Boutique sauvegardée.");

        // 3. On crée et sauvegarde un REPARATEUR
        // Note : On met ID à null car le DAO va générer l'ID automatiquement (voir code UtilisateurDao)
        Utilisateur u1 = new Utilisateur(null, "hassan", "1234", Role.REPARATEUR, b1);
        utilisateurDao.save(u1); 
        System.out.println("[OK] Réparateur hassan sauvegardé.");

        // 4. On crée et sauvegarde un CLIENT
        // Le réparateur 'hassan' (u1) crée le client
        Client c1 = new Client("M. Tazi", "0600000000", "CODE-TAZI", u1);
        clientDao.save(c1);
        System.out.println("[OK] Client Tazi sauvegardé.");

        // --- VERIFICATION (Est-ce que ça marche ?) ---
        System.out.println("\n--- TENTATIVE DE RECUPERATION ---");

        // Test 1 : Retrouver le réparateur par son LOGIN
        Utilisateur userTrouve = utilisateurDao.findByLogin("hassan");
        if (userTrouve != null) {
            System.out.println("1. Réparateur trouvé : " + userTrouve.getLogin());
            System.out.println("   -> ID généré par le DAO : " + userTrouve.getId()); // Doit être 1
        } else {
            System.out.println("ERREUR : Réparateur introuvable !");
        }

        // Test 2 : Le client vérifie son dossier avec son CODE UNIQUE
        Client clientTrouve = clientDao.findByCodeUnique("CODE-TAZI");
        if (clientTrouve != null) {
            System.out.println("2. Client trouvé : " + clientTrouve.getNom());
            
            // Test Ultime : Remonter jusqu'à la boutique
            String nomBoutique = clientTrouve.getCreateur().getBoutique().getNom();
            System.out.println("3. Vérification lien Boutique : " + nomBoutique);
            
            if (nomBoutique.equals("Repar'PC Casablanca")) {
                System.out.println("\nSUCCESS : Tout est connecté parfaitement !");
            }
        } else {
            System.out.println("ERREUR : Client introuvable !");
        }
    }
}