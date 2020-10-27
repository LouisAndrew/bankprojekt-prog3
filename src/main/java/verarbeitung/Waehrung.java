package verarbeitung;

/**
 * Aufzählung aller im Bankprogramm verfügbaren Währungen
 */
public enum Waehrung {
    EUR(1), BGN(1.95583), LTL(3.4528), KM(1.95583);

    private final double umrechnungKurs;

    /**
     * Konstruktor der Enum. 
     * @param umrechnungKurs beschreibt, wie die Umrechnungkurs einer Währung, wenn in Euro umgerechnet wird.
     */
    Waehrung(double umrechnungKurs) {
        this.umrechnungKurs = umrechnungKurs;
    }

    /**
     * in Euro angegebenen Betrag in andere Währung umrechnen. (Hängt von dieser Währung ab)
     * @param betrag in Euro angegebener Betrag
     * @return In "this" Währung umgerechnete Betrag.
     */
    public double euroInWaehrungUmrechnen(double betrag) {
        return this.umrechnungKurs * betrag;   
    }

    /**
     * In andere Waehrung (Hängt von this Währung) angegebener Betrag in Euro umrechnen
     * @param betrag In "this" Waehrung angegebener Betrag
     * @return In Euro umgerechnete Betrag
     */
    public double waehrungInEuroUmrechnen(double betrag) {
        return betrag / this.umrechnungKurs;
    }

    /**
	 * Hilfsmethode zur Berechnung eines Betrags, wenn der Betrag in eine 
	 * andere Währung (anders als "this" Währung) gerechnet werden muss.
	 * 
	 * @param betrag zu berechnende Betrag
	 * @param w Währung, in welcher der Betrag gercehnet soll
	 * @throws IllegalArgumentException wenn der betrag negativ ist 
	 * @return der Betrag, in der angegebenen Währung
	 */
	public double rechneBetragAndererWaehrung(double betrag, Waehrung w) {
		if (betrag < 0 || Double.isNaN(betrag)) {
			throw new IllegalArgumentException("Falscher Betrag");
		}

		if (w == this) {
			return betrag;
		}

		double umgerechnet;

		if (w == EUR) {
			// einfach der Betrag in Euro umrechnen
			umgerechnet = euroInWaehrungUmrechnen(betrag);
		} else if (this == EUR) {
			// der Betrag (in Euro) in eine andere Währung umrechnen
			umgerechnet = w.waehrungInEuroUmrechnen(betrag);
		} else {
			// erstmal den Betrag in Euro umrechnen
			double temp = waehrungInEuroUmrechnen(betrag); // aktueller Betrag in "this" Währung

			// dann in andere Währung (w)
			umgerechnet = w.euroInWaehrungUmrechnen(temp);
		}

		return umgerechnet;
	}
}