package verarbeitung.beobachter;

import verarbeitung.Konto;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class IsGesperrtBeobachter implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("isGesperrt")) {
            Konto konto = (Konto) evt.getSource();
            String istGesperrtAnzeige = (boolean) evt.getNewValue() ? "ist gesperrt" : "ist nicht gesperrt";
            System.out.println("Kontostand des Kontos " + konto.getKontonummerFormatiert() + " von " + System.lineSeparator() + konto.getInhaber() + istGesperrtAnzeige);
        }
    }
}
