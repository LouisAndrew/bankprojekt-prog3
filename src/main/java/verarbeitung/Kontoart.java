package verarbeitung;

/**
 * Aufz�hlung aller im Bankprogramm angebotenen Kontoarten
 * @author Dorothea Hubrich
 *
 */
public enum Kontoart {
	//zuerst die Konstanten:
	GIROKONTO("Ganz hoher Dispo"), 
	SPARBUCH("viele Zinsen"), 
	FESTGELDKONTO("sp�ter vielleicht....");
	
	//dann alle Attribute, Methoden, Konstruktoren
	private final String werbebotschaft;

	/**
	 * @return die Werbebotschaft
	 */
	public String getWerbebotschaft() {
		return this.werbebotschaft;
	}
	
	@Override
	public String toString()
	{
		return this.name() + ": " + this.werbebotschaft; 
	}
	
	Kontoart(String werbe)
		//Konstruktor ist privat, weil er NICHT von woanders aufgerufen
		//werden darf, es sollen keine neue Objekte m�glich sein
	{
		this.werbebotschaft = werbe;
	}
	
	
}
