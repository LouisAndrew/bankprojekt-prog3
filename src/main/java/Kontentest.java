import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import bank.Bank;
import fabriken.GirokontoFabrik;

import verarbeitung.*;
import verarbeitung.beobachter.InhaberBeobachter;
import verarbeitung.beobachter.IsGesperrtBeobachter;
import verarbeitung.beobachter.KontostandBeobachter;
import verarbeitung.beobachter.WaehrungBeobachter;

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
		List<PropertyChangeListener> liste = new LinkedList<>();

		liste.add(new IsGesperrtBeobachter());
		liste.add(new KontostandBeobachter());
		liste.add(new WaehrungBeobachter());
		liste.add(new InhaberBeobachter());

		 Konto k = new Girokonto();
		 k.anmelden(liste);

		System.out.println(k);
		System.out.println("--ende kontrolle--");

		System.out.println("setKontostand hier..");
		k.einzahlen(20.0);
		System.out.println();

		System.out.println("setInhaber hier..");
		k.setInhaber(new Kunde("Man", "Mango", "Str", LocalDate.parse("2020-10-10")));
		System.out.println();

		System.out.println("Abheben hier");
		k.abheben(30);
		System.out.println();

		System.out.println("SetWaehrung hier..");
		k.setWaehrung(Waehrung.BGN);
		System.out.println();

		System.out.println("Sollte nichts ausgeben");
		boolean abgehoben = k.abheben(530);
		System.out.println("Abgehoben: " + abgehoben);
		System.out.println();

		System.out.println("Sperren hier.");
		k.sperren();
	}

}

