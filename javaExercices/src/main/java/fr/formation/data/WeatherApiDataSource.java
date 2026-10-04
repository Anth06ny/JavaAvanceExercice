package fr.formation.data;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class WeatherApiDataSource {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final Gson gson = new Gson();
    private final boolean fakeData;


    public WeatherApiDataSource() {
        this(false);
    }

    /**
     * @param fakeData true : donnees fictives (environ 50 villes de France), sans appel reseau
     *                 et sans consommer la cle d'API ; false : vrai appel a l'API OpenWeatherMap
     *                 Les 2 ont un Thread.sleep pour ralentir l'appel
     */
    public WeatherApiDataSource(boolean fakeData) {
        this.fakeData = fakeData;
    }

    // Petit test manuel
    public static void main(String[] args) {
        System.out.println(new WeatherApiDataSource(false).loadWeather("Toulouse"));
    }


    /**
     * Interroge l'API et retourne la meteo courante d'une ville.
     *
     * @param city nom de la ville, par exemple "Toulouse"
     * @return la ville, la temperature (°C) et le vent (m/s)
     * @throws IllegalArgumentException si la ville est vide
     * @throws RuntimeException         si l'appel echoue ou si la ville est inconnue
     */
    public Weather loadWeather(String city) {
        if (fakeData) {
            return loadWeatherFake(city);
        }

        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("Le nom de la ville est obligatoire");
        }

        String url = "%s?q=%s&appid=%s&units=metric&lang=fr".formatted(
                "https://api.openweathermap.org/data/2.5/weather",
                URLEncoder.encode(city, StandardCharsets.UTF_8),
                "b80967f0a6bd10d23e44848547b26550");


        var uri  =URI.create(url);
        System.out.println(uri);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            Thread.sleep(1000);
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (Exception e) {
            throw new RuntimeException("Appel meteo impossible pour " + city, e);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("Ville inconnue ou erreur API (code %d) pour %s"
                    .formatted(response.statusCode(), city));
        }

        return parse(response.body());
    }

    /** Duree simulee d'un appel en mode fakeData (millisecondes). */
    public static final long FAKE_DELAY_MS = 1000;

    /**
     * Donnees fictives pour ne pas consommer la cle d'API pendant les exercices :
     * les 50 plus grandes villes de France metropolitaine, plus Niort.
     * Quelques villes ont un vent fort (plus de 10 m/s).
     */
    private static final Map<String, Weather> FAKE_WEATHERS = Stream.of(
                    new Weather("Paris", 18.5, 3.2),
                    new Weather("Marseille", 24.1, 8.4),
                    new Weather("Lyon", 21.3, 2.8),
                    new Weather("Toulouse", 23.0, 4.1),
                    new Weather("Nice", 23.8, 3.5),
                    new Weather("Nantes", 17.9, 6.7),
                    new Weather("Montpellier", 24.6, 5.9),
                    new Weather("Strasbourg", 16.2, 2.1),
                    new Weather("Bordeaux", 21.7, 4.6),
                    new Weather("Lille", 15.4, 7.3),
                    new Weather("Rennes", 16.8, 5.2),
                    new Weather("Toulon", 24.9, 9.1),
                    new Weather("Reims", 15.9, 4.4),
                    new Weather("Saint-Étienne", 17.2, 3.0),
                    new Weather("Le Havre", 14.6, 11.2),
                    new Weather("Villeurbanne", 21.1, 2.9),
                    new Weather("Dijon", 17.5, 3.3),
                    new Weather("Angers", 18.3, 4.8),
                    new Weather("Grenoble", 19.8, 1.9),
                    new Weather("Nîmes", 25.2, 6.1),
                    new Weather("Aix-en-Provence", 24.3, 5.4),
                    new Weather("Clermont-Ferrand", 18.1, 2.6),
                    new Weather("Le Mans", 17.6, 4.2),
                    new Weather("Brest", 14.2, 12.5),
                    new Weather("Tours", 18.4, 3.9),
                    new Weather("Amiens", 15.1, 6.3),
                    new Weather("Annecy", 17.0, 1.7),
                    new Weather("Limoges", 16.9, 3.4),
                    new Weather("Metz", 15.7, 3.1),
                    new Weather("Boulogne-Billancourt", 18.6, 3.0),
                    new Weather("Perpignan", 25.7, 11.5),
                    new Weather("Besançon", 16.4, 2.7),
                    new Weather("Orléans", 17.8, 4.0),
                    new Weather("Saint-Denis", 18.4, 3.3),
                    new Weather("Rouen", 15.8, 5.6),
                    new Weather("Argenteuil", 18.2, 3.4),
                    new Weather("Montreuil", 18.7, 2.9),
                    new Weather("Caen", 15.3, 7.8),
                    new Weather("Mulhouse", 16.6, 2.4),
                    new Weather("Nancy", 15.5, 3.6),
                    new Weather("Roubaix", 15.2, 7.1),
                    new Weather("Tourcoing", 15.0, 7.4),
                    new Weather("Nanterre", 18.3, 3.1),
                    new Weather("Vitry-sur-Seine", 18.9, 2.8),
                    new Weather("Créteil", 18.6, 3.0),
                    new Weather("Avignon", 25.0, 9.8),
                    new Weather("Poitiers", 18.0, 4.3),
                    new Weather("Aubervilliers", 18.5, 3.2),
                    new Weather("Dunkerque", 14.8, 10.8),
                    new Weather("Versailles", 17.9, 3.5),
                    new Weather("Niort", 19.4, 4.7))
            .collect(Collectors.toMap(meteo -> normaliser(meteo.city()), meteo -> meteo));

    /**
     * Meme comportement que {@link #loadWeather(String)}, mais avec des donnees fictives :
     * attend 1 seconde, sans appel reseau, pour ne pas consommer la cle d'API.
     * La recherche ignore les majuscules et les accents ("saint-etienne" trouve "Saint-Étienne").
     *
     * @param city nom de la ville, par exemple "Toulouse"
     * @return la ville, la temperature (°C) et le vent (m/s)
     * @throws IllegalArgumentException si la ville est vide
     * @throws RuntimeException         si la ville ne fait pas partie des donnees fictives
     */
    private Weather loadWeatherFake(String city) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("Le nom de la ville est obligatoire");
        }

        try {
            Thread.sleep(FAKE_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Appel meteo interrompu pour " + city, e);
        }

        Weather meteo = FAKE_WEATHERS.get(normaliser(city));
        if (meteo == null) {
            throw new RuntimeException("Ville inconnue (donnees fictives) pour " + city);
        }
        return meteo;
    }

    /**
     * Donnee fictive d'une ville, renvoyee immediatement (sans attente) : utile pour les tests.
     *
     * @return la meteo fictive, ou null si la ville ne fait pas partie des donnees fictives
     */
    public static Weather getFakeWeather(String city) {
        return city == null ? null : FAKE_WEATHERS.get(normaliser(city));
    }

    /** Minuscules, sans accents et sans espaces autour : "  Saint-Étienne " devient "saint-etienne". */
    private static String normaliser(String ville) {
        return Normalizer.normalize(ville.strip(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    /**
     * Extrait les 3 informations utiles du JSON renvoye par l'API.
     * <p>
     * Gson construit le record ReponseApi par reflexion, a partir des noms des champs JSON.
     */
    private Weather parse(String json) {
        ReponseApi reponse = gson.fromJson(json, ReponseApi.class);

        return new Weather(reponse.name(), reponse.main().temp(), reponse.wind().speed());
    }


}
