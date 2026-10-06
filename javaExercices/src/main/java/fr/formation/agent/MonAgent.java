package fr.formation.agent;

import java.lang.instrument.Instrumentation;

/**
 * TP 3 (Journalisation et supervision) - INTERCEPTER SANS TOUCHER AU CODE
 */
public class MonAgent {

    /**
     * Appelee par la JVM avant le main de l'application.
     *
     * @param arguments       ce qui suit le = dans -javaagent:mon.jar=arguments, sinon null
     * @param instrumentation le point d'entree de l'API Instrument
     */
    public static void premain(String arguments, Instrumentation instrumentation) {
        System.out.println("[agent] démarré, arguments = " + arguments);

        //TODO 1 : afficher le nombre de classes deja chargees


        //TODO 2 : un ClassFileTransformer qui affiche chaque classe chargee

        //TODO 3 : ByteBuddy, mesurer le temps des methodes de MeteoServiceImpl
    }
}
