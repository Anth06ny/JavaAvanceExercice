package fr.formation.reactif.tp02_flow_avance;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.SubmissionPublisher;

import static fr.formation.reactif.tp02_flow_avance.Emetteur.emettreEnBloquant;
import static fr.formation.reactif.tp02_flow_avance.Emetteur.emettreSansAttendre;
import static fr.formation.reactif.tp02_flow_avance.Outils.log;

/**
 * TP 2 Flow avancé - API Flow. Changer ETAPE pour lancer chaque etape.
 */
public class Tp2FlowAvance {

    /** Etape a lancer, de 1 a 5. */
    private static final int ETAPE = 1;

    /** Taille du tampon de chaque abonne (etapes 2 et 3). */
    private static final int TAMPON = 4;

    private static final int NB_ELEMENTS = 20;

    /** Au-dela, on considere que le flux est bloque. */
    private static final long ATTENTE_MAX_S = 10;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=".repeat(70));
        switch (ETAPE) {
            case 1 -> etape1PremierAbonne();
            case 2 -> etape2Backpressure();
            case 3 -> etape3DeuxVitesses();
            case 4 -> etape4Processors();
            case 5 -> etape5PourLesPlusRapides();
            default -> System.out.println("Choisissez une valeur entre 1 et 5.");
        }
        System.out.println("=".repeat(70));
    }

    // =========================================================================
    // ETAPE 1 : premier abonne
    // =========================================================================

    private static void etape1PremierAbonne() throws InterruptedException {
        LentSubscriber<Integer> abonne = new LentSubscriber<>("lent", 200);

        try (SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>()) {
            publisher.subscribe(abonne);
            emettreEnBloquant(publisher, 10);
        } // close() : onComplete sera envoye apres les elements en attente

        log("close() appelé, le main attend la fin du flux");
        verifierFin(abonne.attendreFin(ATTENTE_MAX_S)); // Etape 1.3 : commenter cette ligne
        log("fin du main");
    }

    // =========================================================================
    // ETAPE 2 : backpressure
    // =========================================================================

    private static void etape2Backpressure() throws InterruptedException {
        LentSubscriber<Integer> abonne = new LentSubscriber<>("lent", 300);

        try (SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>(ForkJoinPool.commonPool(), TAMPON)) {
            publisher.subscribe(abonne);
            emettreEnBloquant(publisher, NB_ELEMENTS);                                  // Etapes 2.1 et 2.2
            // log("perdus : " + emettreSansAttendre(publisher, NB_ELEMENTS));          // Etape 2.3
        }

        verifierFin(abonne.attendreFin(ATTENTE_MAX_S));
        log("reçus : " + abonne.getRecus().size() + " / " + NB_ELEMENTS);
    }

    // =========================================================================
    // ETAPE 3 : deux abonnes, deux vitesses
    // =========================================================================

    private static void etape3DeuxVitesses() throws InterruptedException {
        LentSubscriber<Integer> rapide = new LentSubscriber<>("rapide", 20);
        LentSubscriber<Integer> lent = new LentSubscriber<>("lent", 300);

        try (SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>(ForkJoinPool.commonPool(), TAMPON)) {
            publisher.subscribe(rapide);
            publisher.subscribe(lent);
            emettreEnBloquant(publisher, NB_ELEMENTS);                                  // Etape 3.1
            // log("perdus : " + emettreSansAttendre(publisher, NB_ELEMENTS));          // Etape 3.2
        }

        verifierFin(rapide.attendreFin(ATTENTE_MAX_S));
        verifierFin(lent.attendreFin(ATTENTE_MAX_S));
        log("rapide a reçu " + rapide.getRecus().size() + ", lent a reçu " + lent.getRecus().size());
    }

    // =========================================================================
    // ETAPE 4 : chaine de Processors
    // =========================================================================

    private static void etape4Processors() throws InterruptedException {
        LentSubscriber<String> abonne = new LentSubscriber<>("abonné", 100);
        FiltreProcessor<Integer> pairs = new FiltreProcessor<>(i -> i % 2 == 0);
        MapProcessor<Integer, String> enTexte = new MapProcessor<>(i -> "Élément " + i);

        try (SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>()) {
            // publisher -> pairs -> enTexte -> abonne
            publisher.subscribe(pairs);
            pairs.subscribe(enTexte);
            enTexte.subscribe(abonne);
            emettreEnBloquant(publisher, NB_ELEMENTS);
        }

        verifierFin(abonne.attendreFin(ATTENTE_MAX_S));
        log("reçus : " + abonne.getRecus());
    }

    // =========================================================================
    // POUR LES PLUS RAPIDES
    // =========================================================================

    private static void etape5PourLesPlusRapides() throws InterruptedException {
        titre("5.1 : demande par lots");
        LotSubscriber<Integer> lot = new LotSubscriber<>(5);
        try (SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>()) {
            publisher.subscribe(lot);
            emettreEnBloquant(publisher, 12);
        }
        verifierFin(lot.attendreFin(ATTENTE_MAX_S));

        titre("5.2 : désabonnement après 5 éléments");
        LimiteSubscriber<Integer> limite = new LimiteSubscriber<>(5);
        try (SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>()) {
            publisher.subscribe(limite);
            emettreEnBloquant(publisher, 12);
        }
        verifierFin(limite.attendreFin(ATTENTE_MAX_S));
        log("limite a reçu " + limite.getRecus());

        titre("5.3 : la source tombe en panne après 5 éléments");
        LentSubscriber<Integer> victime = new LentSubscriber<>("victime", 50);
        SubmissionPublisher<Integer> enPanne = new SubmissionPublisher<>();
        enPanne.subscribe(victime);
        emettreEnBloquant(enPanne, 5);
        enPanne.closeExceptionally(new IllegalStateException("source en panne"));
        verifierFin(victime.attendreFin(ATTENTE_MAX_S));
        log("victime a reçu " + victime.getRecus());

        titre("5.4 : exception dans onNext");
        LentSubscriber<Integer> fragile = new LentSubscriber<>("fragile", 50) {
            @Override
            public void onNext(Integer item) {
                if (item == 3) {
                    throw new IllegalStateException("élément 3 illisible");
                }
                super.onNext(item);
            }
        };
        try (SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>()) {
            publisher.subscribe(fragile);
            emettreEnBloquant(publisher, 6);
        }
        verifierFin(fragile.attendreFin(ATTENTE_MAX_S));
        log("fragile a reçu " + fragile.getRecus());
    }

    // =========================================================================
    // Outils
    // =========================================================================

    private static void verifierFin(boolean termine) {
        if (!termine) {
            log("Pas de fin de flux au bout de " + ATTENTE_MAX_S + " s : étape pas encore faite, ou flux bloqué");
        }
    }

    private static void titre(String titre) {
        System.out.println();
        System.out.println("=== " + titre + " ===");
    }
}
