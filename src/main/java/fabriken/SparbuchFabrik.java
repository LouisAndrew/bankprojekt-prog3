package fabriken;

import verarbeitung.Konto;
import verarbeitung.Kunde;
import verarbeitung.Sparbuch;

public class SparbuchFabrik  extends  Kontofabrik{

    @Override
    public Konto erstellen(Kunde inhaber, long kontoNummer) {
        return new Sparbuch(inhaber, kontoNummer);
    }
}
