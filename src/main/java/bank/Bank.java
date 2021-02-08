package bank;

import fabriken.Kontofabrik;
import util.Logger;
import verarbeitung.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Bank implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L; // Aus Musterloesung.

    private final long bankleitzahl;
    private long letztVergebeneNummer;

    private Map<Long, Konto> kontoliste;

    private final String msgAbsenderNichtExist = "Das Konto des Absenders existiert nicht";
    private final String msgEmpfaengerNichtExist = "Das Konto des Empfängers existiert nicht";
    private final String msgKontoNichtExist = "Das Konto existiert nicht";

    /**
     * erstellt eine Bank mit der angegebenen Bankleitzahl
     *
     * @param bankleitzahl long
     * @throws IllegalArgumentException wenn die angegebene BLZ negativ
     */
    public Bank(long bankleitzahl) {

        if (bankleitzahl < 0) {
            throw new IllegalArgumentException("Bankleitzahl kann nicht negativ sein!");
        }

        this.bankleitzahl = bankleitzahl;
        this.kontoliste = new TreeMap<>();
        this.letztVergebeneNummer = 0;
    }

    /**
     * Methode zur Erstellung eines Kontos. Sowohl Girokonto als auch Sparbuch wird beim Aufruf dieser Methode erstellt.
     * @param fabrik Abstract-Factory-Muster zur Erstellung eines Kontos.
     * @param inhaber Inhaber des Kontos
     * @return die Kontonummer.
     */
    public long kontoErstellen(Kontofabrik fabrik, Kunde inhaber) {
        if (inhaber == null || fabrik == null) {
            throw new IllegalArgumentException();
        }

        letztVergebeneNummer += 1; // Markiert die letzt vergebene Numemr -> ist die Kontonummer
        long kontoNummer = letztVergebeneNummer;

        Konto k = fabrik.erstellen(inhaber, kontoNummer); // Ein Konto aus der Fabrik erstellen

        kontoliste.put(kontoNummer, k); // Dann setze das Konto in die Kontoliste

        // Wenn ein Konto von der Bank erstellt wird, wird dann automatisch die Observern angemeldet.

        return kontoNummer;
    }

    /**
     * liefert eine Auflistung von Kontoinformationen aller Konten (mindestens Kontonummer und Kontostand)
     *
     * @return String
     */
    public String getAlleKonten() {
        String alleKonten = "";

        for (Konto k : kontoliste.values()) {
            alleKonten = "Kontonummer: " + k.getKontonummer() + ". Kontostand: " + k.getKontostandFormatiert() + "\n";
        }

        return alleKonten;
    }

    /**
     * liefert eine Liste aller gültigen Kontonummern in der Bank
     *
     * @return List<Long>
     */
    public List<Long> getAlleKontonummern() {
        return new ArrayList<>(kontoliste.keySet());
    }

    /**
     * hebt den Betrag vom Konto mit der Nummer von ab und gibt zurück, ob die Abhebung geklappt hat.
     *
     * @param von    Nummer des Kontos, wovon der Betrag abgehoben wird
     * @param betrag der zu abhebende Betrag
     * @return true: wenn die Abhebung geklappt.
     * @throws KontoNichtExistiertException wenn die angegebene Kontonummer in der Kontoliste nicht enthalten ist.
     */
    public boolean geldAbheben(long von, double betrag) throws KontoNichtExistiertException {
        if (!kontoliste.containsKey(von)) {
            throw new KontoNichtExistiertException(msgKontoNichtExist);
        }

        Konto k = kontoliste.get(von);
        try {
            return k.abheben(betrag);
        } catch (GesperrtException | IllegalArgumentException e) {
            // wie kann man mit diesem Fehler umgehen?
            Logger.logFehler("Konto " + von + " ist gesperrt und möchte eine Abhebung durchfüren");
            return false;
        }
    }

    /**
     * zahlt den angegebenen Betrag auf das Konto mit der Nummer auf ein
     *
     * @param auf    Nummer des Kontos, worauf der Betrag eingezahlt wird.
     * @param betrag der zu einzahlende Betrag
     * @throws KontoNichtExistiertException wenn die angegebene Kontonummer in der Kontoliste nicht enthalten ist.
     */
    public void geldEinzahlen(long auf, double betrag) throws KontoNichtExistiertException, IllegalArgumentException {
        if (!kontoliste.containsKey(auf)) {
            throw new KontoNichtExistiertException(msgKontoNichtExist);
        }

        Konto k = kontoliste.get(auf);
        k.einzahlen(betrag);
    }

    /**
     * löscht das Konto mit der angegebenen nummer und gibt zurück, ob die Löschung geklappt hat (Kontonummer nicht vorhanden)
     *
     * @param nummer Nummer des Kontos, das gelöscht wird
     * @return true, wenn das Loeschen geklappt hat.
     */
    public boolean kontoLoeschen(long nummer) {
        if (!kontoliste.containsKey(nummer)) {
            return false;
        }

        kontoliste.remove(nummer);
        return true;
    }

    /**
     * liefert den Kontostand des Kontos mit der angegebenen nummer zurück.
     *
     * @param nummer Nummer des Kontos, dessen Kontostand zurückgeliefert wird.
     * @return der Kontostand
     * @throws KontoNichtExistiertException wenn die angegebene Kontonummer in der Kontoliste nicht enthalten ist.
     */
    public double getKontostand(long nummer) throws KontoNichtExistiertException {
        if (!kontoliste.containsKey(nummer)) {
            throw new KontoNichtExistiertException(msgKontoNichtExist);
        }

        Konto k = kontoliste.get(nummer);
        return k.getKontostand();
    }

    /**
     * überweist (das ist etwas anderes als Geld abheben und wieder einzahlen…) den genannten Betrag vom überweisungsfähigen Konto mit der Nummer vonKontonr zum
     * überweisungsfähigen Konto mit der Nummer nachKontonr und gibt zurück, ob die
     * Überweisung geklappt hat (nur bankinterne Überweisungen!) Überlegen Sie gut, was
     * dabei alles schief gehen kann, so dass Sie false zurückliefern müssen!
     *
     * @param vonKontonr       Nummer des Kontos, das die Überweisung absendet
     * @param nachKontonr      Nummer des Kontos, das die Überweisung empfängt
     * @param betrag           der zu überweisende Betrag
     * @param verwendungszweck Verwendungszweck der Überweisung
     * @return true, wenn die Überweisung geklappt hat
     * @throws KontoNichtExistiertException wenn die angegebene Kontonummer in der Kontoliste nicht enthalten ist.
     */
    public boolean geldUeberweisen(long vonKontonr, long nachKontonr, double betrag, String verwendungszweck) throws KontoNichtExistiertException {

        if (!kontoliste.containsKey(vonKontonr)) {
            throw new KontoNichtExistiertException(msgAbsenderNichtExist);
        }

        if (!kontoliste.containsKey(nachKontonr)) {
            throw new KontoNichtExistiertException(msgEmpfaengerNichtExist);
        }

        // Überweisung zum überweisungsfähigen Konto!
        Konto absender = kontoliste.get(vonKontonr);
        Konto empfaenger = kontoliste.get(nachKontonr);
        boolean abgesendet;

        // Die beiden Konten müssen überweisungsfähig sein.
        if (!(empfaenger instanceof Ueberweisungsfaehig) || !(absender instanceof Ueberweisungsfaehig)) {
            return false;
        }

        // wenn Empfänger oder Absender gesperrt ist, muss die Überweisung nicht genommen.
        if (empfaenger.isGesperrt() || absender.isGesperrt()) {
            return false;
        }

        // erstmal versuchen, die Überweisung abzusenden
        try {
            abgesendet = ((Ueberweisungsfaehig) absender).ueberweisungAbsenden(betrag, empfaenger.getInhaber().getName(), empfaenger.getKontonummer(), this.bankleitzahl, verwendungszweck);
        } catch (GesperrtException | IllegalArgumentException e) {
            Logger.logFehler("Konto " + vonKontonr + " ist gesperrt + ueberweisung ");
            return false;
        }

        // Empfaenger sollte hier die Ueberweisung empfangen.
        if (abgesendet) {
            try {
                ((Ueberweisungsfaehig) empfaenger).ueberweisungEmpfangen(betrag, absender.getInhaber().getName(), absender.getKontonummer(), this.bankleitzahl, verwendungszweck);
                return true;
            } catch (IllegalArgumentException e) {
                // wenn das Empfaengen der Ueberweisung nicht geklappt, zahle den Betrag ins Konto des Absenders wieder ein.
                geldEinzahlen(absender.getKontonummer(), betrag);
                Logger.logFehler("Absender " + nachKontonr + " ist gesperrt. Geld in Hoehe von " + betrag + " ist zurueck zu " + vonKontonr);
                return false;
            }
        } else {
            // wenn die Ueberweisung nicht geklappt hat, return false.
            return false;
        }
    }

    /**
     * liefert die Bankleitzahl zurück
     *
     * @return long
     */
    public long getBankleitzahl() {
        return bankleitzahl;
    }

    /**
     * die Methode sperrt alle Konten, deren Kontostand im Minus ist.
     */
    public void pleitegeierSperren() {
        Stream<Konto> kontenStream = kontoliste.values().stream();
        kontenStream.forEach(konto -> {
            // prueft ob der aktuelle Kontostand negativ ist
            if (konto.getKontostand() < 0) {
                konto.sperren(); // sperrt das Konto wenn der Kontostand negativ ist.
            }
        });
    }

    /**
     * Die Methode liefert eine Liste aller Kunden, die auf einem Konto einen Kontostand haben, der mindestens minimum beträgt.
     *
     * @param minimum Minimumbetrag.
     * @return Liste aller Kunden, den Kontostand hoeher als Minimum hat.
     */
    public List<Kunde> getKundenMitVollemKonto(double minimum) {
        Stream<Konto> kontenStream = kontoliste.values().stream();
        return kontenStream
                .filter(konto -> konto.getKontostand() >= minimum) // den Stream filtern, dass nur Konten mit Kontostand gleicher gleich den minimum Betrag in den Stream beinhaltet wird
                .map(konto -> konto.getInhaber()) // nimmt den Ihaber aller Konten im Stream heraus
                .collect(Collectors.toList()); // den Strem in Liste umwandeln
    }

    /**
     * liefert die Namen und Geburtstage aller Kunden der Bank. Doppelte Namen sollen dabei aussortiert werden. Sortieren Sie die Liste nach dem Geburtsdatum.
     *
     * @return Namen und geburtstage aller Kunden.
     */
    public String getKundengeburtstage() {

        Stream<Kunde> kontenStream = kontoliste
                .values()
                .stream()
                .map(konto -> konto.getInhaber()) // nimmt Ihaber aller Konten heraus
                .distinct() // doppelte namen aussortieren
                .sorted((kunde1, kunde2) -> kunde1.getName().compareTo(kunde2.getName())); // sortiert die inhaltee des streams

        StringBuilder builder = new StringBuilder();

        kontenStream.forEach(kunde -> {
            String str = "Name: " + kunde.getName() + " " + kunde.getNachname() + ". Geburtstag: " + kunde.getGeburtstag().toString() + System.lineSeparator();

            builder.append(str);
        });

        return builder.toString();
    }

    /**
     * liefert eine Liste aller freien Kontonummern, die im von Ihnen vergebenen Bereich
     * liegen (sicher gibt es in Ihrem Programm eine Untergrenze für Kontonummern
     * und eine derzeitige Obergrenze; es geht um die Kontonummern, die dazwischen
     * liegen und für die es gerade kein Konto gibt, z.B. weil es gelöscht wurde.)
     *
     * @return Liste der Kontonummern, die in Lueceken stehen
     */
    public List<Long> getKontonummernLuecken() {
        if (kontoliste.size() == 0) {
            return new ArrayList<>();
        }

        return LongStream
                .rangeClosed(1, letztVergebeneNummer) // Erstmal eine Longstream im Intervall 1 bis letzt vergebener Nummer erzeugen
                .boxed() // Dann diesen Longstream in einen Stream<Long> umwandeln
                .filter(kontonummer -> !kontoliste.containsKey(kontonummer)) // den Stream filtern, dass nur kontonummern, die keinem Konto zugeordnet ist, beeihaltet werden
                .collect(Collectors.toList()); // den Stream in eine Liste umwandeln
    }

    /**
     * Liefert eine vollstaendige Kopie von this zurueck.
     *
     * @return vollstaendige Kopie von this
     */
    public Bank clone() throws CloneNotSupportedException {
        byte[] arr;
        try (
                ByteArrayOutputStream baout = new ByteArrayOutputStream();
                ObjectOutputStream oo = new ObjectOutputStream(baout);
                ) {
            oo.writeObject(this);
            arr = baout.toByteArray();
        } catch (NotSerializableException e) {
            throw new CloneNotSupportedException();
        } catch (IOException e) {
            return null;
        }

        try (
                ByteArrayInputStream bain = new ByteArrayInputStream(arr);
                ObjectInputStream oi = new ObjectInputStream(bain);
        ) {
            return (Bank) oi.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }
}
