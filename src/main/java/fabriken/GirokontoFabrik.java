package fabriken;

import verarbeitung.Girokonto;
import verarbeitung.Konto;
import verarbeitung.Kunde;

public class GirokontoFabrik extends Kontofabrik {

    private static final double DEFAULT_DISPO = 20;

    @Override
    public Konto erstellen(Kunde inhaber, long kontoNummer) {
        return new Girokonto(inhaber, kontoNummer, DEFAULT_DISPO);
    }
}
