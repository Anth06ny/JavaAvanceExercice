package fr.formation.reactif.tp01_flow;

import fr.formation.reactif.tp01_flow.myflow.Lecteur;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

/**
 * TP 1 Flow - La fenetre est deja ecrite : il reste a completer les TODO des boutons.
 * <p>
 * Le code des boutons tourne dans un thread virtuel nomme "clic" (voir enFond) :
 * il peut appeler le lecteur et l'editeur sans bloquer la fenetre.
 */
public class MagazineUI extends JFrame {

    private static final DateTimeFormatter HEURE = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static final List<String> TITRES = List.of(
            "Spécial Java 25", "Les threads virtuels", "Tout sur les verrous",
            "CompletableFuture en pratique", "Le réactif sans douleur", "Flow démystifié");

    /** Nombre de numeros publies par l'editeur maison. */
    private static final int NB_NUMEROS_MAISON = 5;

    private JRadioButton jrbJdk;
    private JRadioButton jrbMaison;
    private JButton jbNouvelEditeur;
    private JButton jbPublier;
    private JButton jbFermer;
    private JButton jbFaillite;
    private JTextArea jtaEditeur;

    private JButton jbSAbonner;
    private JButton jbDemander1;
    private JButton jbDemander3;
    private JButton jbResilier;
    private JCheckBox jcbLectureAuto;
    private JTextArea jtaLecteur;

    /** Affiche dans la zone de l'editeur, depuis n'importe quel thread. */
    private final Journal journalEditeur = message -> ajouterAuJournal(jtaEditeur, message);
    /** Affiche dans la zone du lecteur, depuis n'importe quel thread. */
    private final Journal journalLecteur = message -> ajouterAuJournal(jtaLecteur, message);

    // volatile : lus et modifies par les threads "clic" successifs

    /** L'editeur du JDK, quand il est choisi. */
    private volatile SubmissionPublisher<Numero> editeurJdk;
    /** L'editeur en cours : celui du JDK ou l'editeur maison. */
    private volatile Flow.Publisher<Numero> editeur;
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
        jrbJdk = new JRadioButton("Éditeur du JDK", true);
        jrbMaison = new JRadioButton("Éditeur maison");
        ButtonGroup groupe = new ButtonGroup();
        groupe.add(jrbJdk);
        groupe.add(jrbMaison);
        jbNouvelEditeur = new JButton("Nouvel éditeur");
        jbPublier = new JButton("Publier un numéro");
        jbFermer = new JButton("Fermer");
        jbFaillite = new JButton("Faillite");
        jtaEditeur = creerZoneAffichage();

        //construct components : lecteur
        jbSAbonner = new JButton("S'abonner");
        jbDemander1 = new JButton("Demander 1");
        jbDemander3 = new JButton("Demander 3");
        jbResilier = new JButton("Résilier");
        jcbLectureAuto = new JCheckBox("Redemander après chaque numéro");
        jtaLecteur = creerZoneAffichage();

        //Conteneurs : une colonne pour l'editeur, une pour le lecteur
        JPanel colonneEditeur = creerColonne("Éditeur",
                ligne(jrbJdk, jrbMaison, jbNouvelEditeur),
                ligne(jbPublier, jbFermer, jbFaillite),
                jtaEditeur);
        JPanel colonneLecteur = creerColonne("Lecteur",
                ligne(jbSAbonner, jbDemander1, jbDemander3, jbResilier),
                ligne(jcbLectureAuto),
                jtaLecteur);

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

        //Seul l'editeur du JDK se publie a la main : l'editeur maison publie a la demande
        jrbJdk.addActionListener(e -> activerBoutonsJdk(true));
        jrbMaison.addActionListener(e -> activerBoutonsJdk(false));

        brancherBoutonsEditeur();
        brancherBoutonsLecteur();
    }

    // =========================================================================
    // Boutons de l'editeur
    // =========================================================================

    private void brancherBoutonsEditeur() {

        jbNouvelEditeur.addActionListener(e -> {
            boolean jdk = jrbJdk.isSelected();
            enFond(() -> {
                lecteur = null;
                if (jdk) {
                    editeurJdk = new SubmissionPublisher<>();
                    editeur = editeurJdk;
                    journalEditeur.afficher("Nouvel éditeur du JDK");
                } else {
                    editeurJdk = null;
                    // TODO etape 3 : creer un Editeur maison de NB_NUMEROS_MAISON numeros et le ranger dans editeur

                    journalEditeur.afficher("Nouvel éditeur maison de " + NB_NUMEROS_MAISON + " numéros");
                }
            });
        });

        jbPublier.addActionListener(e -> {
            Numero numero = new Numero(prochainNumero, TITRES.get((prochainNumero - 1) % TITRES.size()));
            prochainNumero++;
            enFond(() -> {
                if (editeurJdk == null) {
                    journalEditeur.afficher("Pas d'éditeur du JDK : cliquer sur Nouvel éditeur");
                    return;
                }
                journalEditeur.afficher("Je publie " + numero);
                // TODO etape 2 : publier le numero avec editeurJdk

            });
        });

        jbFermer.addActionListener(e -> enFond(() -> {
            if (editeurJdk == null) {
                journalEditeur.afficher("Pas d'éditeur du JDK : cliquer sur Nouvel éditeur");
                return;
            }
            journalEditeur.afficher("J'arrête le magazine");
            // TODO etape 2 : fermer editeurJdk normalement
        }));

        jbFaillite.addActionListener(e -> enFond(() -> {
            if (editeurJdk == null) {
                journalEditeur.afficher("Pas d'éditeur du JDK : cliquer sur Nouvel éditeur");
                return;
            }
            journalEditeur.afficher("Je fais faillite");
            // TODO etape 2 : fermer editeurJdk en erreur, avec new RuntimeException("Faillite")

        }));
    }

    // =========================================================================
    // Boutons du lecteur
    // =========================================================================

    private void brancherBoutonsLecteur() {

        jbSAbonner.addActionListener(e -> {
            boolean lectureAuto = jcbLectureAuto.isSelected();
            enFond(() -> {
                if (editeur == null) {
                    journalLecteur.afficher("Pas d'éditeur : cliquer sur Nouvel éditeur");
                    return;
                }
                if (lecteur != null) {
                    journalLecteur.afficher("Déjà abonné : cliquer sur Nouvel éditeur pour recommencer");
                    return;
                }
                journalLecteur.afficher("Je m'abonne");
                // TODO etape 2 : creer un Lecteur avec journalLecteur, lui appliquer lectureAuto,
                //  le ranger dans l'attribut lecteur, puis l'abonner a l'editeur

            });
        });

        jbDemander1.addActionListener(e -> enFond(() -> {
            if (lecteur == null) {
                journalLecteur.afficher("Pas abonné : cliquer sur S'abonner");
                return;
            }
            // TODO etape 2 : le lecteur demande 1 numero

        }));

        jbDemander3.addActionListener(e -> enFond(() -> {
            if (lecteur == null) {
                journalLecteur.afficher("Pas abonné : cliquer sur S'abonner");
                return;
            }
            // TODO etape 2 : le lecteur demande 3 numeros

        }));

        jbResilier.addActionListener(e -> enFond(() -> {
            if (lecteur == null) {
                journalLecteur.afficher("Pas abonné : cliquer sur S'abonner");
                return;
            }
            // TODO etape 2 : le lecteur resilie son abonnement

        }));

        jcbLectureAuto.addActionListener(e -> {
            boolean lectureAuto = jcbLectureAuto.isSelected();
            enFond(() -> {
                if (lecteur == null) {
                    return;
                }
                // TODO etape 2 : appliquer lectureAuto au lecteur

            });
        });
    }

    // =========================================================================
    // Outils, deja ecrits
    // =========================================================================

    /** Lance l'action dans un thread virtuel nomme "clic", pour ne jamais bloquer la fenetre. */
    private void enFond(Runnable action) {
        Thread.ofVirtual().name("clic").start(() -> {
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

    private static JPanel creerColonne(String titre, JPanel ligne1, JPanel ligne2, JTextArea zone) {
        JPanel boutons = new JPanel(new GridLayout(2, 1));
        boutons.add(ligne1);
        boutons.add(ligne2);

        JPanel colonne = new JPanel(new BorderLayout());
        colonne.setBorder(BorderFactory.createTitledBorder(titre));
        colonne.add(boutons, BorderLayout.NORTH);
        colonne.add(new JScrollPane(zone), BorderLayout.CENTER);
        return colonne;
    }

    private void activerBoutonsJdk(boolean actif) {
        jbPublier.setEnabled(actif);
        jbFermer.setEnabled(actif);
        jbFaillite.setEnabled(actif);
    }
}
