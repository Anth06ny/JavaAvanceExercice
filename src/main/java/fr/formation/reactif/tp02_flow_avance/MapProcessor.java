package fr.formation.reactif.tp02_flow_avance;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Function;

/**
 * TP 2 Flow avancé - Etape 4 : Processor qui transforme chaque element, deja ecrit (voir le cours).
 */
public class MapProcessor<T, R> extends SubmissionPublisher<R> implements Flow.Processor<T, R> {

    private final Function<T, R> transformation;
    private Flow.Subscription subscription;

    public MapProcessor(Function<T, R> transformation) {
        this.transformation = transformation;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(T item) {
        submit(transformation.apply(item)); // Republie l'element transforme
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        closeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
        close();
    }
}
