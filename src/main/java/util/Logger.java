package util;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Eine Singleton-Klasse zum Logging von Fehlern in eine Datei.
 */
public class Logger {

    /**
     * Einzige Instanz
     */
    private static Logger singleton = new Logger();

    /**
     * Dateiname für die logging.
     */
    private static final String dateiName = "log.txt";

    /**
     * Zum Schreiben von Meldungen in eine Datei
     */
    private static FileOutputStream writer;

    /**
     * Privater Konstruktor. Ist nicht von außen zugreifbar
     */
    private Logger() {
        try {
            // Initialisiert eine BufferedWriter Instanz
            writer = new FileOutputStream(dateiName, true);
            // append true = Wenn die Datei bereits vorhanden ist sollte den Inhalt nicht gelöscht
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Zum Loggen von Fehlern
     * @param meldung die Fehlermeldung
     */
    public static void logFehler(String meldung) {
        if (writer == null) {
            singleton = new Logger(); // Konstruktor wieder aufrufen..
        }

        try {
            byte[] bytes = meldung.getBytes();

            writer.write(bytes);
            writer.write(System.lineSeparator().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            logFehler(e.getMessage());
        }
    }
}
