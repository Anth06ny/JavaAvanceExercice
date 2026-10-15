package fr.formation.modules.catastrophe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.spi.ToolProvider;
import java.util.stream.Stream;

/**
 * CATASTROPHE : LE JAR QUI CHANGE EN PRODUCTION
 * <p>
 * Une bibliotheque "meteo-utils" existe en 2 versions. L'application est compilee avec la 2.0.
 * Ce main compile tout (javac, jar) puis lance l'application dans un processus separe,
 * pour choisir exactement le classpath, comme on le ferait en production.
 * <p>
 * Fichiers generes dans build/catastrophe-classpath (supprimes a chaque lancement).
 */
public class CatastropheClasspath {

    private static final Path DOSSIER = Path.of("build", "catastrophe-classpath").toAbsolutePath();

    // =========================================================================
    // Le code de la bibliotheque et de l'application
    // =========================================================================

    /** meteo-utils v1.0 : une seule methode formater. */
    private static final String TEMPERATURE_V1 = """
            package fr.meteoutils;

            public class Temperature {
                public static String formater(double celsius) {
                    return celsius + " C";
                }
            }
            """;

    /** meteo-utils v2.0 : ajoute une methode formater(celsius, fahrenheit). */
    private static final String TEMPERATURE_V2 = """
            package fr.meteoutils;

            public class Temperature {
                public static String formater(double celsius) {
                    return celsius + " C";
                }

                public static String formater(double celsius, boolean fahrenheit) {
                    return fahrenheit ? (celsius * 9 / 5 + 32) + " F" : formater(celsius);
                }
            }
            """;

    /** Presente dans les 2 versions. Publique, mais pas faite pour etre utilisee hors de la bibliotheque. */
    private static final String CACHE_INTERNE = """
            package fr.meteoutils.internal;

            /** INTERNE : ne pas utiliser, peut changer ou disparaitre a tout moment. */
            public class CacheInterne {
                public static String contenu() {
                    return "cache interne de meteo-utils";
                }
            }
            """;

    /** L'application, ecrite et compilee avec meteo-utils 2.0. */
    private static final String APP = """
            package fr.app;

            import fr.meteoutils.Temperature;
            import fr.meteoutils.internal.CacheInterne;

            public class Main {
                public static void main(String[] args) {
                    System.out.println("Temperature chargee depuis : "
                            + Temperature.class.getProtectionDomain().getCodeSource().getLocation());
                    System.out.println("Classe interne utilisee    : " + CacheInterne.contenu());
                    System.out.println("Niort                      : " + Temperature.formater(19.4, true));
                }
            }
            """;

    public static void main(String[] args) throws Exception {
        final int SCENE = 1;

        //Compile et génère le jarr des 2 bibliothèques et le jar de l'application
        preparer();

        switch (SCENE) {

            // SCENE 1 : on lance l'APP sans lui donner la bibliotheque dont elle dépend
            case 1 -> lancer("SCENE 1 : bibliothèque oubliée");

            // SCENE 2 : sur le poste du developpeur. On lance l'APP avec en dépendance la v2.jar
            case 2 -> lancer("SCENE 2 : poste du développeur", "meteo-utils-2.0.jar");

            // SCENE 3 : on ajoute la v2.jar sans retirer l'ancienne v1.jar
            case 3 -> lancer("SCENE 3 : ajout de la 2.0 sans retirer la 1.0", "meteo-utils-2.0.jar", "meteo-utils-1.0.jar");

            // SCENE 4 : En production, les memes jars, mais l'ordre s'est inverse sans le faire exprès
            case 4 -> lancer("SCENE 4 : production, ordre des jars inversé", "meteo-utils-1.0.jar", "meteo-utils-2.0.jar");

            default -> System.out.println("Choisissez une valeur entre 1 et 4.");
        }
    }

    // =========================================================================
    // Utilitaires : rien a modifier en dessous
    // =========================================================================

    /** Compile les 2 versions de la bibliotheque en jar, puis l'application avec la 2.0. */
    private static void preparer() throws IOException {
        supprimer(DOSSIER);

        compiler("meteo-utils-1.0", null, TEMPERATURE_V1, CACHE_INTERNE);
        creerJar("meteo-utils-1.0");

        compiler("meteo-utils-2.0", null, TEMPERATURE_V2, CACHE_INTERNE);
        creerJar("meteo-utils-2.0");

        compiler("app", DOSSIER.resolve("meteo-utils-2.0.jar").toString(), APP);
    }

    /** Lance l'application dans un nouveau processus Java, avec app + les jars donnes dans le classpath. */
    private static void lancer(String titre, String... jars) throws IOException, InterruptedException {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println(titre);
        System.out.println("=".repeat(70));

        List<String> classpath = new ArrayList<>();
        classpath.add(DOSSIER.resolve("app").toString());
        for (String jar : jars) {
            classpath.add(DOSSIER.resolve(jar).toString());
        }

        // La commande lancee, avec des chemins courts (les vrais sont dans build/catastrophe-classpath)
        List<String> classpathCourt = new ArrayList<>(List.of("app"));
        classpathCourt.addAll(List.of(jars));
        System.out.println("Commande : java -cp " + String.join(File.pathSeparator, classpathCourt) + " fr.app.Main");
        System.out.println("-".repeat(70));

        String java = ProcessHandle.current().info().command().orElse("java");
        Process process = new ProcessBuilder(java, "-cp", String.join(File.pathSeparator, classpath), "fr.app.Main")
                .inheritIO()
                .start();
        int code = process.waitFor();
        // Petite attente pour que la sortie d'erreur du processus s'affiche avant la ligne suivante
        Thread.sleep(200);
        System.out.println("-".repeat(70));
        System.out.println("Code de retour : " + code + (code == 0 ? " (succès)" : " (échec)"));
    }

    private static void compiler(String nom, String classpath, String... sources) throws IOException {
        Path dossierSources = DOSSIER.resolve("src").resolve(nom);
        List<String> arguments = new ArrayList<>(List.of("-d", DOSSIER.resolve(nom).toString()));
        if (classpath != null) {
            arguments.addAll(List.of("-cp", classpath));
        }
        for (String source : sources) {
            arguments.add(ecrireSource(dossierSources, source).toString());
        }
        int code = outil("javac")
                .run(System.out, System.err, arguments.toArray(String[]::new));
        if (code != 0) {
            throw new IllegalStateException("Compilation impossible : " + nom);
        }
    }

    private static void creerJar(String nom) {
        int code = outil("jar").run(System.out, System.err,
                "--create", "--file", DOSSIER.resolve(nom + ".jar").toString(),
                "-C", DOSSIER.resolve(nom).toString(), ".");
        if (code != 0) {
            throw new IllegalStateException("Création du jar impossible : " + nom);
        }
    }

    /** javac et jar du JDK en cours d'execution : ni JAVA_HOME ni PATH ne sont utilises. */
    private static ToolProvider outil(String nom) {
        return ToolProvider.findFirst(nom).orElseThrow(() -> new IllegalStateException(
                nom + " introuvable : lancer ce main avec un JDK, pas un JRE (" + System.getProperty("java.home") + ")"));
    }

    /** Ecrit la source dans package/Classe.java, en lisant le package et la classe dans le code. */
    private static Path ecrireSource(Path dossier, String source) throws IOException {
        String paquet = source.lines().filter(l -> l.startsWith("package ")).findFirst().orElseThrow()
                .replace("package ", "").replace(";", "").strip();
        String classe = source.lines().filter(l -> l.startsWith("public class ")).findFirst().orElseThrow()
                .replace("public class ", "").replace("{", "").strip();
        Path fichier = dossier.resolve(paquet.replace('.', '/')).resolve(classe + ".java");
        Files.createDirectories(fichier.getParent());
        return Files.writeString(fichier, source);
    }

    private static void supprimer(Path dossier) throws IOException {
        if (!Files.exists(dossier)) {
            return;
        }
        try (Stream<Path> fichiers = Files.walk(dossier)) {
            for (Path fichier : fichiers.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(fichier);
            }
        }
    }
}
