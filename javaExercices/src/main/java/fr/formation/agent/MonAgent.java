package fr.formation.agent;

import java.lang.instrument.Instrumentation;

/**
 * TP 3 (Journalisation et supervision) - INTERCEPTER SANS TOUCHER AU CODE
 * <p>
 * Un javaagent : du code charge par la JVM AVANT le main de l'application.
 * <p>
 * A FAIRE (etapes 5 a 7 du TP) :
 * <ol>
 *     <li>Afficher le nombre de classes deja chargees au demarrage
 *         (instrumentation.getAllLoadedClasses())</li>
 *     <li>Declarer l'attribut Premain-Class dans le manifeste du jar (build.gradle.kts)</li>
 *     <li>Lancer Tp3Proxy avec l'option
 *         -javaagent:build/libs/JavaExercice-1.0-SNAPSHOT.jar</li>
 *     <li>Ajouter un ClassFileTransformer qui affiche le nom et la taille des classes
 *         du package fr/formation, et qui renvoie null (bytecode inchange)</li>
 * </ol>
 * ATTENTION : ClassFileTransformer n'est pas une interface fonctionnelle
 * (ses deux methodes transform() sont des methodes par defaut depuis Java 9),
 * une lambda ne compile donc pas : il faut une classe anonyme ou une classe nommee.
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

        //A FAIRE : nombre de classes deja chargees

        //A FAIRE : instrumentation.addTransformer(...)
    }
}
