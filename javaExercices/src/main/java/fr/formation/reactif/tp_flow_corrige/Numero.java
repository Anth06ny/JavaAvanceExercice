package fr.formation.reactif.tp_flow_corrige;

/**
 * TP 1 Flow - Un numero du magazine, deja ecrit.
 *
 * @param numero numero de parution
 * @param titre  titre du numero
 */
public record Numero(int numero, String titre) {

    @Override
    public String toString() {
        return "le n°%d : %s".formatted(numero, titre);
    }
}
