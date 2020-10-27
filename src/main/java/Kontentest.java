import java.time.LocalDate;
import verarbeitung.GesperrtException;
import verarbeitung.Girokonto;
import verarbeitung.Konto;
import verarbeitung.Kontoart;
import verarbeitung.Kunde;
import verarbeitung.Sparbuch;
import verarbeitung.Waehrung;

/**
 * Testprogramm für Konten
 * @author Doro
 *
 */
public class Kontentest {

	/**
	 * Testprogramm für Konten
	 * @param args wird nicht benutzt
	 * @throws GesperrtException 
	 */
	public static void main(String[] args) throws GesperrtException {
		//viiiiiiiiel Code
		Kunde ich = new Kunde("Dorothea", "Hubrich", "zuhause", LocalDate.parse("1976-07-13"));
		
		Girokonto meinGiro = new Girokonto(ich, 1234, 1000.0);
		meinGiro.einzahlen(50);
        System.out.println(meinGiro);
        
        meinGiro.abheben(20, Waehrung.KM);
        System.out.println(meinGiro);
		
		Sparbuch meinSpar = new Sparbuch(ich, 9876, Waehrung.BGN);
		meinSpar.einzahlen(50);
		try
		{
			boolean hatGeklappt = meinSpar.abheben(70);
			System.out.println("Abhebung hat geklappt: " + hatGeklappt);
			System.out.println(meinSpar);
		}
		catch (GesperrtException e)
		{
			System.out.println("Zugriff auf gesperrtes Konto - Polizei rufen!");
		}
	
		System.out.println("Neu:------------------------");
		Konto k = new Girokonto();
		k.abheben(100);   // Code in Girokonto
							//Konstruktor ist st�rker als Variablentyp
		System.out.println(k.toString()); //Code im Girokonto
		System.out.println("�ber ausgeben-Methode:------------------");
		k.ausgeben();
		
		int a = 100;
		int b = a;     //primitive Datentypen: Kopien
		a += 50;
		System.out.println(b);  //100
		Konto ka = new Girokonto();
		Konto kb = ka;   //nur ein einziges Objekt! Alle Objektvariablen
						//sind Referenzen
		ka.einzahlen(50);
		System.out.println(kb.getKontostand());  //50
		
		System.out.println("Enums:-------------------------");
		Kontoart art = Kontoart.GIROKONTO;  //Zugriff auf statische Elemente
		Kontoart art2 = Kontoart.SPARBUCH;
		
		System.out.println(art2.toString());
		System.out.println(art2.name() + " " + art2.ordinal());
		System.out.println(Kontoart.FESTGELDKONTO.getWerbebotschaft());
		
		Kontoart[] alle = Kontoart.values();
		for(int i=0; i< alle.length; i++)
			System.out.println(alle[i].ordinal()+ ": " + alle[i]);
		
		String frage = "GIROKONTO";
		Kontoart gewaehlt = Kontoart.valueOf(frage);
		System.out.println(gewaehlt);
		
	}

}

