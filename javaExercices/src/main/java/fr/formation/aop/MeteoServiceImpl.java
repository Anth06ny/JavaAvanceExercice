package fr.formation.aop;

import fr.formation.data.Weather;
import fr.formation.data.WeatherApiDataSource;

/**
 * TP 3 (Journalisation et supervision) - INTERCEPTER SANS TOUCHER AU CODE
 * <p>
 * L'implementation reelle du service. Elle ne contient que du metier :
 * aucune journalisation, aucune mesure de temps. Tout cela sera ajoute par le proxy,
 * sans modifier cette classe, c'est tout l'interet de la programmation par aspect.
 * <p>
 * ATTENTION : meteoDeuxVilles() appelle meteo(), donc this.meteo().
 * Ce detail est volontaire, il est au coeur de la question 3 du TP.
 */
public class MeteoServiceImpl implements MeteoService {

    // Donnees fictives : 1 seconde par appel, sans reseau et sans cle d'API
    private final WeatherApiDataSource source = new WeatherApiDataSource(true);

    @Override
    public Weather meteo(String ville) {
        return source.loadWeather(ville);
    }

    @Override
    public String meteoDeuxVilles(String ville1, String ville2) {
        // Appel interne : meteo() est appelee sur this, l'objet reel
        Weather meteo1 = meteo(ville1);
        Weather meteo2 = meteo(ville2);

        Weather plusChaude = meteo1.temperature() >= meteo2.temperature() ? meteo1 : meteo2;
        Weather plusFroide = plusChaude == meteo1 ? meteo2 : meteo1;

        return "Il fait plus chaud à %s (%.1f°C) qu'à %s (%.1f°C)".formatted(
                plusChaude.city(), plusChaude.temperature(),
                plusFroide.city(), plusFroide.temperature());
    }
}
