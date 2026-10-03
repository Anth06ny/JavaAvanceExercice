package fr.formation.mdc.tp02;

import fr.formation.data.Weather;
import fr.formation.data.WeatherApiDataSource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * TP 2 (Journalisation) - SUIVRE UNE TACHE DANS PLUSIEURS THREADS
 * <p>
 * 18 commandes meteo traitees par un pool de 4 threads.
 * Chaque commande passe par plusieurs etapes, et les lignes des 4 threads s'entrelacent.
 * Une ville sur trois n'existe pas : sa commande echoue.
 * <p>
 * Point de depart : tous les affichages sont des System.out.println, a remplacer par des logs.
 */
public class TraitementCommandes {


    /** Une ville sur trois ne fait pas partie des donnees fictives : sa commande echoue. */
    private static final List<String> VILLES = List.of(
            "Niort", "Nantes", "Atlantide",
            "Lyon", "Brest", "Eldorado",
            "Toulouse", "Marseille", "Gotham",
            "Lille", "Le Havre", "Poudlard",
            "Bordeaux", "Rennes", "Utopia",
            "Strasbourg", "Perpignan", "Arkham");

    /** Au-dela de cette vitesse (m/s), la livraison est retardee. */
    private static final double VENT_FORT = 10;

    // Donnees fictives : 1 seconde par appel, sans reseau
    private final WeatherApiDataSource source = new WeatherApiDataSource(true);

    public static void main(String[] args) {
        TraitementCommandes traitement = new TraitementCommandes();
        long debut = System.currentTimeMillis();
        System.out.println("Début du lot : " + VILLES.size() + " commandes");

        // Pool fixe : les 4 threads sont reutilises d'une commande a l'autre
        try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
            for (int i = 0; i < VILLES.size(); i++) {
                String commande = "CMD-" + (i + 1);
                String ville = VILLES.get(i);
                executor.submit(() -> traitement.traiter(commande, ville));
            }
        }   // attend la fin de toutes les commandes

        System.out.println("Fin du lot en " + (System.currentTimeMillis() - debut) + " ms");
    }

    /**
     * Traite une commande. Les messages ne disent pas de quelle commande il s'agit :
     * c'est tout l'enjeu du TP.
     *
     * @param commande identifiant de la commande, par exemple "CMD-7"
     * @param ville    ville dont on veut la meteo
     */
    public void traiter(String commande, String ville) {
        System.out.println("Début du traitement");
        try {
            pause();
            System.out.println("Appel de l'API météo");
            Weather meteo = source.loadWeather(ville);
            System.out.println("Météo reçue : " + meteo);

            pause();
            if (meteo.wind() > VENT_FORT) {
                System.out.println("Vent fort, livraison retardée");
            }
            System.out.println("Commande terminée");
        } catch (RuntimeException e) {
            System.out.println("Échec de la commande : " + e.getMessage());
        }
    }

    /** Pause aleatoire, pour que les lignes des threads s'entrelacent. */
    private static void pause() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(50, 300));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
