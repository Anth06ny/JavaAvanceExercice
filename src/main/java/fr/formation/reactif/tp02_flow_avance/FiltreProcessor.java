package fr.formation.reactif.tp02_flow_avance;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Predicate;

import static fr.formation.reactif.tp02_flow_avance.Outils.log;

/**
 * TP 2 Flow avancé - Etape 4 : Processor qui ne republie que les elements acceptes par le filtre.
 * S'inspirer de MapProcessor.
 */
public class FiltreProcessor<T> extends SubmissionPublisher<T> implements Flow.Processor<T, T> {

    private final Predicate<T> filtre;
    private Flow.Subscription subscription;

    public FiltreProcessor(Predicate<T> filtre) {
        this.filtre = filtre;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        // TODO etape 4
    }

    @Override
    public void onNext(T item) {
        // TODO etape 4 : republier l'element s'il passe le filtre, sinon afficher "filtre écarte [item]" avec log
    }

    @Override
    public void onError(Throwable throwable) {
        // TODO etape 4 : transmettre l'erreur au maillon suivant
    }

    @Override
    public void onComplete() {
        // TODO etape 4 : transmettre la fin du flux au maillon suivant
    }
}
