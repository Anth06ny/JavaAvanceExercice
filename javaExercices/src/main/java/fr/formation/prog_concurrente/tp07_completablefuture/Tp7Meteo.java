package fr.formation.prog_concurrente.tp07_completablefuture;

import fr.formation.data.WeatherApiDataSource;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;

/**
 * TP 7 - Lance chaque etape et affiche le temps ecoule.
 * Donnees fictives par defaut : passer false au constructeur de WeatherApiDataSource pour la vraie API.
 * Une etape pas encore faite affiche "a faire".
 */
public class Tp7Meteo {

    private static final List<String> VILLES = List.of("Niort", "Nantes", "Brest", "Lyon", "Toulouse");

    public static void main(String[] args) {

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            MeteoAsync meteo = new MeteoAsync(executor);

            etape("Sequentiel", () -> meteo.appelSequentiel(VILLES));
            etape("Etape 1 : parallele", () -> meteo.appelParallele(VILLES));
            etape("Etape 2 : meteo en texte", () -> meteo.meteoEnTexte("Niort").join());
            etape("Etape 3 : comparer", () -> meteo.comparer("Niort", "Marseille").join());
            etape("Etape 4 : tableau de bord tolerant", () -> meteo.tableauDeBordTolerant(List.of("Niort", "VilleQuiNExistePas", "Brest")));
            etape("Etape 5 : delai de 500 ms", () -> meteo.meteoAvecDelai("Niort", Duration.ofMillis(500)).join());
            etape("Etape 6 : meteo ou repli", () -> meteo.meteoOuRepli("Brest", "Lyon").join());
            etape("Pour les plus rapides : threads virtuels", () -> meteo.appelParalleleThreadsVirtuels(VILLES));
        }
    }

    private static void etape(String nom, Callable<Object> action) {
        System.out.println("=== " + nom + " ===");
        long debut = System.currentTimeMillis();
        try {
            Object resultat = action.call();
            System.out.println(resultat);
            System.out.println("(" + (System.currentTimeMillis() - debut) + " ms)");
        } catch (UnsupportedOperationException e) {
            System.out.println("a faire");
        } catch (Exception e) {
            System.out.println("Erreur : " + e);
        }
        System.out.println();
    }
}
