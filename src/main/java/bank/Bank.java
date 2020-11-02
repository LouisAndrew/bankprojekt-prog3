package bank;

import verarbeitung.*;

import java.util.*;

public class Bank {

    private final long bankleitzahl;
    private long letztVergebeneNummer;

    private Map<Long, Konto> kontoliste;

    /**
     * erstellt eine Bank mit der angegebenen Bankleitzahl
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
     * erstellt ein Girokonto für den angegebenen Kunden. Dabei soll die Methode eine
     * neue, noch nicht vergebene Kontonummer erzeugen, das neue Girokonto mit dieser
     * Nummer in der Kontenliste speichern und die vergebene Kontonummer zurückgeben.
     *
     * @param inhaber Kunde: aus der verarbeitung Paket.
     * @return long: Kontonummer
     * @throws IllegalArgumentException wenn der angegebene Inhaber null ist.
     */
    public long girokontoErstellen(Kunde inhaber) {
        if (inhaber == null) {
            throw new IllegalArgumentException();
        }

        letztVergebeneNummer += 1; // Markiert die letzt vergebene Numemr -> ist die Kontonummer
        long kontoNummer = letztVergebeneNummer;
        final double DEFAULT_DISPO = 20; // default dispo -> wie muss man mit Dispo eingehen? Muss man etwa eine default-dispo verfügen oder einfach die Dispo am Anfang auf 0 setzen?

        Konto k = new Girokonto(inhaber, kontoNummer, DEFAULT_DISPO);

        kontoliste.put(kontoNummer, k); // trägt das Konto in die Kontoliste ein

        return kontoNummer;
    }

    /**
     * erstellt ein Sparbuch für den angegebenen Kunden. Dabei soll die Methode eine
     * neue, noch nicht vergebene Kontonummer erzeugen, das neue Sparbuch mit dieser
     * Nummer in der Kontenliste speichern und die vergebene Kontonummer zurückgeben.
     *
     * @param inhaber Kunde: aus der verarbeitung Paket.jac
     * @return long: Kontonummer
     * @throws IllegalArgumentException wenn der angegebene Inhaber null ist.
     */
    public long sparbuchErstellen(Kunde inhaber) {
        if (inhaber == null) {
            throw new IllegalArgumentException();
        }

        letztVergebeneNummer += 1; // Markiert die letzt vergebene Numemr -> ist die Kontonummer
        long kontoNummer = letztVergebeneNummer;

        Konto k = new Sparbuch(inhaber, kontoNummer);

        kontoliste.put(kontoNummer, k);

        return kontoNummer;
    }

    /**
     * liefert eine Auflistung von Kontoinformationen aller Konten (mindestens Kontonummer und Kontostand)
     *
     * @return String
     */
    public String getAlleKonten() {
        String alleKonten = "";

        for (Konto k: kontoliste.values()) {
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
     * @param von Nummer des Kontos, wovon der Betrag abgehoben wird
     * @param betrag der zu abhebende Betrag
     * @return true: wenn die Abhebung geklappt.
     * @throws KontoNichtExistiertException wenn die angegebene Kontonummer in der Kontoliste nicht enthalten ist.
     */
    public boolean geldAbheben(long von, double betrag) throws KontoNichtExistiertException {
        if (!kontoliste.containsKey(von)) {
            throw new KontoNichtExistiertException();
        }

        Konto k = kontoliste.get(von);
        try {
            return k.abheben(betrag);
        } catch (GesperrtException e) {
            // wie kann man mit diesem Fehler umgehen?
            System.out.println("Konto ist gesperrt.");
        } catch (IllegalArgumentException e) {
            System.out.println("Angegebener Betrag kann nicht negativ sein");
        }

        return false;
    }

    /**
     * zahlt den angegebenen Betrag auf das Konto mit der Nummer auf ein
     *
     * @param auf Nummer des Kontos, worauf der Betrag eingezahlt wird.
     * @param betrag der zu einzahlende Betrag
     * @throws KontoNichtExistiertException wenn die angegebene Kontonummer in der Kontoliste nicht enthalten ist.
     */
    public void geldEinzahlen(long auf, double betrag) throws KontoNichtExistiertException {
        if (!kontoliste.containsKey(auf)) {
            throw new KontoNichtExistiertException();
        }

        Konto k = kontoliste.get(auf);
        try {
            k.einzahlen(betrag);
        } catch (IllegalArgumentException e) {
            System.out.println("Angegebene Betrag kann nicht negativ sein. \nEinzahlung ist nicht erfolgreich.");
        }
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
            throw new KontoNichtExistiertException();
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
     * @param vonKontonr Nummer des Kontos, das die Überweisung absendet
     * @param nachKontonr Nummer des Kontos, das die Überweisung empfängt
     * @param betrag der zu überweisende Betrag
     * @param verwendungszweck Verwendungszweck der Überweisung
     * @return true, wenn die Überweisung geklappt hat
     * @throws KontoNichtExistiertException wenn die angegebene Kontonummer in der Kontoliste nicht enthalten ist.
     */
    public boolean geldUeberweisen(long vonKontonr, long nachKontonr, double betrag, String verwendungszweck) throws KontoNichtExistiertException {

        if (!kontoliste.containsKey(vonKontonr)) {
            System.out.println("Überweisungabsender nicht existiert");
            throw new KontoNichtExistiertException();
        }

        if (!kontoliste.containsKey(nachKontonr)) {
            System.out.println("Überweisungempfänger nicht existiert");
            throw new KontoNichtExistiertException();
        }

        // Überweisung zum überweisungsfähigen Konto!
        Konto absender = kontoliste.get(vonKontonr);
        Konto empfaenger = kontoliste.get(nachKontonr);
        boolean abgesendet;

        // Absender ist Überweisungsunfähig
        if (!(absender instanceof  Ueberweisungsfaehig)) {
            System.err.println("Absender ist Überweisungsunfähig!");
            return false;
        }

        if (empfaenger instanceof  Ueberweisungsfaehig) {
            System.err.println("Empfänger ist Überweisungsfähig!");
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
            return false;
        }


        if (abgesendet) {
            // Empfänger sollte hier Geld empfangen.
            geldEinzahlen(empfaenger.getKontonummer(), betrag); // Kontonummer des Empfängers sollte existiert.

            return true;
        } else {
            // wenn abgesendet false ist, heißt es, dass die Überweisung nicht erfolgreich ist. Dann, Empfänger sollte kein Geld empfangen.
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
}
