package fr.formation.reactif.tp02_flow_avance;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

import static fr.formation.reactif.tp02_flow_avance.Outils.log;
import static fr.formation.reactif.tp02_flow_avance.Outils.pause;

/**
 * TP 2 Flow avancé - Pour les plus rapides : un abonne qui demande les elements par lots.
 */
public class LotSubscriber<T> implements Flow.Subscriber<T> {

    private final int taille;
    private final List<T> recus = new CopyOnWriteArrayList<>();
    private final CountDownLatch termine = new CountDownLatch(1);

    private Flow.Subscription subscription;

    /**
     * @param taille nombre d'elements demandes a chaque request
     */
    public LotSubscriber(int taille) {
        this.taille = taille;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        // TODO plus rapides : demander un premier lot de taille elements, en l'affichant avec log
    }

    @Override
    public void onNext(T item) {
        // TODO plus rapides : ajouter l'element a recus, pause(50),
        //  et redemander un lot seulement quand le lot en cours est entierement recu
    }

    @Override
    public void onError(Throwable throwable) {
        // TODO plus rapides
    }

    @Override
    public void onComplete() {
        // TODO plus rapides
    }

    public List<T> getRecus() {
        return List.copyOf(recus);
    }

    public boolean attendreFin(long secondes) throws InterruptedException {
        return termine.await(secondes, TimeUnit.SECONDS);
    }
}
