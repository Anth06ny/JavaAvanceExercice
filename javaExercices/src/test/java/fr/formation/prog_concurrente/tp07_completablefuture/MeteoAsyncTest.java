package fr.formation.prog_concurrente.tp07_completablefuture;

import fr.formation.data.Weather;
import fr.formation.data.WeatherApiDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TP 7 - Tests de MeteoAsync.
 * <p>
 * Les tests utilisent WeatherApiDataSource en mode fakeData :
 * - les resultats attendus viennent des donnees fictives (getFakeWeather) ;
 * - chaque appel dure FAKE_DELAY_MS, ce qui permet de verifier que les appels sont paralleles.
 */
class MeteoAsyncTest {

    private static final long DUREE_APPEL = WeatherApiDataSource.FAKE_DELAY_MS;

    private static final List<String> CINQ_VILLES = List.of("Niort", "Nantes", "Brest", "Lyon", "Toulouse");

    private ExecutorService executor;
    private MeteoAsync meteo;

    @BeforeEach
    void preparer() {
        executor = Executors.newVirtualThreadPerTaskExecutor();
        meteo = new MeteoAsync(executor);
    }

    @AfterEach
    void fermer() {
        executor.close();
    }

    /** Meteo fictive attendue pour une ville. */
    private static Weather fake(String ville) {
        Weather meteo = WeatherApiDataSource.getFakeWeather(ville);
        assertNotNull(meteo, "Ville absente des donnees fictives : " + ville);
        return meteo;
    }

    @Test
    void etape1_appelParallele_memeResultatQueSequentiel_etPlusRapide() {
        long debut = System.currentTimeMillis();
        List<Weather> resultat = meteo.appelParallele(CINQ_VILLES);
        long duree = System.currentTimeMillis() - debut;

        assertEquals(CINQ_VILLES.stream().map(MeteoAsyncTest::fake).toList(), resultat, "Resultats ou ordre incorrects");
        assertTrue(duree < 2 * DUREE_APPEL,
                "5 appels de " + DUREE_APPEL + " ms en " + duree + " ms : ils ne sont pas lances en meme temps");
    }

    @Test
    void etape2_meteoEnTexte_renvoieToutDeSuiteUnFuturPuisLeTexte() {
        long debut = System.currentTimeMillis();
        CompletableFuture<String> futur = meteo.meteoEnTexte("Niort");
        long duree = System.currentTimeMillis() - debut;

        assertTrue(duree < DUREE_APPEL / 2, "La methode doit rendre la main sans attendre l'appel (" + duree + " ms)");
        assertEquals(fake("Niort").toString(), futur.join());
    }

    @Test
    void etape3_comparer_laVilleLaPlusChaudeEnPremier() {
        Weather nantes = fake("Nantes");
        Weather niort = fake("Niort");
        assertNotEquals(nantes.temperature(), niort.temperature(), "Les donnees fictives doivent avoir des temperatures differentes");
        Weather plusChaude = nantes.temperature() > niort.temperature() ? nantes : niort;
        Weather plusFroide = plusChaude == nantes ? niort : nantes;
        String attendu = "Il fait plus chaud à " + plusChaude.city() + " qu'à " + plusFroide.city();

        long debut = System.currentTimeMillis();
        assertEquals(attendu, meteo.comparer("Nantes", "Niort").join());
        long duree = System.currentTimeMillis() - debut;

        assertEquals(attendu, meteo.comparer("Niort", "Nantes").join(), "L'ordre des parametres ne doit pas changer le texte");
        assertTrue(duree < 1.5 * DUREE_APPEL,
                "2 appels de " + DUREE_APPEL + " ms en " + duree + " ms : ils ne sont pas lances en meme temps");
    }

    @Test
    void etape3_comparer_temperaturesEgales() {
        assertEquals(fake("Paris").temperature(), fake("Aubervilliers").temperature(),
                "Les donnees fictives doivent avoir la meme temperature pour Paris et Aubervilliers");

        assertEquals("Il fait aussi chaud à Paris qu'à Aubervilliers", meteo.comparer("Paris", "Aubervilliers").join());
    }

    @Test
    void etape4_tableauDeBordTolerant_uneVilleInconnueNeBloquePasLesAutres() {
        assertNull(WeatherApiDataSource.getFakeWeather("Inconnue"), "La ville Inconnue ne doit pas exister");

        List<String> resultat = meteo.tableauDeBordTolerant(List.of("Niort", "Inconnue", "Brest"));

        assertEquals(List.of(
                fake("Niort").toString(),
                "Inconnue : météo indisponible",
                fake("Brest").toString()), resultat);
    }

    @Test
    void etape5_meteoAvecDelai_delaiDepasse() {
        long debut = System.currentTimeMillis();
        String resultat = meteo.meteoAvecDelai("Niort", Duration.ofMillis(DUREE_APPEL / 10)).join();
        long duree = System.currentTimeMillis() - debut;

        assertEquals("Niort : météo indisponible (délai dépassé)", resultat);
        assertTrue(duree < DUREE_APPEL / 2, "Le delai de " + DUREE_APPEL / 10 + " ms n'a pas ete respecte (" + duree + " ms)");
    }

    @Test
    void etape5_meteoAvecDelai_delaiRespecte() {
        assertEquals(fake("Niort").toString(),
                meteo.meteoAvecDelai("Niort", Duration.ofMillis(3 * DUREE_APPEL)).join());
    }

    @Test
    void etape6_meteoOuRepli_ventFort_renvoieLaVilleDeRepli() {
        assertTrue(fake("Brest").wind() > MeteoAsync.VENT_FORT, "Brest doit avoir un vent fort dans les donnees fictives");

        assertEquals(fake("Lyon"), meteo.meteoOuRepli("Brest", "Lyon").join());
    }

    @Test
    void etape6_meteoOuRepli_ventFaible_nAppellePasLaVilleDeRepli() {
        assertTrue(fake("Niort").wind() <= MeteoAsync.VENT_FORT, "Niort doit avoir un vent faible dans les donnees fictives");

        // Un seul appel attendu, donc environ DUREE_APPEL (deux appels a la suite : 2 x DUREE_APPEL)
        long debut = System.currentTimeMillis();
        Weather resultat = meteo.meteoOuRepli("Niort", "Lyon").join();
        long duree = System.currentTimeMillis() - debut;

        assertEquals(fake("Niort"), resultat);
        assertTrue(duree < 1.5 * DUREE_APPEL,
                "Duree de " + duree + " ms : la ville de repli ne doit pas etre appelee si le vent est faible");
    }

    @Test
    void pourLesPlusRapides_tableauDeBordThreadsVirtuels() {
        long debut = System.currentTimeMillis();
        List<Weather> resultat = meteo.appelParalleleThreadsVirtuels(CINQ_VILLES);
        long duree = System.currentTimeMillis() - debut;

        assertEquals(CINQ_VILLES.stream().map(MeteoAsyncTest::fake).toList(), resultat, "Resultats ou ordre incorrects");
        assertTrue(duree < 2 * DUREE_APPEL,
                "5 appels de " + DUREE_APPEL + " ms en " + duree + " ms : ils ne sont pas lances en meme temps");
    }
}
