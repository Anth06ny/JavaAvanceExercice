package fr.formation.profiling;

import fr.formation.data.Weather;
import fr.formation.data.WeatherApiDataSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Un traitement de commandes meteo qui tourne en continu.
 * A lancer avec les options de VM : -Xmx64m -XX:+HeapDumpOnOutOfMemoryError
 */
public class FuiteMemoire {

    private static final List<String> VILLES = List.of(
            "Niort", "Nantes", "Lyon", "Brest", "Toulouse", "Marseille");

    // On garde l'historique pour le rapport final
    private static final List<Trace> HISTORIQUE = new ArrayList<>();

    /** Ce qu'on retient d'une commande traitee. */
    record Trace(long numero, LocalDateTime date, String ville, String detail) {
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("FuiteMemoire en marche, pid = " + ProcessHandle.current().pid());
        System.out.println("Dossier de travail : " + System.getProperty("user.dir"));

        long numero = 0;
        while (true) {
            // Les commandes arrivent par lots de 50, dix fois par seconde
            for (int i = 0; i < 50; i++) {
                numero++;
                traiter(numero, VILLES.get((int) (numero % VILLES.size())));
            }

            if (numero % 5_000 == 0) {
                System.out.println(numero + " commandes traitées");
            }
            Thread.sleep(100);
        }
    }

    private static void traiter(long numero, String ville) {
        Weather meteo = WeatherApiDataSource.getFakeWeather(ville);   // immediat, sans attente

        String detail = """
                Bon de livraison de la commande CMD-%d
                Destination : %s
                Météo au moment de la livraison : %s
                Contrôle qualité effectué, colis remis en main propre, signature enregistrée.
                """.formatted(numero, ville, meteo).repeat(4);   // un exemplaire par service

        HISTORIQUE.add(new Trace(numero, LocalDateTime.now(), ville, detail));
    }
}
