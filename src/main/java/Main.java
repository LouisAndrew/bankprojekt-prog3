import javafx.application.Application;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import util.Logger;
import verarbeitung.GesperrtException;
import verarbeitung.Girokonto;
import verarbeitung.Konto;

public class Main extends Application {

    private ReadOnlyStringWrapper meldungText = new ReadOnlyStringWrapper(""); // Meldung, ob etwas schief geht oder wenn eine Operation erfolgreich abgearbeitet
    private int statusCode = 0; // statusCode zur Bestimmung der Meldung.

    @FXML private Konto model;
//    @FXML private Kunde kunde;

    @FXML private Text ueberschrift;

    @FXML private GridPane anzeige;
    @FXML private Text txtNummer;
    /**
     * Anzeige der Kontonummer
     */
    @FXML private Text nummer;
    @FXML private Text txtStand;
    /**
     * Anzeige des Kontostandes
     */
    @FXML private Text stand;
    @FXML private Text txtGesperrt;
    /**
     * Anzeige und Änderung des Gesperrt-Zustandes
     */
    @FXML private CheckBox gesperrt;
    @FXML private Text txtAdresse;
    /**
     * Anzeige und Änderung der Adresse des Kontoinhabers
     */
    @FXML private TextArea adresse;
    /**
     * Anzeige von Meldungen über Kontoaktionen
     */
    @FXML private Text meldung;
    @FXML private HBox aktionen;
    /**
     * Auswahl des Betrags für eine Kontoaktion
     */
    @FXML private TextField betrag;
    /**
     * löst eine Einzahlung aus
     */
    @FXML private Button einzahlen;
    /**
     * löst eine Abhebung aus
     */
    @FXML private Button abheben;

    /**
     * Kontrollmethode zum Einzahlen von Betrag ins Konto k
     * @param betrag einzuzahlender Betrag
     * @param k Konto
     */
    public void einzahlen(double betrag, Konto k) {
        k.einzahlen(betrag);
        statusCode = 1;
        updateMeldung();
    }

    /**
     * Kontrollmethode zum Abheben von Betrag aus dem Konto k
     * @param betrag abzuhebender Betrag
     * @param k Konto
     */
    public void abheben(double betrag, Konto k) {
        try {
            boolean abgehoben = k.abheben(betrag);
            statusCode= abgehoben ? 2:3;
        } catch (GesperrtException e) {
            Logger.logFehler("Konto mit Nummer von " + k.getKontonummer() + " ist gesperrt, führt aber eine Abhebung durch ");
            statusCode = 4;
        } finally {
            updateMeldung();
        }
    }

    /**
     * Meldung immer aktualisieren, abhaenging vom Status Code
     */
     void updateMeldung() {
        switch (statusCode) {
            case 0: // Default
                this.meldungText.set("Willkomen lieber Benutzer");
                break;
            case 1: // Wenn einzahlung erfolgreich ist
                this.meldungText.set("Einzahlung erfolgreich!");
                break;
            case 2: // Wenn Abheben erfolgreich
                this.meldungText.set("Abheben erfolgreich");
                break;
            case 3: // Wenn Abheben nicht geklappt (z.B Kontostand reicht nicht)
                this.meldungText.set("Abheben klappt nicht");
                break;
            case 4: // Wenn Abheben nicht geklappt, weil Konto gesperrt ist.
                this.meldungText.set("Konto ist gesperrt, Abheben klappt nicht");
                break;
            case 5: // wenn betrag nicht eine zahl ist
                this.meldungText.set("der betrag muss eine zahl sein");
                break;
            default:
                this.meldungText.set("Willkomen lieber Benutzer");
                break;
        }
    }

    /**
     * zur Fehlermeldung wenn Benutzer nicht eine zahl ins Betragfeld eingegeben
     */
    public void betragFehlerMeldung() {
        statusCode = 5;
        updateMeldung();
    }

    /**
     * Initialisierung. Wird automatisch nach der Objekterzeugung aufgerufen.
     */
    @FXML public void initialize() {

        model = new Girokonto();
        model.einzahlen(10); // Anfangs 10EUR

        nummer.textProperty().set(Long.toString(model.getKontonummer()));
        stand.textProperty().bind(model.kontostandProperty().asString());
        stand.fillProperty().bind(Bindings.createObjectBinding(
                                    () -> model.istKontostandNegativProperty().getValue()
                                            ? Color.RED : Color.GREEN
                                    , model.istKontostandNegativProperty()));
        gesperrt.selectedProperty().bindBidirectional(model.gesperrtProperty());
        adresse.textProperty().bindBidirectional(model.getInhaber().getAdresseProperty());

        updateMeldung(); // Erstmal, zeige Begrussung

        meldung.textProperty().bind(meldungText);
    }

    /**
     * Wird ausgefuehrt, wenn Einzahlen-Button ist geklickt
     */
    @FXML private void einzahlenOnClick() {
        try {
            double betragDouble = Double.parseDouble(betrag.getText());
            einzahlen(betragDouble, model);
        } catch (NumberFormatException ex) {
            betragFehlerMeldung();
            Logger.logFehler("Falsche Formattierung von Betrag eingegeben");
        }
    }

    /**
     * Wird ausgefuehrt, wenn Abheben-Button ist geklickt
     */
    @FXML private void abhebenOnClick() {
        try {
            double betragDouble = Double.parseDouble(betrag.getText());
            abheben(betragDouble, model);
        } catch (NumberFormatException ex) {
            betragFehlerMeldung();
            Logger.logFehler("Falsche Formattierung von Betrag eingegeben");
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Main.class.getResource("/KontoOberflaeche.fxml"));
//        Parent root = new KontoOberflaeche(model);
        Scene scene = new Scene(root, 400, 350);

        stage.setScene(scene);
        stage.setTitle("Kontooberflaeche");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
