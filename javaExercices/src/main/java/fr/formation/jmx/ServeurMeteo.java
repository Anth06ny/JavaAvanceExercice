package fr.formation.jmx;

import fr.formation.data.Weather;
import fr.formation.data.WeatherApiDataSource;

import java.io.File;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Un petit serveur meteo : 4 clients demandent en boucle le bulletin d'une ville au hasard.
 */
public class ServeurMeteo {

    private static final List<String> VILLES = List.of(
            "Niort", "Nantes", "Lyon", "Brest",
            "Toulouse", "Marseille", "Lille", "Le Havre",
            "Bordeaux", "Rennes", "Strasbourg", "Perpignan");

    private static final int NB_CLIENTS = 4;

    /** Nombre de rafales simulees par quart d'heure pour calculer la temperature ressentie. */
    private static final int NB_RAFALES = 50_000;

    // Donnees fictives : 1 seconde par appel, sans reseau et sans cle d'API
    private final WeatherApiDataSource source = new WeatherApiDataSource(true);

    public static void main(String[] args) {
        System.out.println("ServeurMeteo en marche, pid = " + ProcessHandle.current().pid());
        System.out.println("Outils du JDK : " + System.getProperty("java.home") + File.separator + "bin");

        ServeurMeteo serveur = new ServeurMeteo();

        //TODO 1 : enregistrer le MBean de statistiques

        for (int i = 1; i <= NB_CLIENTS; i++) {
            new Thread(serveur::simulerClient, "client-" + i).start();
        }
    }

    /** Un client : demande sans fin le bulletin d'une ville au hasard. */
    private void simulerClient() {
        while (true) {
            String ville = VILLES.get(ThreadLocalRandom.current().nextInt(VILLES.size()));
            try {
                demander(ville);
            } catch (RuntimeException e) {
                // Le client n'affiche rien : comment savoir qu'une demande a echoue ?
            }
            pause(100);
        }
    }

    /**
     * Traite la demande d'un client.
     *
     * @param ville ville dont on veut le bulletin
     * @return le bulletin, par exemple "Niort : 19,4°C, vent 4,7 m/s, ressenti 18,1°C"
     */
    public String demander(String ville) {
        //TODO 2 : mesurer la duree de la demande
        Weather meteo = source.loadWeather(ville);
        return rediger(meteo);
    }

    /** Met en forme le bulletin d'une ville. */
    private String rediger(Weather meteo) {
        return "%s, ressenti %.1f°C".formatted(meteo, ressenti(meteo));
    }

    /**
     * Temperature ressentie. Le vent n'est jamais constant : pour chaque quart d'heure de la journee,
     * on simule des rafales autour de la vitesse moyenne, puis on fait la moyenne.
     */
    private double ressenti(Weather meteo) {
        double somme = 0;
        for (int heure = 0; heure < 24; heure++) {
            double[] mesures = new double[NB_RAFALES];
            for (int quart = 0; quart < 4; quart++) {
                for (int i = 0; i < NB_RAFALES; i++) {
                    double rafale = meteo.wind() * 3.6 * (1 + Math.sin(heure + quart + i) / 4);   // en km/h
                    mesures[i] = refroidissementEolien(meteo.temperature(), rafale);
                }
                for (double mesure : mesures) {
                    somme += mesure;
                }
            }
        }
        return somme / (24 * 4 * NB_RAFALES);
    }

    /** Formule officielle du refroidissement eolien (temperature en °C, vent en km/h). */
    private static double refroidissementEolien(double temperature, double vent) {
        double v = Math.pow(vent, 0.16);
        return 13.12 + 0.6215 * temperature - 11.37 * v + 0.3965 * temperature * v;
    }

    private static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
