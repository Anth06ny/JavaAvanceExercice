package fr.formation.prog_concurrente.tp07_completablefuture;

import fr.formation.data.Weather;
import fr.formation.data.WeatherApiDataSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class MeteoAsync {

    /** Au-dela de cette vitesse (m/s), le vent est considere comme fort. */
    public static final double VENT_FORT = 10;

    private final WeatherApiDataSource source = new WeatherApiDataSource(true);
    private final ExecutorService executor;

    /**
     * @param executor executor sur lequel lancer les taches asynchrones
     */
    public MeteoAsync(ExecutorService executor) {
        this.executor = executor;
    }


    // =========================================================================
    // ETAPE 1 : tableau de bord
    // =========================================================================

    /**
     * La meteo de chaque ville, une ville apres l'autre.
     * Avec 5 villes, il faut attendre 5 appels soit 5s.
     */
    public List<Weather> appelSequentiel(List<String> villes) {
        List<Weather> meteos = new ArrayList<>();
        for (String ville : villes) {
            meteos.add(source.loadWeather(ville));
        }
        return meteos;
    }

    /**
     * Meme resultat que appelSequentiel, le résultat doit être dans le meme ordre,
     * mais tous les appels sont lances en meme temps.
     */
    public List<Weather> appelParallele(List<String> villes) {

        throw new UnsupportedOperationException("TODO étape 1");
    }

    // =========================================================================
    // ETAPE 2 : transformer le resultat
    // =========================================================================

    /**
     * Lance l'appel et renvoie tout de suite un CompletableFuture
     * qui contiendra le texte formate, par exemple "Niort : 18°C, vent faible".
     */
    public CompletableFuture<String> meteoEnTexte(String ville) {
        // TODO etape 2 : supplyAsync, puis thenApply avec formater
        throw new UnsupportedOperationException("TODO étape 2");
    }

    // =========================================================================
    // ETAPE 3 : comparer deux villes
    // =========================================================================

    /**
     * Charge les deux villes en meme temps et renvoie :
     * - "Il fait plus chaud à Nantes qu'à Niort" (la ville la plus chaude en premier) ;
     * - "Il fait aussi chaud à Nantes qu'à Toulouse" si les temperatures sont egales.
     */
    public CompletableFuture<String> comparer(String ville1, String ville2) {
        // TODO etape 3 : deux supplyAsync, puis thenCombine
        throw new UnsupportedOperationException("TODO étape 3");
    }

    // =========================================================================
    // ETAPE 4 : une ville inconnue ne doit pas tout faire echouer
    // =========================================================================

    /**
     * Comme appelParallele, mais renvoie des textes formates.
     * Si l'appel d'une ville echoue, son texte devient "Inconnue : météo indisponible",
     * et les autres villes s'affichent normalement.
     */
    public List<String> tableauDeBordTolerant(List<String> villes) {
        // TODO etape 4 : thenApply avec formater, puis exceptionally
        throw new UnsupportedOperationException("TODO étape 4");
    }

    // =========================================================================
    // ETAPE 5 : trop lent
    // =========================================================================

    /**
     * Comme meteoEnTexte, mais si le resultat n'est pas arrive au bout du delai,
     * renvoie "Niort : météo indisponible (délai dépassé)".
     */
    public CompletableFuture<String> meteoAvecDelai(String ville, Duration delai) {
        // TODO etape 5 : essayer orTimeout, puis utiliser completeOnTimeout
        throw new UnsupportedOperationException("TODO étape 5");
    }

    // =========================================================================
    // ETAPE 6 : enchainer deux appels
    // =========================================================================

    /**
     * Charge la meteo de la ville. Si le vent depasse VENT_FORT, charge et renvoie
     * la meteo de la ville de repli. Sinon, renvoie la meteo de la ville
     * sans appeler la ville de repli.
     */
    public CompletableFuture<Weather> meteoOuRepli(String ville, String villeDeRepli) {
        // TODO etape 6 : supplyAsync, puis thenCompose (completedFuture si le vent est faible)
        throw new UnsupportedOperationException("TODO étape 6");
    }

    // =========================================================================
    // POUR LES PLUS RAPIDES : sans CompletableFuture
    // =========================================================================

    /**
     * Meme resultat que appelParallele, avec des threads virtuels
     * (Executors.newVirtualThreadPerTaskExecutor), des Future et du code sequentiel.
     */
    public List<Weather> appelParalleleThreadsVirtuels(List<String> villes) {
        // TODO pour les plus rapides
        throw new UnsupportedOperationException("TODO pour les plus rapides");
    }

    // =========================================================================
    //  Utilitaires
    // =========================================================================
    /**
     * Met en forme une meteo : "Niort : 18°C, vent faible".
     * Deja ecrite : a utiliser dans les etapes suivantes.
     */
    public static String formater(Weather meteo) {
        return meteo.city() + " : " + Math.round(meteo.temperature()) + "°C, vent " + forceDuVent(meteo.wind());
    }

    private static String forceDuVent(double vent) {
        if (vent < 5) {
            return "faible";
        }
        if (vent <= VENT_FORT) {
            return "modéré";
        }
        return "fort";
    }
}
