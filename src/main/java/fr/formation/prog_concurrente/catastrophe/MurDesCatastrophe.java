package fr.formation.prog_concurrente.catastrophe;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public class MurDesCatastrophe {



    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(70));

        final int CATASTROPHE = 4;

        switch (CATASTROPHE) {
            case 1 -> scene1LeCompteurQuiMent();
            case 2 -> scene2LaCollectionQuiPerdDesDonnees();
            case 3 -> scene3LeDrapeauQuiNeSArreteJamais();
            case 4 -> scene4LaMemoireQuiExplose();
            case 5 -> scene5LeProgrammeQuiSeFige();
            case 6 -> scene6LErreurQuePersonneNeVoit();
            default -> System.out.println("Choisissez une valeur entre 1 et 6.");
        }
        System.out.println("=".repeat(70));
    }


    // =========================================================================
    // SCENE 1 : LE COMPTEUR QUI MENT
    // -------------------------------------------------------------------------
    // Dix threads incrementent la meme variable, 100 000 fois chacun.
    // =========================================================================

    /** Volontairement partagee entre tous les threads, et volontairement nue. */
    private static int compteur = 0;

    private static void scene1LeCompteurQuiMent() throws InterruptedException {
        System.out.println("SCENE 1 : le compteur qui ment");
        System.out.println("=".repeat(70));

        final int nbThreads = 10;
        final int nbIncrementsParThread = 100_000;

        compteur = 0;

        // Le "top depart" : sans lui, le premier thread a souvent fini avant
        // que le dernier ne demarre, et le bug ne se produit pas.
        CountDownLatch topDepart = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < nbThreads; i++) {
            Thread t = new Thread(() -> {
                attendre(topDepart);
                for (int j = 0; j < nbIncrementsParThread; j++) {
                    compteur++;
                }
            });
            t.start();
            threads.add(t);
        }

        topDepart.countDown();          // tout le monde part en meme temps
        for (Thread t : threads) {
            t.join();
        }

        System.out.println("Obtenu  : " + compteur);
    }


    // =========================================================================
    // SCENE 2 : LA COLLECTION QUI PERD DES DONNEES
    // -------------------------------------------------------------------------
    // Huit threads inserent 10 000 entrees chacun, toutes avec des cles
    // differentes. La map devrait contenir 80 000 entrees. Elle en contient
    // moins.
    //
    // =========================================================================

    private static void scene2LaCollectionQuiPerdDesDonnees() throws InterruptedException {
        System.out.println("SCENE 2 : la collection qui perd des donnees");
        System.out.println("=".repeat(70));

        final int nbThreads = 8;
        final int nbEntreesParThread = 10_000;

        Map<Integer, String> map = new HashMap<>();

        CountDownLatch topDepart = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();

        // On attrape les exceptions au lieu de les laisser inonder la console :
        // les piles d'appel sont spectaculaires mais illisibles a 8 threads.
        AtomicInteger threadsEnErreur = new AtomicInteger();
        AtomicReference<Throwable> premiereErreur = new AtomicReference<>();

        for (int i = 0; i < nbThreads; i++) {
            final int idThread = i;
            Thread t = new Thread(() -> {
                attendre(topDepart);
                try {
                    for (int j = 0; j < nbEntreesParThread; j++) {
                        // Chaque cle est unique : aucun ecrasement legitime possible.
                        map.put(idThread * nbEntreesParThread + j, "valeur");
                    }
                } catch (Throwable e) {
                    threadsEnErreur.incrementAndGet();
                    premiereErreur.compareAndSet(null, e);
                }
            });
            t.start();
            threads.add(t);
        }

        topDepart.countDown();

        // Une HashMap maltraitee par plusieurs threads a DEUX facons de casser.
        // On ne sait pas a l'avance laquelle on va obtenir : c'est precisement
        // ce qui rend ce bug si desagreable en production.
        // /!\ Le delai est GLOBAL : 10 secondes pour l'ensemble des threads.
        long limite = System.currentTimeMillis() + 10_000;
        boolean tousTermines = true;
        for (Thread t : threads) {
            long reste = limite - System.currentTimeMillis();
            if (reste > 0) {
                t.join(reste);
            }
            if (t.isAlive()) {
                tousTermines = false;
            }
        }

        int attendu = nbThreads * nbEntreesParThread;

        if (!tousTermines) {
            // CAS 1 : boucle infinie pendant le redimensionnement interne.
            System.out.println("""
CAS 1 : PROGRAMME EST BLOQUE.
Un ou plusieurs threads tournent en boucle infinie à l'intérieur de HashMap.put(), pendant que la map se redimensionne. Ils n'en sortiront jamais.
            """);
            for (Thread t : threads) {
                if (t.isAlive()) {
                    System.out.println("     bloque -> " + t.getName()
                            + "  (etat " + t.getState() + ")");
                }
            }
            System.out.println();
            System.out.println("Une simple HashMap peut donc figer une application entière. Sans exception, sans log, sans rien.");
        }
        else {
            // CAS 2 : tout le monde a fini.
            System.out.println("CAS 2 : Tout le monde a fini. ");
            System.out.println("Attendu : " + attendu + " entrees");
            System.out.println("Obtenu  : " + map.size() + " entrees");
            System.out.println("Perdues : " + (attendu - map.size()));
        }

        if (threadsEnErreur.get() > 0) {
            Throwable e = premiereErreur.get();
            System.out.println(threadsEnErreur.get() + " thread(s) sur " + nbThreads
                    + " ont plante en pleine écriture :");
            System.out.println("""
                La structure INTERNE de la HashMap est corrompue.
                Ce n'est plus une question de donnees  perdues, l'objet lui-meme n'est plus coherent.
                """);
            e.printStackTrace();
        }
        System.exit(0);
    }


    // =========================================================================
    // SCENE 3 : LE DRAPEAU QUI NE S'ARRETE JAMAIS
    // -------------------------------------------------------------------------
    // Un thread boucle tant que stop vaut false. Le main met stop a true.
    // Le thread ne s'arrete pas.
    //
    // Reponse apportee au module M4 (modele memoire et visibilite).
    //
    // /!\ SCENE LA PLUS FRAGILE DU TP : elle depend du compilateur JIT.
    //     Si elle s'arrete normalement chez vous, c'est deja une information
    //     interessante -- et c'est tout le probleme de ce genre de bug.
    // =========================================================================

    /** Volontairement PAS volatile. C'est tout le sujet. */
    private static boolean stop = false;

    private static void scene3LeDrapeauQuiNeSArreteJamais() throws InterruptedException {
        System.out.println("SCENE 3 : le drapeau qui ne s'arrete jamais");
        System.out.println("=".repeat(70));

        stop = false;

        Thread worker = new Thread(() -> {
            System.out.println("Le worker démarre");
            long tours = 0;
            while (!stop) {
                tours++;
            }
            System.out.println("[worker] je me suis arrêté apres " + tours + " tours");
        }, "worker");

        worker.start();

        Thread.sleep(2000);

        System.out.println("[main] stop = true");
        stop = true;

        System.out.println("[main] On attend 8s que le worker termine...");
        worker.join(8000);

        if (worker.isAlive()) {
            System.out.println();
            System.out.println("Le worker tourne toujours et ne s'arretera jamais");
            System.exit(0);
        }
    }



    // =========================================================================
    // SCENE 4 : LA MEMOIRE QUI EXPLOSE
    // -------------------------------------------------------------------------
    // On créé des threads, encore et encore
    // =========================================================================

    /**
     * Augmentez si votre machine suit. 20 000 suffit a faire passer le message
     * en une trentaine de secondes
     */
    private static final int NB_THREADS_SCENE_4 = 20_000;

    private static void scene4LaMemoireQuiExplose() {
        System.out.println("SCENE 4 : le prix d'un thread");
        System.out.println("=".repeat(70));
        System.out.println("On créé " + NB_THREADS_SCENE_4 + " threads qui ne font que dormir.");

        List<Thread> threads = new ArrayList<>();
        int crees = 0;
        boolean crash = false;

        long debut = System.currentTimeMillis();
        try {
            for (int i = 0; i < NB_THREADS_SCENE_4; i++) {
                Thread t = new Thread(() -> dormir(120_000));
                threads.add(t);
                t.start();
                crees++;

                if (crees % 2_500 == 0) {
                    long ms = System.currentTimeMillis() - debut;
                    System.out.println("  " + crees + " threads vivants   (" + ms + " ms)");
                }
            }
        } catch (OutOfMemoryError e) {
            crash = true;
            System.out.println();
            System.out.println(">> CRASH : " + e.getClass().getName());
            System.out.println(">> " + e.getMessage());
        }

        long duree = System.currentTimeMillis() - debut;

        System.out.println();
        System.out.println("-".repeat(70));
        System.out.println("  Threads crees      : " + crees);
        System.out.println("  Temps total        : " + duree + " ms");
        System.out.println("  Cout unitaire      : " + String.format("%.3f", (double) duree / Math.max(crees, 1)) + " ms / thread");
        System.out.println("  Threads vivants    : "  + ManagementFactory.getThreadMXBean().getThreadCount());
        System.out.println("-".repeat(70));
        System.out.println();

        if (crash) {
            System.out.println("Votre machine a dit non avant d'arriver au bout.");
        } else {
            System.out.println("Votre machine a tenu");
        }
        System.out.println(">> Chaque thread reserve environ 1 Mo de pile, et sa creation");
        System.out.println(">> passe par le systeme d'exploitation.");
        System.out.println();
        System.out.println(">> NOTEZ CES DEUX CHIFFRES : " + crees + " threads, " + duree + " ms.");
        System.out.println(">> Cette scene est la seule du mur qui n'aura pas sa reponse");
        System.out.println(">> aujourd'hui. Rendez-vous demain matin.");

        System.exit(0);   // on n'attend pas la fin des dodos
    }


    // =========================================================================
    // SCENE 5 : LE PROGRAMME QUI SE FIGE
    // -------------------------------------------------------------------------
    // Deux threads transferent de l'argent entre deux comptes, mais pas dans
    // le meme sens. Chacun attend le verrou que l'autre detient.
    // =========================================================================

    private static final Object COMPTE_A = new Object();
    private static final Object COMPTE_B = new Object();

    private static void scene5LeProgrammeQuiSeFige() throws InterruptedException {
        System.out.println("SCENE 5 : le programme qui se fige");
        System.out.println("=".repeat(70));

        Thread versB = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                synchronized (COMPTE_A) {
                    dormir(10);                     // elargit la fenetre de tir
                    synchronized (COMPTE_B) {
                        // le transfert lui-meme n'a aucune importance ici
                        System.out.println("B donne à A");
                    }
                }
            }
        }, "transfert-A-vers-B");

        Thread versA = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                synchronized (COMPTE_B) {           // <-- ordre inverse
                    dormir(10);
                    synchronized (COMPTE_A) {
                        System.out.println("A donne à B");
                    }
                }
            }
        }, "transfert-B-vers-A");

        versB.start();
        versA.start();

        System.out.println("2000 transferts lances. On attend 5 secondes...");
        versB.join(5000);
        versA.join(5000);

        if (versB.isAlive() || versA.isAlive()) {
            System.out.println();
            System.out.println(">> FIGE. Les deux threads s'attendent mutuellement.");
            System.out.println();

            // La JVM le sait. Personne ne le lui demande jamais.
            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            long[] bloques = bean.findDeadlockedThreads();
            if (bloques != null) {
                System.out.println(">> La JVM avait la reponse depuis le debut :");
                for (long id : bloques) {
                    System.out.println("     bloque -> " + bean.getThreadInfo(id).getThreadName());
                }
            }
            System.out.println();
            System.out.println(">> A essayer maintenant, avant de passer a la suite :");
            System.out.println(">>   jcmd " + ProcessHandle.current().pid() + " Thread.print");
            System.out.println(">> Cherchez la ligne 'Found one Java-level deadlock'.");
            System.exit(0);
        } else {
            System.out.println();
            System.out.println(">> Pas de blocage cette fois. Relancez : ca finira par arriver.");
        }
    }


    // =========================================================================
    // SCENE 6 : L'ERREUR QUE PERSONNE NE VOIT
    // -------------------------------------------------------------------------
    // Une tache soumise a un pool leve une exception. La console reste vide.
    // L'application continue comme si tout allait bien.
    // =========================================================================

    private static void scene6LErreurQuePersonneNeVoit() throws InterruptedException {
        System.out.println("SCENE 6 : l'erreur que personne ne voit");
        System.out.println("=".repeat(70));

        ExecutorService executor = Executors.newFixedThreadPool(2);

        System.out.println("--- Tache soumise avec submit() ---");
        executor.submit(() -> {
            throw new IllegalStateException("La base de donnees est injoignable !");
        });
        Thread.sleep(500);
        System.out.println(">>Aucune trace. L'application continue...");
        System.out.println();

        System.out.println("--- La meme, soumise avec execute() ---");
        executor.execute(() -> {
            throw new IllegalStateException("La base de donnees est injoignable !");
        });
        Thread.sleep(500);
        System.out.println();

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        System.out.println(">> Deux lignes de code presque identiques.");
        System.out.println(">> Une seule vous previent que votre traitement a echoue.");
        System.out.println(">> Combien de submit() y a-t-il dans votre code ?");
    }


    // =========================================================================
    // Petits utilitaires, sans interet pedagogique
    // =========================================================================

    private static void dormir(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void attendre(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
