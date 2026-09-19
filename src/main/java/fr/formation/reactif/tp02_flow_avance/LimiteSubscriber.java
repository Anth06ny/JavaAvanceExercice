package fr.formation.reactif.tp02_flow_avance;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

import static fr.formation.reactif.tp02_flow_avance.Outils.log;

/**
 * TP 2 Flow avancé - Pour les plus rapides : un abonne qui se desabonne apres max elements.
 */
public class LimiteSubscriber<T> implements Flow.Subscriber<T> {

    private final int max;
    private final List<T> recus = new CopyOnWriteArrayList<>();
    private final CountDownLatch termine = new CountDownLatch(1);

    private Flow.Subscription subscription;

    /**
     * @param max nombre d'elements a recevoir avant de se desabonner
     */
    public LimiteSubscriber(int max) {
        this.max = max;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        // TODO plus rapides
    }

    @Override
    public void onNext(T item) {
        // TODO plus rapides : au max-ieme element, se desabonner (cancel)
        //  Attention : apres cancel(), ni onComplete ni onError ne seront appeles
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
