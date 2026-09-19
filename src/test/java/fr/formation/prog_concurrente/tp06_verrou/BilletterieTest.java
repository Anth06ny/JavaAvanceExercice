package fr.formation.prog_concurrente.tp06_verrou;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TP 6 - Tests de la billetterie.
 * <p>
 * Un bug de concurrence ne se produit pas a chaque execution :
 * les tests concurrents sont repetes plusieurs fois (@RepeatedTest).
 */
class BilletterieTest {

    @Test
    void unSeulThread_reservationsNormales() {
        Billetterie billetterie = new Billetterie(2);

        assertTrue(billetterie.reserver("alice"));
        assertFalse(billetterie.reserver("alice"), "Un client ne peut reserver qu'une place");
        assertTrue(billetterie.reserver("bob"));
        assertFalse(billetterie.reserver("chloe"), "La salle est complete");

        assertEquals(0, billetterie.getPlacesLibres());
        assertEquals(List.of("alice", "bob"), billetterie.getReservations());
    }

    @RepeatedTest(20)
    void milleTentativesEnMemeTemps_jamaisPlusDeReservationsQueDePlaces() throws InterruptedException {
        Billetterie billetterie = new Billetterie(100);
        AtomicInteger reponsesOui = new AtomicInteger();

        // 500 clients qui tentent chacun 2 fois
        lancerEnMemeTemps(1000, i -> {
            if (billetterie.reserver("client-" + (i % 500))) {
                reponsesOui.incrementAndGet();
            }
        });

        List<String> reservations = billetterie.getReservations();
        assertEquals(100, reponsesOui.get(), "Nombre de reponses \"reserve\"");
        assertEquals(100, reservations.size(), "Nombre de reservations");
        assertEquals(100, new HashSet<>(reservations).size(), "Un client a obtenu plusieurs places");
        assertEquals(0, billetterie.getPlacesLibres(), "Places libres restantes");
    }

    @RepeatedTest(20)
    void reservationsEtAnnulationsEnMemeTemps_placesToujoursCoherentes() throws InterruptedException {
        Billetterie billetterie = new Billetterie(50);

        // 50 clients reservent, annulent, puis reservent a nouveau, tous en meme temps
        lancerEnMemeTemps(50, i -> {
            String client = "client-" + i;
            billetterie.reserver(client);
            billetterie.annuler(client);
            billetterie.reserver(client);
        });

        assertEquals(50, billetterie.getReservations().size(), "Nombre de reservations");
        assertEquals(0, billetterie.getPlacesLibres(), "Places libres restantes");
    }

    /**
     * Lance nb taches sur des threads virtuels, qui demarrent toutes au meme moment,
     * et fait echouer le test si l'une d'elles a leve une exception.
     */
    private static void lancerEnMemeTemps(int nb, IntConsumer tache) throws InterruptedException {
        CountDownLatch topDepart = new CountDownLatch(1);
        Queue<Throwable> erreurs = new ConcurrentLinkedQueue<>();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < nb; i++) {
                int numero = i;
                executor.submit(() -> {
                    try {
                        topDepart.await();
                        tache.accept(numero);
                    } catch (Throwable e) {
                        erreurs.add(e);
                    }
                });
            }
            topDepart.countDown();
        }

        if (!erreurs.isEmpty()) {
            fail("Exception dans un thread : " + erreurs.peek(), erreurs.peek());
        }
    }
}
