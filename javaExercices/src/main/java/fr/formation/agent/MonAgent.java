package fr.formation.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

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
        System.out.println("[agent] start, arguments = " + arguments);

        //TODO 1 : afficher le nombre de classes deja chargees

        //TODO 2 : un ClassFileTransformer qui affiche chaque classe chargee

        //TODO 3 : ByteBuddy, mesurer le temps des methodes de MeteoServiceImpl


    }
}
