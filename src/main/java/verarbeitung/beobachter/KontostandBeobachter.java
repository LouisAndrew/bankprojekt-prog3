package verarbeitung.beobachter;

import verarbeitung.Konto;
import verarbeitung.Waehrung;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class KontostandBeobachter implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("Kontostand")) {
            Konto konto = (Konto) evt.getSource();
            Waehrung waehrung = konto.getAktuelleWaehrung();
            System.out.println("Kontostand des Kontos " + konto.getKontonummerFormatiert() + " von " + System.lineSeparator() + konto.getInhaber() + "ist veraendert von " + evt.getOldValue() + "  " + waehrung +  " auf " + evt.getNewValue() + " " + waehrung);
        }
    }
}
