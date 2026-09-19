package fr.formation.reactif.tp02_flow_avance;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * TP 2 Flow avancé - Affichage et pause, deja ecrits.
 */
public class Outils {

    private static final DateTimeFormatter HEURE = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    /** Affiche le message precede de l'heure et du nom du thread. */
    public static void log(String message) {
        System.out.printf("%s [%-33s] %s%n", LocalTime.now().format(HEURE), Thread.currentThread().getName(), message);
    }

    /** Simule un traitement lent. */
    public static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
