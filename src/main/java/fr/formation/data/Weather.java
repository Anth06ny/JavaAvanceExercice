package fr.formation.data;

/**
 * Resultat d'un appel meteo : juste ce dont l'IHM a besoin.
 *
 * @param city        nom de la ville tel que renvoye par l'API
 * @param temperature temperature en degres Celsius
 * @param wind        vitesse du vent en m/s
 */
public record Weather(String city, double temperature, double wind) {

    @Override
    public String toString() {
        return "%s : %.1f°C, vent %.1f m/s".formatted(city, temperature, wind);
    }
}
