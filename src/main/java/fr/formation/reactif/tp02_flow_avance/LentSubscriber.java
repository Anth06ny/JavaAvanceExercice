package fr.formation.reactif.tp02_flow_avance;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

import static fr.formation.reactif.tp02_flow_avance.Outils.log;
import static fr.formation.reactif.tp02_flow_avance.Outils.pause;

/**
 * TP 2 Flow avancé - Etape 1 : un abonne qui traite les elements un par un, lentement.
 */
public class LentSubscriber<T> implements Flow.Subscriber<T> {

    private final String nom;
    private final long delaiMs;
    private final List<T> recus = new CopyOnWriteArrayList<>();
    /** Passe a 0 a la fin du flux (onComplete ou onError). */
    private final CountDownLatch termine = new CountDownLatch(1);

    private Flow.Subscription subscription;

    /**
     * @param nom     nom affiche dans la console
     * @param delaiMs duree du traitement de chaque element
     */
    public LentSubscriber(String nom, long delaiMs) {
        this.nom = nom;
        this.delaiMs = delaiMs;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        // TODO etape 1 : garder la subscription, puis demander le premier element
    }

    @Override
    public void onNext(T item) {
        // TODO etape 1 : afficher "[nom] traite [item]" avec log, ajouter l'element a recus,
        //  simuler le traitement avec pause(delaiMs), puis demander l'element suivant
    }

    @Override
    public void onError(Throwable throwable) {
        // TODO etape 1 : afficher l'erreur avec log, puis signaler la fin du flux (termine)
    }

    @Override
    public void onComplete() {
        // TODO etape 1 : afficher "[nom] terminé" avec log, puis signaler la fin du flux (termine)
    }

    /** Copie des elements recus. */
    public List<T> getRecus() {
        return List.copyOf(recus);
    }

    /**
     * Attend la fin du flux.
     *
     * @return false si le flux n'est pas termine au bout du delai
     */
    public boolean attendreFin(long secondes) throws InterruptedException {
        return termine.await(secondes, TimeUnit.SECONDS);
    }

    @Override
    public String toString() {
        return nom;
    }
}
