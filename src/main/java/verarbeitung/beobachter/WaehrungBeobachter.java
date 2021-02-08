package verarbeitung.beobachter;

import verarbeitung.Konto;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class WaehrungBeobachter implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("Waehrung")) {
            Konto konto = (Konto) evt.getSource();
            System.out.println("Die Waehrung des Kontos " + konto.getKontonummerFormatiert() + " von " + System.lineSeparator() + konto.getInhaber() + "ist veraendert von " + evt.getOldValue() +  " auf " + evt.getNewValue());
        }
    }
}
