package fr.formation.modules.catastrophe;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.spi.ToolProvider;
import java.util.stream.Stream;

/**
 * CATASTROPHE : LA CLASSE QUI N'EST PAS ELLE-MEME
 * <p>
 * 4 scenes autour du chargement des classes. Comme pour la catastrophe du classpath,
 * tout le code des scenes est ecrit en clair ci-dessous, compile a la volee, puis
 * lance : soit dans un processus separe (scenes 1 et 2), soit dans des ClassLoaders
 * fabriques pour l'occasion (scenes 3 et 4).
 * <p>
 * Fichiers generes dans build/catastrophe-classloader (supprimes a chaque lancement).
 */
public class CatastropheClassLoader {

    private static final Path DOSSIER = Path.of("build", "catastrophe-classloader").toAbsolutePath();

    // =========================================================================
    // Le code des scenes
    // =========================================================================

    /** SCENE 1 : notre propre java.lang.String, pose dans le classpath de l'application. */
    private static final String FAUX_STRING = """
            package java.lang;

            public class String {

                static {
                    System.out.println(">>> le FAUX String vient d'etre chargé <<<");
                }

                public int length() {
                    return 42;
                }
            }
            """;

    /** SCENE 1 : l'application, qui ne sait pas qu'un faux String traine dans son classpath. */
    private static final String APP_STRING = """
            package fr.app;

            public class Main {
                public static void main(String[] args) {
                    System.out.println("longueur de abc    : " + "abc".length());
                    System.out.println("String chargee par : " + String.class.getClassLoader());
                }
            }
            """;

    /** SCENE 2 : une classe dans un package du JDK. */
    private static final String CLASSE_INTERDITE = """
            package java.formation;

            public class Bidon {
                public static java.lang.String salut() {
                    return "je suis chargee";
                }
            }
            """;

    /** SCENE 2 : l'application demande explicitement la classe interdite. */
    private static final String APP_INTERDITE = """
            package fr.app;

            public class Main {
                public static void main(String[] args) throws Exception {
                    Class.forName("java.formation.Bidon");
                    System.out.println("classe chargee sans probleme");
                }
            }
            """;

    /** SCENES 3 et 4 : l'interface commune, celle que tout le monde doit partager. */
    private static final String API = """
            package fr.meteo;

            public interface SourceMeteo {
                String nom();
            }
            """;

    /** SCENES 3 et 4 : le code de l'hote, qui recoit un plugin et l'utilise a travers l'interface. */
    private static final String HOTE = """
            package fr.meteo;

            public class Hote {
                public static String utiliser(Object plugin) {
                    SourceMeteo source = (SourceMeteo) plugin;   // le cast qui fait tout
                    return source.nom();
                }
            }
            """;

    /** SCENE 3 : le meme cast, mais vers la classe du plugin : le nom est alors identique des 2 cotes. */
    private static final String PONT = """
            package fr.plugins;

            public class Pont {
                public static String utiliser(Object plugin) {
                    SourceFake source = (SourceFake) plugin;
                    return source.nom();
                }
            }
            """;

    /** SCENES 3 et 4 : le plugin, livre a part, qui implemente l'interface. */
    private static final String PLUGIN = """
            package fr.plugins;

            import fr.meteo.SourceMeteo;

            public class SourceFake implements SourceMeteo {
                public String nom() {
                    return "donnees fictives";
                }
            }
            """;

    public static void main(String[] args) throws Exception {
        final int SCENE = 4;

        preparer();

        switch (SCENE) {

            // SCENE 1 : on pose notre propre java.lang.String dans le classpath de l'application
            // On lance le main de APP_STRING qui utilise java.lang.String pour voir s'il utilise le notre
            case 1 -> lancerProcessus("SCENE 1 : le faux String", "faux-string", "app-string");

            // SCENE 2 : on charge une classe rangee dans un package java.*
            case 2 -> lancerProcessus("SCENE 2 : le package interdit", "interdite", "app-interdite");

            // SCENE 3 : 2 plugins, chacun avec son ClassLoader, qui ne partagent rien
            case 3 -> scene3();

            // SCENE 4 : les memes 2 plugins, mais l'interface commune vient du parent
            case 4 -> scene4();

            default -> System.out.println("Choisissez une valeur entre 1 et 4.");
        }
    }

    // =========================================================================
    // SCENE 3 : la catastrophe
    // =========================================================================

    /**
     * Chaque plugin recoit l'interface ET l'implementation : chacun definit donc
     * sa propre version de SourceMeteo et de Hote. Les deux mondes n'ont rien en commun.
     */
    private static void scene3() throws Exception {
        titre("SCENE 3 : deux plugins qui ne partagent rien");

        URL[] tout = {url("api"), url("plugin")};
        try (URLClassLoader pluginA = new URLClassLoader("plugin-A", tout, ClassLoader.getPlatformClassLoader());
             URLClassLoader pluginB = new URLClassLoader("plugin-B", tout, ClassLoader.getPlatformClassLoader())) {

            Class<?> interfaceA = pluginA.loadClass("fr.meteo.SourceMeteo");
            Class<?> interfaceB = pluginB.loadClass("fr.meteo.SourceMeteo");

            System.out.println("meme nom de classe    : " + interfaceA.getName().equals(interfaceB.getName()));
            System.out.println("meme objet Class      : " + (interfaceA == interfaceB));
            System.out.println("chargee par           : " + interfaceA.getClassLoader().getName()
                    + " et " + interfaceB.getClassLoader().getName());
            System.out.println();

            // Un objet fabrique par le plugin A, donne au code du plugin B
            Object sourceDeA = pluginA.loadClass("fr.plugins.SourceFake").getDeclaredConstructor().newInstance();
            Class<?> hoteDeB = pluginB.loadClass("fr.meteo.Hote");

            System.out.println("Le plugin B utilise un objet du plugin A...");
            afficherResultat(hoteDeB, sourceDeA);

            // Le meme cast, mais vers la classe concrete : le nom est identique des 2 cotes
            System.out.println();
            System.out.println("Et en castant vers la classe du plugin elle-meme...");
            afficherResultat(pluginB.loadClass("fr.plugins.Pont"), sourceDeA);
        }
    }

    // =========================================================================
    // SCENE 4 : le remede
    // =========================================================================

    /**
     * L'interface commune est chargee une seule fois, par un ClassLoader parent.
     * Les 2 plugins n'ont plus que leur implementation, et heritent de la meme interface.
     */
    private static void scene4() throws Exception {
        titre("SCENE 4 : l'interface commune vient du parent");

        URL[] api = {url("api")};
        URL[] plugin = {url("plugin")};

        try (URLClassLoader hote = new URLClassLoader("hote", api, ClassLoader.getPlatformClassLoader());
             URLClassLoader pluginA = new URLClassLoader("plugin-A", plugin, hote);
             URLClassLoader pluginB = new URLClassLoader("plugin-B", plugin, hote)) {

            Class<?> interfaceA = pluginA.loadClass("fr.meteo.SourceMeteo");
            Class<?> interfaceB = pluginB.loadClass("fr.meteo.SourceMeteo");

            System.out.println("meme objet Class      : " + (interfaceA == interfaceB));
            System.out.println("chargee par           : " + interfaceA.getClassLoader().getName());
            System.out.println();

            Object sourceDeA = pluginA.loadClass("fr.plugins.SourceFake").getDeclaredConstructor().newInstance();
            Class<?> hoteDeB = pluginB.loadClass("fr.meteo.Hote");

            System.out.println("Le plugin B utilise un objet du plugin A...");
            afficherResultat(hoteDeB, sourceDeA);
        }
    }

    // =========================================================================
    // Utilitaires : rien a modifier en dessous
    // =========================================================================

    /** Appelle Hote.utiliser(plugin) et affiche le resultat, ou l'erreur telle quelle. */
    private static void afficherResultat(Class<?> hote, Object plugin) throws Exception {
        try {
            Object resultat = hote.getMethod("utiliser", Object.class).invoke(null, plugin);
            System.out.println("Resultat : " + resultat);
        } catch (InvocationTargetException e) {
            System.out.println(e.getCause().getClass().getName());
            System.out.println(e.getCause().getMessage());
        }
    }

    /** Compile tout ce dont les 4 scenes ont besoin. */
    private static void preparer() throws IOException {
        supprimer(DOSSIER);

        // --release 8 : depuis Java 9, javac refuse un package deja fourni par un module du JDK
        compiler("faux-string", null, true, FAUX_STRING);
        compiler("app-string", null, false, APP_STRING);

        compiler("interdite", null, true, CLASSE_INTERDITE);
        compiler("app-interdite", null, false, APP_INTERDITE);

        compiler("api", null, false, API, HOTE);
        compiler("plugin", DOSSIER.resolve("api").toString(), false, PLUGIN, PONT);
    }

    /** Lance fr.app.Main dans un nouveau processus, avec les dossiers donnes dans le classpath. */
    private static void lancerProcessus(String titre, String... dossiers) throws IOException, InterruptedException {
        titre(titre);

        List<String> classpath = new ArrayList<>();
        for (String dossier : dossiers) {
            classpath.add(DOSSIER.resolve(dossier).toString());
        }
        System.out.println("Commande : java -cp " + String.join(File.pathSeparator, List.of(dossiers))
                + " fr.app.Main");
        System.out.println("-".repeat(70));

        String commandeJava = ProcessHandle.current().info().command().orElse("java");
        Process process = new ProcessBuilder(commandeJava, "-cp",
                String.join(File.pathSeparator, classpath), "fr.app.Main")
                .inheritIO()
                .start();
        int code = process.waitFor();
        // Petite attente pour que la sortie d'erreur du processus s'affiche avant la ligne suivante
        Thread.sleep(200);
        System.out.println("-".repeat(70));
        System.out.println("Code de retour : " + code + (code == 0 ? " (succes)" : " (echec)"));
    }

    private static void titre(String titre) {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println(titre);
        System.out.println("=".repeat(70));
    }

    private static URL url(String dossier) throws IOException {
        return DOSSIER.resolve(dossier).toUri().toURL();
    }

    private static void compiler(String nom, String classpath, boolean java8, String... sources) throws IOException {
        Path dossierSources = DOSSIER.resolve("src").resolve(nom);
        List<String> arguments = new ArrayList<>(List.of("-nowarn", "-d", DOSSIER.resolve(nom).toString()));
        if (java8) {
            arguments.addAll(List.of("--release", "8"));
        }
        if (classpath != null) {
            arguments.addAll(List.of("-cp", classpath));
        }
        for (String source : sources) {
            arguments.add(ecrireSource(dossierSources, source).toString());
        }
        int code = outil("javac").run(System.out, System.err, arguments.toArray(String[]::new));
        if (code != 0) {
            throw new IllegalStateException("Compilation impossible : " + nom);
        }
    }

    /** javac du JDK en cours d'execution : ni JAVA_HOME ni PATH ne sont utilises. */
    private static ToolProvider outil(String nom) {
        return ToolProvider.findFirst(nom).orElseThrow(() -> new IllegalStateException(
                nom + " introuvable : lancer ce main avec un JDK, pas un JRE (" + System.getProperty("java.home") + ")"));
    }

    /** Ecrit la source dans package/Classe.java, en lisant le package et le type dans le code. */
    private static Path ecrireSource(Path dossier, String source) throws IOException {
        String paquet = source.lines().filter(l -> l.startsWith("package ")).findFirst().orElseThrow()
                .replace("package ", "").replace(";", "").strip();
        String declaration = source.lines()
                .filter(l -> l.startsWith("public class ") || l.startsWith("public interface "))
                .findFirst().orElseThrow();
        String type = declaration
                .replace("public class ", "").replace("public interface ", "")
                .replace("{", "").strip();
        // "SourceFake implements SourceMeteo" -> "SourceFake"
        type = type.split(" ")[0];
        Path fichier = dossier.resolve(paquet.replace('.', '/')).resolve(type + ".java");
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
