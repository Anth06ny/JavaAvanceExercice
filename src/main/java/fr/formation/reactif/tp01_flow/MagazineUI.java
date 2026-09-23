package fr.formation.reactif.tp01_flow;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.SubmissionPublisher;

/**
 * TP 1 Flow - La fenetre est deja ecrite
 * <p>
 * Le code des boutons tourne dans un thread virtuel nomme "clic" (voir enFond) :
 * il peut appeler le lecteur et l'editeur sans bloquer la fenetre.
 */
public class MagazineUI extends JFrame {

    private static final DateTimeFormatter HEURE = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static final List<String> TITRES = List.of(
            "Spécial Java 25", "Les threads virtuels", "Tout sur les verrous",
            "CompletableFuture en pratique", "Le réactif sans douleur", "Flow démystifié");

    private JButton jbNouvelEditeur;
    private JButton jbPublier;
    private JButton jbPublier300;
    private JButton jbFermer;
    private JButton jbFaillite;
    private JTextArea jtaEditeur;

    private JButton jbSAbonner;
    private JButton jbDemander1;
    private JButton jbDemander3;
    private JButton jbResilier;
    private JTextArea jtaLecteur;

    /** Affiche dans la zone de l'editeur, depuis n'importe quel thread. */
    private final Journal journalEditeur = message -> ajouterAuJournal(jtaEditeur, message);
    /** Affiche dans la zone du lecteur, depuis n'importe quel thread. */
    private final Journal journalLecteur = message -> ajouterAuJournal(jtaLecteur, message);

    // volatile : lus et modifies par les threads "clic" successifs

    /** L'editeur, cree par le bouton Nouvel editeur. */
    private volatile SubmissionPublisher<Numero> editeurJdk;

    /** L'unique lecteur, cree par le bouton S'abonner. */
    private volatile Lecteur lecteur;

    private int prochainNumero = 1;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MagazineUI::new);
    }

    public MagazineUI() {
        //Appel du constructeur parent
        super("Mon abonnement à un magazine");

        //construct components : editeur
        jbNouvelEditeur = new JButton("Nouvel éditeur");
        jbPublier = new JButton("Publier un numéro");
        jbPublier300 = new JButton("Publier 300 numéros");
        jbFermer = new JButton("Fermer");
        jbFaillite = new JButton("Faillite");
        jtaEditeur = creerZoneAffichage();

        //construct components : lecteur
        jbSAbonner = new JButton("S'abonner");
        jbDemander1 = new JButton("Demander 1");
        jbDemander3 = new JButton("Demander 3");
        jbResilier = new JButton("Résilier");
        jtaLecteur = creerZoneAffichage();

        //Conteneurs : une colonne pour l'editeur, une pour le lecteur
        JPanel colonneEditeur = creerColonne("Éditeur", jtaEditeur,
                ligne(jbNouvelEditeur),
                ligne(jbPublier, jbPublier300, jbFermer, jbFaillite));
        JPanel colonneLecteur = creerColonne("Lecteur", jtaLecteur,
                ligne(jbSAbonner, jbDemander1, jbDemander3, jbResilier));

        JPanel colonnes = new JPanel(new GridLayout(1, 2, 10, 0));
        colonnes.add(colonneEditeur);
        colonnes.add(colonneLecteur);

        JButton jbEffacer = new JButton("Effacer les affichages");
        jbEffacer.addActionListener(e -> {
            jtaEditeur.setText("");
            jtaLecteur.setText("");
        });

        //Travail Sur la fenêtre
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(colonnes, BorderLayout.CENTER);
        getContentPane().add(jbEffacer, BorderLayout.SOUTH);
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        brancherBoutonsEditeur();
        brancherBoutonsLecteur();
    }

    // =========================================================================
    // Boutons de l'editeur
    // =========================================================================

    private void brancherBoutonsEditeur() {

        jbNouvelEditeur.addActionListener(e -> enFond(() -> {
            lecteur = null;
            editeurJdk = new SubmissionPublisher<>();
            journalEditeur.afficher("Nouvel éditeur");
        }));

        jbPublier.addActionListener(e -> {
            Numero numero = new Numero(prochainNumero, TITRES.get((prochainNumero - 1) % TITRES.size()));
            prochainNumero++;
            enFond(() -> {
                if (editeurJdk == null) {
                    journalEditeur.afficher("Pas d'éditeur : cliquer sur Nouvel éditeur");
                    return;
                }
                journalEditeur.afficher("Je publie " + numero);
                // TODO

            });
        });

        jbPublier300.addActionListener(e -> {
            // TODO
        });

        jbFermer.addActionListener(e -> enFond(() -> {
            if (editeurJdk == null) {
                journalEditeur.afficher("Pas d'éditeur : cliquer sur Nouvel éditeur");
                return;
            }
            journalEditeur.afficher("J'arrête le magazine");
            // TODO
        }));

        jbFaillite.addActionListener(e -> enFond(() -> {
            if (editeurJdk == null) {
                journalEditeur.afficher("Pas d'éditeur : cliquer sur Nouvel éditeur");
                return;
            }
            journalEditeur.afficher("Je fais faillite");
            // TODO

        }));
    }

    // =========================================================================
    // Boutons du lecteur
    // =========================================================================

    private void brancherBoutonsLecteur() {

        jbSAbonner.addActionListener(e -> {
            enFond(() -> {
                if (editeurJdk == null) {
                    journalLecteur.afficher("Pas d'éditeur : cliquer sur Nouvel éditeur");
                    return;
                }
                if (lecteur != null) {
                    journalLecteur.afficher("Déjà abonné : cliquer sur Nouvel éditeur pour recommencer");
                    return;
                }
                // TODO

            });
        });

        jbDemander1.addActionListener(e -> enFond(() -> {
            if (lecteur == null) {
                journalLecteur.afficher("Pas abonné : cliquer sur S'abonner");
                return;
            }
            // TODO

        }));

        jbDemander3.addActionListener(e -> enFond(() -> {
            if (lecteur == null) {
                journalLecteur.afficher("Pas abonné : cliquer sur S'abonner");
                return;
            }
            // TODO

        }));

        jbResilier.addActionListener(e -> enFond(() -> {
            if (lecteur == null) {
                journalLecteur.afficher("Pas abonné : cliquer sur S'abonner");
                return;
            }
            // TODO
        }));

    }

    // =========================================================================
    // Outils, deja ecrits
    // =========================================================================

    /** Lance l'action dans un thread virtuel nomme "clic", pour ne jamais bloquer la fenetre. */
    private void enFond(Runnable action) {
        Thread.ofVirtual().name("VirtualThread clic").start(() -> {
            try {
                action.run();
            } catch (Exception ex) {
                journalLecteur.afficher("Exception : " + ex);
                ex.printStackTrace();
            }
        });
    }

    /** Ajoute une ligne a la zone. L'heure et le thread sont lus ici, dans le thread appelant. */
    private static void ajouterAuJournal(JTextArea zone, String message) {
        String ligne = "%s [%s] %s%n".formatted(LocalTime.now().format(HEURE), Thread.currentThread().getName(), message);
        SwingUtilities.invokeLater(() -> {
            zone.append(ligne);
            zone.setCaretPosition(zone.getDocument().getLength());
        });
    }

    private static JTextArea creerZoneAffichage() {
        JTextArea zone = new JTextArea();
        zone.setEditable(false);
        zone.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        return zone;
    }

    private static JPanel ligne(JComponent... composants) {
        JPanel ligne = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JComponent composant : composants) {
            ligne.add(composant);
        }
        return ligne;
    }

    private static JPanel creerColonne(String titre, JTextArea zone, JPanel... lignes) {
        JPanel boutons = new JPanel(new GridLayout(lignes.length, 1));
        for (JPanel ligne : lignes) {
            boutons.add(ligne);
        }

        JPanel colonne = new JPanel(new BorderLayout());
        colonne.setBorder(BorderFactory.createTitledBorder(titre));
        colonne.add(boutons, BorderLayout.NORTH);
        colonne.add(new JScrollPane(zone), BorderLayout.CENTER);
        return colonne;
    }
}
