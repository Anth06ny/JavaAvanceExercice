package fr.formation.reactif.tp02_flow_avance;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TP 2 Flow avancé - Tests de l'API Flow.
 * <p>
 * Chaque test est limite a quelques secondes : un flux bloque (request oublie, tampon plein...)
 * fait echouer le test au lieu de le figer.
 */
class FlowTest {

    private static final Duration LIMITE = Duration.ofSeconds(10);
    private static final long ATTENTE_S = 5;

    /** Les entiers de 1 a nb. */
    private static List<Integer> deUnA(int nb) {
        return IntStream.rangeClosed(1, nb).boxed().toList();
    }

    @Test
    void etape1_lentSubscriber_recoitToutDansLOrdre() {
        assertTimeoutPreemptively(LIMITE, () -> {
            LentSubscriber<Integer> abonne = new LentSubscriber<>("test", 10);
            try (SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>()) {
                publisher.subscribe(abonne);
                Emetteur.emettreEnBloquant(publisher, 10);
            }
            assertTrue(abonne.attendreFin(ATTENTE_S), "onComplete doit signaler la fin du flux");
            assertEquals(deUnA(10), abonne.getRecus());
        });
    }

    @Test
    void etape1_lentSubscriber_signaleLaFinEnErreur() {
        assertTimeoutPreemptively(LIMITE, () -> {
            LentSubscriber<Integer> abonne = new LentSubscriber<>("test", 10);
            SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();
            publisher.subscribe(abonne);
            publisher.submit(1);
            publisher.closeExceptionally(new IllegalStateException("panne"));
            assertTrue(abonne.attendreFin(ATTENTE_S), "onError doit signaler la fin du flux");
        });
    }

    @Test
    void etape2_emettreSansAttendre_perdDesElementsSansBloquer() {
        assertTimeoutPreemptively(LIMITE, () -> {
            LentSubscriber<Integer> abonne = new LentSubscriber<>("lent", 300);
            int perdus;
            try (SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>(ForkJoinPool.commonPool(), 4)) {
                publisher.subscribe(abonne);
                perdus = Emetteur.emettreSansAttendre(publisher, 20);
            }
            assertTrue(abonne.attendreFin(ATTENTE_S));
            assertTrue(perdus > 0, "Avec un tampon de 4 et un abonne lent, des elements doivent etre perdus");
            assertEquals(20, abonne.getRecus().size() + perdus, "recus + perdus = emis");
        });
    }

    @Test
    void etape4_filtreProcessor_neGardeQueLesElementsAcceptes() {
        assertTimeoutPreemptively(LIMITE, () -> {
            LentSubscriber<Integer> abonne = new LentSubscriber<>("test", 0);
            FiltreProcessor<Integer> pairs = new FiltreProcessor<>(i -> i % 2 == 0);
            try (SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>()) {
                publisher.subscribe(pairs);
                pairs.subscribe(abonne);
                Emetteur.emettreEnBloquant(publisher, 20);
            }
            assertTrue(abonne.attendreFin(ATTENTE_S), "Le flux doit se terminer : un request(1) oublie ?");
            assertEquals(List.of(2, 4, 6, 8, 10, 12, 14, 16, 18, 20), abonne.getRecus());
        });
    }

    @Test
    void etape4_filtreProcessor_propageLErreur() {
        assertTimeoutPreemptively(LIMITE, () -> {
            LentSubscriber<Integer> abonne = new LentSubscriber<>("test", 0);
            FiltreProcessor<Integer> pairs = new FiltreProcessor<>(i -> i % 2 == 0);
            SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();
            publisher.subscribe(pairs);
            pairs.subscribe(abonne);
            publisher.submit(2);
            publisher.closeExceptionally(new IllegalStateException("panne"));
            assertTrue(abonne.attendreFin(ATTENTE_S), "L'erreur doit arriver jusqu'a l'abonne");
        });
    }

    @Test
    void plusRapides_lotSubscriber_recoitTout() {
        assertTimeoutPreemptively(LIMITE, () -> {
            LotSubscriber<Integer> lot = new LotSubscriber<>(5);
            try (SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>()) {
                publisher.subscribe(lot);
                Emetteur.emettreEnBloquant(publisher, 12);
            }
            assertTrue(lot.attendreFin(ATTENTE_S));
            assertEquals(deUnA(12), lot.getRecus());
        });
    }

    @Test
    void plusRapides_limiteSubscriber_seDesabonneApresMax() {
        assertTimeoutPreemptively(LIMITE, () -> {
            LimiteSubscriber<Integer> limite = new LimiteSubscriber<>(5);
            try (SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>()) {
                publisher.subscribe(limite);
                Emetteur.emettreEnBloquant(publisher, 12);
            }
            assertTrue(limite.attendreFin(ATTENTE_S), "Apres cancel(), l'abonne doit signaler lui-meme sa fin");
            assertEquals(deUnA(5), limite.getRecus());
        });
    }
}
