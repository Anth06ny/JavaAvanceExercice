package fr.formation.prog_concurrente.tp06_verrou;

import java.util.ArrayList;
import java.util.List;

public class Billetterie {

    private int placesLibres;
    private final List<String> reservations = new ArrayList<>();

    public Billetterie(int nbPlaces) {
        this.placesLibres = nbPlaces;
    }

    /**
     * Reserve une place pour le client.
     *
     * @return true si la place a ete reservee, false si la salle est complete
     * ou si le client a deja une place.
     */
    public boolean reserver(String client) {
        if (placesLibres > 0 && !reservations.contains(client)) {
            verifierPaiement();          // entre la verification et la modification...
            placesLibres--;
            reservations.add(client);
            return true;
        }
        return false;
    }

    /**
     *Annule la reservation du client et lui rend sa place.
     *
     * @return true si le client avait une reservation.
     */
    public boolean annuler(String client) {
        return false;
    }

    public int getPlacesLibres() {
        return placesLibres;
    }

    /**
     * Copie de la liste : l'appelant ne peut pas modifier la liste interne.
     */
    public List<String> getReservations() {
        return List.copyOf(reservations);
    }

    /**
     * Simule la verification du paiement
     */
    private static void verifierPaiement() {
        long fin = System.nanoTime() + 50_000;
        while (System.nanoTime() < fin) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }
}
