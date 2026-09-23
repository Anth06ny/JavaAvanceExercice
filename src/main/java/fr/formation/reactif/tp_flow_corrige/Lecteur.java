package fr.formation.reactif.tp_flow_corrige;

import java.util.concurrent.Flow;

/**
 * TP 1 Flow - Etape 1 : le lecteur abonne au magazine.
 */
public class Lecteur implements Flow.Subscriber<Numero> {

    private Journal journal;
    private volatile Flow.Subscription carte = null;

    public Lecteur(Journal journal) {
        this.journal = journal;
    }

    //Demander @number numéro
    public void request(int number){
        carte.request(number);
    }

    @Override
    public void onSubscribe(Flow.Subscription carte) {
        this.carte = carte;
        journal.afficher("Je reçois ma carte d'abonné");
        //J'accepte tous les numéros de l'editeur
        //carte.request(Long.MAX_VALUE);
        carte.request(5);
    }


    @Override
    public void onNext(Numero item) {
        journal.afficher("J'ai reçu " + item.numero() + "-" + item.titre());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        journal.afficher("J'ai fini de le lire");
    }

    @Override
    public void onError(Throwable throwable) {
        journal.afficher("Erreur : " + throwable.getMessage());
    }

    @Override
    public void onComplete() {
        journal.afficher("Fin de l'abonnement");
    }

    /** "Je résilie mon abonnement". */
    public void resilier() {
        journal.afficher("Je résilie mon abonnement");
        carte.cancel();
    }
}
