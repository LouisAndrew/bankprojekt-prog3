package verarbeitung;

import javafx.beans.property.*;
import verarbeitung.beobachter.InhaberBeobachter;
import verarbeitung.beobachter.IsGesperrtBeobachter;
import verarbeitung.beobachter.KontostandBeobachter;
import verarbeitung.beobachter.WaehrungBeobachter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * stellt ein allgemeines Konto dar
 */
public abstract class Konto implements Comparable<Konto>, Serializable
{
	/** 
	 * der Kontoinhaber
	 */
	private Kunde inhaber;

	/**
	 * die Kontonummer
	 */
	private final long nummer;

	private Waehrung waehrung = Waehrung.EUR;

	/**
	 * Observer-Support dieses Objekts
	 */
	private transient PropertyChangeSupport prop = new PropertyChangeSupport(this);

	/**
	 * der aktuelle Kontostand
	 */
	private ReadOnlyDoubleWrapper kontostand;

	/**
	 * Wenn das Konto gesperrt ist (gesperrt = true), können keine Aktionen daran mehr vorgenommen werden,
	 * die zum Schaden des Kontoinhabers wären (abheben, Inhaberwechsel)
	 */
	private BooleanProperty gesperrt;
	private ReadOnlyBooleanWrapper istKontostandNegativ;

	/**
	 * Set-Methode der PropertyChangeSupport. Wird hauptsaechlich zum Testen benutzt.
	 * @param prop PropertyChangeSupport.
	 */
	void setProp(PropertyChangeSupport prop) { this.prop = prop; }

	/**
	 * Gibt den Kontostand des Kontos als eine ReadOnlyDoubleProperty zurueck
	 * @return Nicht veraenderbare Kontostand als ein Property
	 */
	public ReadOnlyDoubleProperty kontostandProperty() {
		return kontostand.getReadOnlyProperty();
	}

	/**
	 * Gibt zurueck, ob das Konto gesperrt ist, als ein Property
	 * @return BooleanProperty
	 */
	public BooleanProperty gesperrtProperty() {
		return gesperrt;
	}

	/**
	 * Gibt zurueck, ob auf der Oberflaeche die Plus / Minus Icon angezeigt werden soll
	 * @return -, wenn minus angezeigt werden soll
	 */
	public ReadOnlyBooleanProperty istKontostandNegativProperty() {
		return istKontostandNegativ.getReadOnlyProperty();
	}

	/**
	 * Default Aufbau der Beobachter fuer die Klasse Konto..
	 */
	public void setupProp() {
		List<PropertyChangeListener> liste = new LinkedList<>();

		liste.add(new IsGesperrtBeobachter());
		liste.add(new KontostandBeobachter());
		liste.add(new WaehrungBeobachter());
		liste.add(new InhaberBeobachter());

		anmelden(liste);
	}

	/**
	 * setzt den aktuellen Kontostand
	 * @param kontostand neuer Kontostand
	 */
	protected void setKontostand(double kontostand) {
		prop.firePropertyChange("Kontostand", this.kontostand, kontostand);
		this.kontostand.set(kontostand);
		this.istKontostandNegativ.set(kontostand < 0);
	}

	/**
	 * Setzt die beiden Eigenschaften kontoinhaber und kontonummer auf die angegebenen Werte,
	 * der anfängliche Kontostand wird auf 0 gesetzt
	 * 
	 * Zu Beginn setzt die Währung immer auf Euro.
	 *
	 * @param inhaber der Inhaber
	 * @param kontonummer die gewünschte Kontonummer
	 * @throws IllegalArgumentException wenn der Inhaber null
	 */
	public Konto(Kunde inhaber, long kontonummer) {
		if(inhaber == null)
			throw new IllegalArgumentException("Inhaber darf nicht null sein!");
		this.inhaber = inhaber;
		this.nummer = kontonummer;

		kontostand = new ReadOnlyDoubleWrapper(0);
		gesperrt = new SimpleBooleanProperty(false);
		istKontostandNegativ = new ReadOnlyBooleanWrapper(false);

		setupProp();
	}

	/**
	 * Setzt alle Eigenschaften, indem den obigen Konstruktor aufruft.
	 * Dann, setzt die Währung auf die angegebene Währung.
	 * 
	 * @param inhaber der Inhaber
	 * @param kontonummer die gewünschte Kontonummer
	 * @param waehrung die erwünschte Währung, in welcher das Konto gefüht wird.
	 * @throws IllegalArgumentException wenn der Inhaber null
	 */
	public Konto(Kunde inhaber, long kontonummer, Waehrung waehrung) throws IllegalArgumentException {
		this(inhaber, kontonummer);
		this.waehrung = waehrung;
	}

	/**
	 * setzt alle Eigenschaften des Kontos auf Standardwerte
	 */
	public Konto() {
		this(Kunde.MUSTERMANN, 1234567);
	}

	/**
	 * Meldet einen ChangeListener an
	 * @param listener ChangeListener
	 */
	public void anmelden(PropertyChangeListener listener) {
		prop.addPropertyChangeListener(listener);
	}

	/**
	 * Meldet eine Liste von ChangeListeners an
	 * @param listenerList Liste von ChangeListener.
	 */
	public void anmelden(List<PropertyChangeListener> listenerList) {
		listenerList.forEach(listener -> { prop.addPropertyChangeListener(listener); });
	}

	/**
	 * Meldet einen ChangeListener ab
	 * @param listener ChangeListener.
	 */
	public void abmelden(PropertyChangeListener listener) {
		prop.removePropertyChangeListener(listener);
	}

	/**
	 * liefert den Kontoinhaber zurück
	 * @return   der Inhaber
	 */
	public Kunde getInhaber() {
		return this.inhaber;
	}

	/**
	 * liefert die Währung zurück
	 * protected => nur für Unterklasse Zugriff erlaubt.
	 * 
	 * @return die Währung
	 */
	protected Waehrung getWaehrung() {
		return this.waehrung;
	}

	/**
	 * setzt die aktuellste Währung
	 * protected => nur für Unterklasse Zugriff erlaubt.
	 */
	public void setWaehrung(Waehrung waehrung) {
		prop.firePropertyChange("Waehrung", this.waehrung, waehrung);
		this.waehrung = waehrung;
	}
	
	/**
	 * setzt den Kontoinhaber
	 * @param kinh   neuer Kontoinhaber
	 * @throws GesperrtException wenn das Konto gesperrt ist
	 * @throws IllegalArgumentException wenn kinh null ist
	 */
	public void setInhaber(Kunde kinh) throws GesperrtException{
		if (kinh == null)
			throw new IllegalArgumentException("Der Inhaber darf nicht null sein!");
		if(isGesperrt())
			throw new GesperrtException(this.nummer);

		prop.firePropertyChange("Inhaber", this.inhaber, kinh);
		this.inhaber = kinh;

	}

	/**
	 * Sie liefert die Währung zurück, in der das Konto aktuell geführt wird.
	 */
	public Waehrung getAktuelleWaehrung() {
		return this.waehrung;
	}
	
	/**
	 * Wechsel die Währung.
	 * @param neu neue Währung
	 */
	public void waehrungswechsel(Waehrung neu) {
		Waehrung temp = this.waehrung;
		setWaehrung(neu);
		setKontostand(this.waehrung.rechneBetragAndererWaehrung(getKontostand(), temp));
	}
	
	/**
	 * liefert den aktuellen Kontostand
	 * @return   double
	 */
	public double getKontostand() {
		return kontostand.getValue();
	}

	/**
	 * liefert die Kontonummer zurück
	 * @return   long
	 */
	public long getKontonummer() {
		return nummer;
	}

	/**
	 * liefert zurück, ob das Konto gesperrt ist oder nicht
	 * @return true, wenn das Konto gesperrt ist
	 */
	public boolean isGesperrt() {
		return gesperrt.getValue();
	}
	
	/**
	 * Erhöht den Kontostand um den eingezahlten Betrag.
	 *
	 * @param betrag double
	 * @throws IllegalArgumentException wenn der betrag negativ ist 
	 */
	public void einzahlen(double betrag) {
		if (betrag < 0 || Double.isNaN(betrag)) {
			throw new IllegalArgumentException("Falscher Betrag");
		}
		setKontostand(getKontostand() + betrag);
	}

	/**
	 * Zahlt den in der Währung w angegebenen Betrag ein
	 *
	 * @param betrag double
	 * @param w Währung der eingezahlte Betrag
	 * @throws IllegalArgumentException wenn der betrag negativ ist 
	 */
	public void einzahlen(double betrag, Waehrung w) {
		if (Double.isNaN(betrag)) {
			throw new IllegalArgumentException("Falscher Betrag");
		}

		if (w == this.waehrung) {
			einzahlen(betrag);
			return;
		}

		einzahlen(this.waehrung.rechneBetragAndererWaehrung(betrag, w));
	};
	
	/**
	 * Gibt eine Zeichenkettendarstellung der Kontodaten zurück.
	 */
	@Override
	public String toString() {
		String ausgabe;
		ausgabe = "Kontonummer: " + this.getKontonummerFormatiert()
				+ System.getProperty("line.separator");
		ausgabe += "Inhaber: " + this.inhaber;
		ausgabe += "Aktueller Kontostand: " + this.getKontostandFormatiert() ;
		ausgabe += this.getGesperrtText() + System.getProperty("line.separator");
		return ausgabe;
	}

	/**
	 * Mit dieser Methode wird der geforderte Betrag vom Konto abgehoben, wenn es nicht gesperrt ist.
	 * Diese Methode, da final ist, kann nicht weiter veraendert. Daher -> Kann nicht gemockt werden.
	 *
	 * @param betrag double
	 * @throws GesperrtException wenn das Konto gesperrt ist
	 * @throws IllegalArgumentException wenn der betrag negativ ist 
	 * @return true, wenn die Abhebung geklappt hat, 
	 * 		   false, wenn sie abgelehnt wurde
	 */
	public final boolean abheben(double betrag)
								throws GesperrtException {
		//  Test, ob das Konto gesperrt ist
		if (isGesperrt()) {
			throw new GesperrtException(nummer);
		}
		//  Test, ob der übergebene Betrag positiv ist
		if (betrag < 0 || Double.isNaN(betrag)) {
			throw new IllegalArgumentException("Betrag ungültig");
		}

		boolean abhebungErlaubt = istAbhebungErlaubt(betrag);

		//  Das eigentliche Vermindern des Kontostandes
		if (abhebungErlaubt) {
			setKontostand(getKontostand() - betrag);
			sideEffect(betrag);
		}

		return abhebungErlaubt;
	}

	/**
	 * Prueft ob die Abhebung des Kontostandes ueberhaupt erlaubt.
	 * @param betrag der abzuhebender Betrag. Double.
	 * @return true, wenn das Verminder des Kontostandes geklappt hat.
	 */
	protected abstract boolean istAbhebungErlaubt(double betrag);

	/**
	 * Was Uebriges, das noch bei der Abhebung des Kontostands geschehen sollte.
	 * @param betrag abzuhebender Betrag
	 */
	protected abstract void sideEffect(double betrag);
	
	/**
	 * Hebt den gewünschten in der Währung w angegebenen Betrag ab.
	 * 
	 * @param betrag double
	 * @param w die Währung, in der der Betrag abgehoben wird.
	 * @throws GesperrtException wenn das Konto gesperrt ist
	 * @throws IllegalArgumentException wenn der betrag negativ ist 
	 * @return true, wenn die Abhebung geklappt hat, 
	 * 		   false, wenn sie abgelehnt wurde
	 */
	public boolean abheben(double betrag, Waehrung w) throws GesperrtException {
		return abheben(this.waehrung.rechneBetragAndererWaehrung(betrag, w));
	}

	/**
	 * sperrt das Konto, Aktionen zum Schaden des Benutzers sind nicht mehr möglich.
	 */
	public void sperren() {
		prop.firePropertyChange("isGesperrt", gesperrt, true);
		gesperrt.set(true);
	}

	/**
	 * entsperrt das Konto, alle Kontoaktionen sind wieder möglich.
	 */
	public final void entsperren() {
		prop.firePropertyChange("isGesperrt", gesperrt, false);
		gesperrt.set(false);
	}
	
	
	/**
	 * liefert eine String-Ausgabe, wenn das Konto gesperrt ist
	 * @return "GESPERRT", wenn das Konto gesperrt ist, ansonsten ""
	 */
	public final String getGesperrtText()
	{
		if (isGesperrt())
		{
			return "GESPERRT";
		}
		else
		{
			return "";
		}
	}
	
	/**
	 * liefert die ordentlich formatierte Kontonummer
	 * @return auf 10 Stellen formatierte Kontonummer
	 */
	public String getKontonummerFormatiert()
	{
		return String.format("%10d", this.nummer);
	}
	
	/**
	 * liefert den ordentlich formatierten Kontostand
	 * @return formatierter Kontostand mit 2 Nachkommastellen und Währungssymbol €
	 */
	public String getKontostandFormatiert()
	{
		return String.format("%s %10.2f" , this.waehrung.name(), this.getKontostand());
	}
	
	/**
	 * Vergleich von this mit other; Zwei Konten gelten als gleich,
	 * wen sie die gleiche Kontonummer haben
	 * @param other das Vergleichskonto
	 * @return true, wenn beide Konten die gleiche Nummer haben
	 */
	@Override
	public boolean equals(Object other)
	{
		if(this == other)
			return true;
		if(other == null)
			return false;
		if(this.getClass() != other.getClass())
			return false;
		if(this.nummer == ((Konto)other).nummer)
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		return 31 + (int) (this.nummer ^ (this.nummer >>> 32));
	}

	@Override
	public int compareTo(Konto other)
	{
		if(other.getKontonummer() > this.getKontonummer())
			return -1;
		if(other.getKontonummer() < this.getKontonummer())
			return 1;
		return 0;
	}
	
	/**
	 * Diese Ausgabemethode dient gerade nur Lernzwecken, eigentlich sollte
	 * sie hier nicht stehen (Stichwort: Trenne Verarbeitung und Ein-/Ausgabe!)
	 */
	public void ausgeben()
	{
		System.out.println(this.toString());
	}

	/**
	 * Gibt zurueck, ob der Kontostand negativ ist
	 * @return true, wenn der Kontostand negativ ist
	 */
	public boolean getIstKontostandNegativ() {
		return istKontostandNegativ.getValue();
	}
}
