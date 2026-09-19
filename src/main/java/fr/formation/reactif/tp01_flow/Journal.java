package fr.formation.reactif.tp01_flow;

/**
 * TP 1 Flow - Affiche un message dans le journal de la fenetre, deja ecrit.
 * <p>
 * Utilisable depuis n'importe quel thread : l'heure et le nom du thread sont ajoutes automatiquement.
 */
@FunctionalInterface
public interface Journal {

    void afficher(String message);
}
