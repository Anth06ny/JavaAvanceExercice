package fr.formation.aop;

/**
 * TP 3 (Journalisation et supervision) - INTERCEPTER SANS TOUCHER AU CODE
 * <p>
 * Point de depart : le service est utilise directement, sans interception.
 * Les 3 appels ci-dessous ne doivent pas etre modifies, c'est la ligne de creation
 * du service qui change.
 * <p>
 * A FAIRE (etape 2 du TP) :
 * <ol>
 *     <li>Creer un proxy du service avec Proxy.newProxyInstance et un InvocationHandler</li>
 *     <li>Journaliser le nom de la methode et ses arguments avant l'appel (DEBUG)</li>
 *     <li>Journaliser le nom de la methode et sa duree en ms apres l'appel (DEBUG)</li>
 *     <li>Utiliser le proxy a la place du service, sans toucher a MeteoServiceImpl</li>
 * </ol>
 */
public class Tp3Proxy {

    public static void main(String[] args) {

        System.out.println("--- Création du service");

        //TODO : remplacer l'objet reel par un proxy qui l'enveloppe et utilisé le proxy
        MeteoService service = new MeteoServiceImpl();

        System.out.println("--- Appel simple");
        System.out.println(service.meteo("Niort"));

        System.out.println("--- Appel qui en declenche d'autres en interne");
        System.out.println(service.meteoDeuxVilles("Niort", "Nantes"));



    }
}
