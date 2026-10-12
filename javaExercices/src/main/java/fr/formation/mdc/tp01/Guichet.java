package fr.formation.mdc.tp01;

import java.util.List;

/**
 * TP 1 (Journalisation) - DU PRINTLN AU LOGGER
 * <p>
 * Un guichet vend les 5 places d'un concert a 4 clients.
 * L'un d'eux en demande plus qu'il n'en reste : sa reservation echoue.
 * <p>
 * Point de depart : tous les affichages sont des System.out.println, a remplacer par des logs.
 */
public class Guichet {

    /** A partir de ce nombre de places restantes, on previent que le concert est presque complet. */
    private static final int SEUIL_ALERTE = 2;

    private int placesRestantes = 5;

    /** Une demande de reservation : le client et le nombre de places voulu. */
    private record Demande(String client, int places) {
    }

    public static void main(String[] args) {
        Guichet guichet = new Guichet();
        System.out.println("Ouverture du guichet : " + guichet.placesRestantes + " places");

        List<Demande> demandes = List.of(
                new Demande("Alice", 2),
                new Demande("Bob", 1),
                new Demande("Chloé", 3),
                new Demande("David", 1));

        for (Demande demande : demandes) {
            try {
                guichet.reserver(demande.client(), demande.places());
            } catch (IllegalStateException e) {
                System.out.println("Réservation impossible pour " + demande.client());
                e.printStackTrace();
            }
        }

        System.out.println("Fermeture du guichet : " + guichet.placesRestantes + " place(s) invendue(s)");
    }

    /**
     * Reserve des places pour un client.
     *
     * @param client nom du client
     * @param places nombre de places voulu
     * @throws IllegalStateException s'il ne reste pas assez de places
     */
    public void reserver(String client, int places) {
        System.out.println("Demande de " + places + " place(s) pour " + client);

        if (places > placesRestantes) {
            throw new IllegalStateException("Plus assez de places : " + placesRestantes + " restante(s)");
        }

        System.out.println("Calcul des places restantes : " + placesRestantes + " - " + places);
        placesRestantes -= places;
        System.out.println("Réservation acceptée pour " + client);

        if (placesRestantes <= SEUIL_ALERTE) {
            System.out.println("Concert presque complet : " + placesRestantes + " place(s) restante(s)");
        }
    }
}
