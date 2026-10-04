package fr.formation.aop;

import fr.formation.data.Weather;

/**
 * TP 3 (Journalisation et supervision) - INTERCEPTER SANS TOUCHER AU CODE
 * <p>
 * Le service metier, vu comme une interface : c'est indispensable pour pouvoir
 * le remplacer par un Proxy du JDK, qui ne sait implementer que des interfaces.
 */
public interface MeteoService {

    /**
     * Meteo d'une ville.
     *
     * @param ville nom de la ville, par exemple "Niort"
     * @return la ville, la temperature et le vent
     * @throws RuntimeException si la ville est inconnue
     */
    Weather meteo(String ville);

    /**
     * Compare la temperature de deux villes.
     *
     * @return une phrase du type "Il fait plus chaud a Niort (19,4 C) qu'a Nantes (17,9 C)"
     */
    String meteoDeuxVilles(String ville1, String ville2);
}
