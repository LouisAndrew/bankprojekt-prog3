package verarbeitung;

/**
 * Ein Girokonto
 * @author Doro
 *
 */
public class Girokonto extends Konto implements Ueberweisungsfaehig{
	/**
	 * Wert, bis zu dem das Konto überzogen werden darf
	 */
	private double dispo;

	/**
	 * erzeugt ein leeres, nicht gesperrtes Standard-Girokonto
	 * von Herrn MUSTERMANN
	 */
	public Girokonto()
	{
		super(Kunde.MUSTERMANN, 99887766);
		this.dispo = 500;
	}
	
	/**
	 * erzeugt ein Girokonto mit den angegebenen Werten
	 * @param inhaber Kontoinhaber
	 * @param nummer Kontonummer
	 * @param dispo Dispo
	 * @throws IllegalArgumentException wenn der inhaber null ist oder der angegebene dispo negativ bzw. NaN ist
	 */
	public Girokonto(Kunde inhaber, long nummer, double dispo)
	{
		super(inhaber, nummer);
		if(dispo < 0 || Double.isNaN(dispo))
			throw new IllegalArgumentException("Der Dispo ist nicht gültig!");
		this.dispo = dispo;
	}

	/**
	 * erzeugt ein Girokonto mit den angegebenen Werten mit angegebener Währung
	 * @param inhaber Kontoinhaber
	 * @param nummer Kontonummer
	 * @param dispo Dispo
	 * @throws IllegalArgumentException wenn der inhaber null ist oder der angegebene dispo negativ bzw. NaN ist
	 */
	public Girokonto(Kunde inhaber, long nummer, double dispo, Waehrung w)
	{
		super(inhaber, nummer, w);
		if(dispo < 0 || Double.isNaN(dispo))
			throw new IllegalArgumentException("Der Dispo ist nicht gültig!");
		this.dispo = dispo;
	}
	
	/**
	 * liefert den Dispo
	 * @return Dispo von this
	 */
	public double getDispo() {
		return dispo;
	}

	/**
	 * setzt den Dispo neu
	 * @param dispo muss größer sein als 0
	 * @throws IllegalArgumentException wenn dispo negativ bzw. NaN ist
	 */
	public void setDispo(double dispo) {
		if(dispo < 0 || Double.isNaN(dispo))
			throw new IllegalArgumentException("Der Dispo ist nicht gültig!");
		this.dispo = dispo;
	}
	
	@Override
    public boolean ueberweisungAbsenden(double betrag, 
    		String empfaenger, long nachKontonr, 
    		long nachBlz, String verwendungszweck) 
    				throws GesperrtException 
    {
      if (this.isGesperrt())
            throw new GesperrtException(this.getKontonummer());
        if (betrag < 0 || Double.isNaN(betrag) || empfaenger == null || verwendungszweck == null)
            throw new IllegalArgumentException("Parameter fehlerhaft");
        if (getKontostand() - betrag >= - dispo)
        {
            setKontostand(getKontostand() - betrag);
            return true;
        }
        else
        	return false;
    }

    @Override
    public void ueberweisungEmpfangen(double betrag, String vonName, long vonKontonr, long vonBlz, String verwendungszweck)
    {
        if (betrag < 0 || Double.isNaN(betrag) || vonName == null || verwendungszweck == null)
            throw new IllegalArgumentException("Parameter fehlerhaft");
        setKontostand(getKontostand() + betrag);
    }
    
    @Override
    public String toString() {
		String ausgabe = "-- GIROKONTO --" + System.lineSeparator() +
				super.toString()
				+ "Dispo: " + this.dispo + System.lineSeparator();
		return ausgabe;
	}

	@Override
	protected boolean istAbhebungErlaubt(double betrag) {
		return getKontostand() - betrag >= - dispo;
	}

	@Override
	protected void sideEffect(double betrag) {
		// kein SideEffect.
	}

	/**
	 * Das Überschreiben der Methode waehrungswechsel => Dispo sollte auch umgerechnet.
	 * @param neu neue Währung
	 */
	@Override
	public void waehrungswechsel(Waehrung neu) {
		this.dispo = getWaehrung().rechneBetragAndererWaehrung(this.dispo, neu);
		super.waehrungswechsel(neu);
	}
}
