package fr.formation.reactif.tp02_flow_avance;

import java.util.concurrent.SubmissionPublisher;

import static fr.formation.reactif.tp02_flow_avance.Outils.log;

/**
 * TP 2 Flow avancé - Deux facons d'emettre les entiers de 1 a nb.
 */
public class Emetteur {

    /** Delai d'attente maximum de offer quand le tampon d'un abonne est plein. */
    public static final long DELAI_OFFER_MS = 100;

    /**
     * Emet avec submit : bloque le thread appelant tant que le tampon d'un abonne est plein.
     */
    public static void emettreEnBloquant(SubmissionPublisher<Integer> publisher, int nb) {
        for (int i = 1; i <= nb; i++) {
            log("émet " + i);
            publisher.submit(i);
        }
    }

    /**
     * Etape 2.3 : emet avec offer. Si le tampon d'un abonne est plein, attend au plus DELAI_OFFER_MS
     * puis abandonne l'element pour cet abonne, en affichant "perdu pour [abonne] : [element]".
     *
     * @return le nombre total d'elements perdus, tous abonnes confondus
     */
    public static int emettreSansAttendre(SubmissionPublisher<Integer> publisher, int nb) {
        // TODO etape 2.3 : offer(element, delai, unite, onDrop), compter les elements perdus dans onDrop
        throw new UnsupportedOperationException("TODO étape 2.3");
    }
}
