package fr.formation.mdc.tp02;

import fr.formation.data.Weather;
import fr.formation.data.WeatherApiDataSource;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

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
 * Point de depart : le logger et les logs sont en commentaire, car le projet ne compile pas
 * sans logback-classic, ajoute au TP 1. Il faut les decommenter (imports, logger et appels).
 */
public class TraitementCommandes {

    //private static final Logger log = LoggerFactory.getLogger(TraitementCommandes.class);


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
        //log.info("Début du lot : {} commandes", VILLES.size());

        // Pool fixe : les 4 threads sont reutilises d'une commande a l'autre
        try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
            for (int i = 0; i < VILLES.size(); i++) {
                String commande = "CMD-" + (i + 1);
                String ville = VILLES.get(i);
                executor.submit(() -> traitement.traiter(commande, ville));
            }
        }   // attend la fin de toutes les commandes

        //log.info("Fin du lot en {} ms", System.currentTimeMillis() - debut);
    }

    /**
     * Traite une commande. Les messages ne disent pas de quelle commande il s'agit :
     * c'est tout l'enjeu du TP.
     *
     * @param commande identifiant de la commande, par exemple "CMD-7"
     * @param ville    ville dont on veut la meteo
     */
    public void traiter(String commande, String ville) {
        //log.info("Début du traitement");
        try {
            pause();
            //log.debug("Appel de l'API météo");
            Weather meteo = source.loadWeather(ville);
            //log.debug("Météo reçue : {}", meteo);

            pause();
            if (meteo.wind() > VENT_FORT) {
                //log.warn("Vent fort, livraison retardée");
            }
            //log.info("Commande terminée");
        } catch (RuntimeException e) {
            //log.error("Échec de la commande", e);
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
