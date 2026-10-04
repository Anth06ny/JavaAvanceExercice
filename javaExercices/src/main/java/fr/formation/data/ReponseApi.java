package fr.formation.data;

/**
 * Reponse JSON de l'API OpenWeatherMap, limitee aux champs utiles.
 * <p>
 * Gson remplit ce record par reflexion : les noms des composants doivent correspondre
 * aux noms des champs JSON.
 */
public record ReponseApi(String name, Main main, Wind wind) {

    /** Bloc "main" du JSON : la temperature en °C. */
    public record Main(double temp) {
    }

    /** Bloc "wind" du JSON : la vitesse du vent en m/s. */
    public record Wind(double speed) {
    }
}
