package fr.formation.prog_concurrente.tp06_verrou;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TP 6 - LA BILLETTERIE
 * <p>
 * 100 places, 500 clients. Chaque client essaie de reserver 2 fois (double clic),
 * soit 1 000 tentatives lancees en meme temps.
 * <p>
 * Resultat attendu : 100 reservations, 100 clients differents, 0 place libre.
 */
public class Tp6Billetterie {

    private static final int NB_PLACES = 100;
    private static final int NB_CLIENTS = 500;
    private static final int TENTATIVES_PAR_CLIENT = 2;

    public static void main(String[] args) throws InterruptedException {
        Billetterie billetterie = new Billetterie(NB_PLACES);

        //Client à qui on a répondu qu'ils avaient une place
        AtomicInteger reponsesOui = new AtomicInteger();
        AtomicInteger exceptions = new AtomicInteger();
        CountDownLatch topDepart = new CountDownLatch(1);

        // Une tache par tentative, sur des threads virtuels
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < NB_CLIENTS * TENTATIVES_PAR_CLIENT; i++) {
                String client = "client-" + (i % NB_CLIENTS);
                executor.submit(() -> {
                    try {
                        topDepart.await();                  // tout le monde part en meme temps
                        if (billetterie.reserver(client)) {
                            reponsesOui.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // submit() garderait l'exception dans le Future : on la compte (Catastrophe 6)
                        exceptions.incrementAndGet();
                    }
                });
            }
            topDepart.countDown();
        }   // attend la fin de toutes les tentatives

        List<String> reservations = billetterie.getReservations();
        int clientsDifferents = new HashSet<>(reservations).size();

        System.out.println("Places disponibles : " + NB_PLACES);
        System.out.println(reservations.size() + " (reservations) + " +  billetterie.getPlacesLibres() + " (places restantes) = " +  (reservations.size() + billetterie.getPlacesLibres()) );

        System.out.println("Clients différents      : " + clientsDifferents);
        System.out.println("Confirmations envoyées : " + reponsesOui.get());
        System.out.println("Exceptions              : " + exceptions.get());

        boolean correct = reponsesOui.get() == NB_PLACES
                && reservations.size() == NB_PLACES
                && clientsDifferents == NB_PLACES
                && billetterie.getPlacesLibres() == 0
                && exceptions.get() == 0;

        System.out.println();
        System.out.println(correct ? ">> Billetterie correcte" : ">> BILLETTERIE INCOHERENTE");
    }
}
