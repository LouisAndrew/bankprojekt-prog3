package verarbeitung.beobachter;

import verarbeitung.Konto;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class InhaberBeobachter implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("Inhaber")) {
            Konto konto = (Konto) evt.getSource();
            System.out.println("Der Inhaber des Kontos " + konto.getKontonummerFormatiert() + " ist veraendert von " + System.lineSeparator() + evt.getOldValue() + "auf" + System.lineSeparator() + evt.getNewValue());
        }
    }
}
