package fabriken;

import verarbeitung.Konto;
import verarbeitung.Kunde;

public abstract class Kontofabrik {
    /**
     * Erstellt ein Konto.
     * @param inhaber Inhaber des Kontos
     * @param kontoNummer Kontonummer
     * @return das erstellte Konto.
     * @throws IllegalArgumentException
     */
    public abstract Konto erstellen(Kunde inhaber, long kontoNummer);
}
