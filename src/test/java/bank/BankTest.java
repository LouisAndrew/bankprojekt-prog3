package bank;

import fabriken.GirokontoFabrik;
import fabriken.Kontofabrik;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import verarbeitung.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BankTest {

    long blz = 12;
    Bank b = new Bank(blz);

    Kunde kunde1 = new Kunde("John", "Doe", "HTW", LocalDate.now()); // Kunde zu mocken ist unnötig
    Konto k = Mockito.mock(Konto.class); // Setze das Mock ganz am oben -> Man kann die Methoden des Kontos mocken.

    Kontofabrik mockFabrik = new Kontofabrik() {
        @Override
        public Konto erstellen(Kunde inhaber, long kontoNummer) {
            return k;
        }
    };


    /**
     * Testen das Anlegen einer Bank, wenn die angegebene BLZ negativ ist
     */
    @Test
    public void bankAnlegenFehlerTest() {
        try {
            Bank bf = new Bank(-1);
            fail();
        } catch (IllegalArgumentException e) {
            // leer
        }
    }

    /**
     * Testen das Anlegen eines Girokontos UND die zurückgegebene Kontonummer.
     */
    @Test
    public void kontoErstellenTest() throws KontoNichtExistiertException {

        long nummer = b.kontoErstellen(mockFabrik, new Kunde());
        List<Long> konten = b.getAlleKontonummern();

        assertTrue(konten.contains(nummer));
    }

    /**
     * Testen das Anlegen eines Girokontos, wenn der Inhaber null ist.
     */
    @Test
    public void kontoErstellenFehlerTest() {
        try {
            b.kontoErstellen(mockFabrik, null);
            fail();
        } catch (IllegalArgumentException e) {
            // leer
        }
    }

    /**
     * Testen die Geldabhebung eines Kontos
     * ⚠ Die Methode geldAbheben kann nicht gemockt werden.
     */
    // @Test -> Skipping test.
    public void geldAbhebenTest() throws KontoNichtExistiertException, GesperrtException {

        double betrag = 20;
        double zuGross = 50;

        // Setze den default-Kontostand auf 0 Eur.
        // Max. abhebarer Betrag: 20.
        Mockito.when(k.getKontostand()).thenReturn(0.0);

        Mockito
                .doAnswer(invocationOnMock -> {
                    Mockito.when(k.getKontostand()).thenReturn(0 - betrag);
                    return true;
                })
                .when(k).abheben(ArgumentMatchers.eq(betrag)); // Wird hier ein Fehler gemeldet -> veruscht finale Methode zu mocken.

        Mockito.doReturn(false).when(k).abheben(ArgumentMatchers.eq(zuGross));

        long nummer = b.kontoErstellen(mockFabrik, new Kunde());

        double kontostand = b.getKontostand(nummer);
        assertEquals(kontostand, 0);

        // max Abhebung = 20EUR.
        boolean erfolgreich = b.geldAbheben(nummer, betrag);
        assertTrue(erfolgreich);

        double kontostandAktuell = b.getKontostand(nummer);
        assertEquals(kontostandAktuell, kontostand - betrag);

        erfolgreich = b.geldAbheben(nummer, zuGross); // Groesser als Dispo
        assertFalse(erfolgreich);
        assertEquals(kontostandAktuell, b.getKontostand(nummer));

        Mockito.verify(k).abheben(betrag);
    }

    /**
     * Testen die Geldabhebung eines nicht existierenden Kontos
     */
    @Test
    public void geldAbhebenFehlerTest() {
        try {
            b.geldAbheben(1, 20); // Konto mit der Nr. 1 ecistiert nicht.
            fail();
        } catch (KontoNichtExistiertException e) {
            // leer
        }
    }

    /**
     * Testen die Geldeinzahlung eines Kontos
     */
    @Test
    public void geldEinzahlenTest() throws KontoNichtExistiertException {

        double neuerBetrag = 35.0;

        Mockito.when(k.getKontostand()).thenReturn(0.0);
        Mockito.doAnswer(invocationOnMock -> {
            Mockito.when(k.getKontostand()).thenReturn(neuerBetrag);
            return null;
        }).when(k).einzahlen(neuerBetrag);

        long nummer = b.kontoErstellen(mockFabrik, new Kunde());

        assertEquals(b.getKontostand(nummer), 0);

        b.geldEinzahlen(nummer, neuerBetrag);
        assertEquals(b.getKontostand(nummer), neuerBetrag);

        Mockito.verify(k).einzahlen(neuerBetrag);
    }

    /**
     * Testen die Geldeinzahlung eines nicht existierednen Kontos
     */
    @Test
    public void geldEinzahlenFehlerTest() {
        try {
            b.geldEinzahlen(2, 20);
            fail();
        } catch (KontoNichtExistiertException e) {
            // leer
        }
    }

    /**
     * Testen das Löschen eines Kontos.
     */
    @Test
    public void kontoLoeschenTest() {

        long nummer = b.kontoErstellen(mockFabrik, new Kunde());
        boolean erfolgreich = b.kontoLoeschen(nummer);

        assertTrue(erfolgreich);

        List<Long> konten = b.getAlleKontonummern();
        assertFalse(konten.contains(nummer));
    }

    /**
     * Testen das Löschen eines nicht existierenden Kontos
     */
    @Test
    public void kontoLoeschenFehlerTest() {
        boolean erfolgreich = b.kontoLoeschen(1);
        assertFalse(erfolgreich);
    }

    /**
     * Testen die Überprüfung vom Kontostand eines Kontos
     */
    @Test
    public void getKontostandTest() throws KontoNichtExistiertException {
        double betrag = 10;

        Mockito.doAnswer(invocationOnMock -> {
            Mockito.when(k.getKontostand()).thenReturn(betrag);
            return null;
        }).when(k).einzahlen(betrag);

        long nummer = b.kontoErstellen(mockFabrik, new Kunde());

        assertEquals(b.getKontostand(nummer), 0);
        b.geldEinzahlen(nummer, betrag);
        assertEquals(b.getKontostand(nummer), betrag);

        Mockito.verify(k).einzahlen(betrag);
    }

    /**
     * Testen die Überprüfung vom Kontostands eines nicht existierenden Kontos
     */
    @Test
    public void getKontostandFehlerTest() {
        try {
            b.getKontostand(1);
            fail();
        } catch (KontoNichtExistiertException e) {
            // leer
        }
    }

    /**
     * Testen der erfolgreichen Überweisung
     */
    @Test
    public void ueberweisungTest() throws KontoNichtExistiertException, GesperrtException {

        double betrag = 20;
        double zuGross = 50;

        Konto mockAbsender = Mockito.mock(Konto.class, Mockito.withSettings().extraInterfaces(Ueberweisungsfaehig.class));
        Konto mockEmpfaenger = Mockito.mock(Konto.class, Mockito.withSettings().extraInterfaces(Ueberweisungsfaehig.class));

        Mockito.when(mockAbsender.getInhaber()).thenReturn(kunde1);
        Mockito.when(mockEmpfaenger.getInhaber()).thenReturn(new Kunde());

        Mockito.when(mockAbsender.isGesperrt()).thenReturn(false);
        Mockito.when(mockEmpfaenger.isGesperrt()).thenReturn(false);

        long absender = b.kontoErstellen(new Kontofabrik() {
            @Override
            public Konto erstellen(Kunde inhaber, long kontoNummer) {
                return mockAbsender;
            }
        }, new Kunde()); // überweisungsfähig

        long empfaenger = b.kontoErstellen(new Kontofabrik() {
            @Override
            public Konto erstellen(Kunde inhaber, long kontoNummer) {
                return mockEmpfaenger;
            }
        }, new Kunde()); // überweisungsfähig

        // Das verhalten des Ueberweisungabsendens zu mocken
        Mockito.doAnswer(invocationOnMock -> {
            Mockito.when(mockAbsender.getKontostand()).thenReturn(0 - betrag);
            return true;
        }).doReturn(true).when(((Ueberweisungsfaehig) mockAbsender)).ueberweisungAbsenden(ArgumentMatchers.eq(betrag), Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString());

        // Erlauben die Ueberweisung mit dem Betrag zuGross nicht.
        Mockito.doReturn(false).when(((Ueberweisungsfaehig) mockAbsender)).ueberweisungAbsenden(ArgumentMatchers.eq(zuGross), Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString());

        // Das Verhalten des Ueberweisungempfangens zu mocken
        Mockito.doAnswer(invocationOnMock -> {
            Mockito.when(mockEmpfaenger.getKontostand()).thenReturn(betrag);
            return null;
        }).when((Ueberweisungsfaehig) mockEmpfaenger).ueberweisungEmpfangen(ArgumentMatchers.eq(betrag), Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString());

        double absenderKontostand = b.getKontostand(absender);
        double empfaengerKontostand = b.getKontostand(empfaenger);

        boolean erfolgreich = b.geldUeberweisen(absender, empfaenger, betrag, "Hello, World!");
        assertTrue(erfolgreich);

        double absenderKontostandAktuell = b.getKontostand(absender);
        double empfaengerKontostandAktuell = b.getKontostand(empfaenger);

        assertEquals(absenderKontostandAktuell, absenderKontostand - betrag);
        assertEquals(empfaengerKontostandAktuell, empfaengerKontostand + betrag);

        // Testen die Überweisung, wenn der Betrag groesser als Dispo ist.
        erfolgreich = b.geldUeberweisen(absender, empfaenger, zuGross, "Hello, World!");
        assertFalse(erfolgreich);

        assertEquals(absenderKontostandAktuell, b.getKontostand(absender));
        assertEquals(empfaengerKontostandAktuell, b.getKontostand(empfaenger));

        Mockito.verify((Ueberweisungsfaehig) mockAbsender, Mockito.times(2)).ueberweisungAbsenden(Mockito.anyDouble(), Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString());
        Mockito.verify((Ueberweisungsfaehig) mockEmpfaenger).ueberweisungEmpfangen(Mockito.anyDouble(), Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString());
    }

    @Test
    public void pleitegeierSperrenTest () {

        Konto mockKonto1 = Mockito.mock(Konto.class); // das Konto sollte gesperrt werden.
        Konto mockKonto2 = Mockito.mock(Konto.class); // das Konto sollte nicht gesperrt.

        Mockito.when(mockKonto1.getKontostand()).thenReturn(-10.0);
        Mockito.when(mockKonto2.getKontostand()).thenReturn(20.0);

        b.kontoErstellen(new Kontofabrik() {
            @Override
            public Konto erstellen(Kunde inhaber, long kontoNummer) {
                return mockKonto1;
            }
        }, new Kunde());

        b.kontoErstellen(new Kontofabrik() {
            @Override
            public Konto erstellen(Kunde inhaber, long kontoNummer) {
                return mockKonto2;
            }
        }, new Kunde());

        b.pleitegeierSperren();

        Mockito.verify(mockKonto1).sperren(); // sperren sollte aufgerufen in mockKonto1
        Mockito.verify(mockKonto2, Mockito.never()).sperren(); // sperren sollte nicht aufgerufen in mockKonto2
    }

    @Test
    public void getKundenMitVollemKontogetKundenMitVollemKontoTest () {

        Kunde kunde2 = new Kunde();
        Konto mockKonto1 = Mockito.mock(Konto.class);
        Konto mockKonto2 = Mockito.mock(Konto.class);

        Mockito.when(mockKonto1.getKontostand()).thenReturn(10.0);
        Mockito.when(mockKonto1.getInhaber()).thenReturn(kunde1);

        Mockito.when(mockKonto2.getKontostand()).thenReturn(20.0);
        Mockito.when(mockKonto2.getInhaber()).thenReturn(kunde2);

        double min = 15.0;

        b.kontoErstellen(new Kontofabrik() {
            @Override
            public Konto erstellen(Kunde inhaber, long kontoNummer) {
                return mockKonto1;
            }
        }, new Kunde());

        b.kontoErstellen(new Kontofabrik() {
            @Override
            public Konto erstellen(Kunde inhaber, long kontoNummer) {
                return mockKonto2;
            }
        }, new Kunde());;

        List<Kunde> list = b.getKundenMitVollemKonto(min);

        assertTrue(list.contains(kunde2));
        assertFalse(list.contains(kunde1));
    }

    @Test
    public void getKundengeburtstageTest () {

        Mockito.when(k.getInhaber()).thenReturn(kunde1);

        b.kontoErstellen(mockFabrik, new Kunde());

        String nameGeburtstage = b.getKundengeburtstage();

        assertTrue(nameGeburtstage.contains(kunde1.getNachname()));
        assertTrue(nameGeburtstage.contains(kunde1.getName()));
        assertTrue(nameGeburtstage.contains(kunde1.getGeburtstag().toString()));
    }

    @Test
    public void getKundengeburtstageDoppeltKundenTest () {

        Konto mockKonto1 = Mockito.mock(Konto.class);
        Konto mockKonto2 = Mockito.mock(Konto.class);
        Mockito.when(mockKonto1.getInhaber()).thenReturn(kunde1);
        Mockito.when(mockKonto2.getInhaber()).thenReturn(kunde1);

        b.kontoErstellen(new Kontofabrik() {
            @Override
            public Konto erstellen(Kunde inhaber, long kontoNummer) {
                return mockKonto1;
            }
        }, new Kunde());

        b.kontoErstellen(new Kontofabrik() {
            @Override
            public Konto erstellen(Kunde inhaber, long kontoNummer) {
                return mockKonto2;
            }
        }, new Kunde());;

        String nameGeburtstage = b.getKundengeburtstage();

        assertEquals("Name: " + kunde1.getName() + " " + kunde1.getNachname() + ". Geburtstag: " + kunde1.getGeburtstag().toString() + System.lineSeparator(), nameGeburtstage);
    }

    @Test
    public void getKontonummernLuecken () {
        Konto mockKonto1 = Mockito.mock(Konto.class);
        Konto mockKonto2 = Mockito.mock(Konto.class);

        long nummer1 = b.kontoErstellen(new Kontofabrik() {
            @Override
            public Konto erstellen(Kunde inhaber, long kontoNummer) {
                return mockKonto1;
            }
        }, new Kunde());
        long nummer2 = b.kontoErstellen(new Kontofabrik() {
            @Override
            public Konto erstellen(Kunde inhaber, long kontoNummer) {
                return mockKonto2;
            }
        }, new Kunde());;

        // erstmal die mockKonto1 loeschen
        b.kontoLoeschen(nummer1);

        // dann get die liste der luecken
        List<Long> luecken = b.getKontonummernLuecken();

        assertTrue(luecken.contains(nummer1));
        assertFalse(luecken.contains(nummer2));
    }

    /**
     * Hilfsmethode zum Vergleichen zweier Banken
     * @param b1 Bank 1
     * @param b2 Bank 2
     * @return true, wenn die zwei Banken gleich sind.
     */
    private boolean bankenVergleichen(Bank b1, Bank b2) {
        boolean hatGleicheBlz = b1.getBankleitzahl() == b2.getBankleitzahl(); // Die BLZ vergleichen
        boolean hatGleicheKonten = b1.getAlleKonten().equals(b2.getAlleKonten());
        return hatGleicheBlz && hatGleicheKonten;
    }

    /**
     * Teste die Erstellung einer Kopie von Bank und deren "Side-Effects", wenn nach der Erstellung der Kopie etwas innerhalb der Bank geaendert wird
     * @throws KontoNichtExistiertException wenn das konto nicht innerhalb kontoliste enthalten ist
     */
    @Test
    public void cloneTest() throws CloneNotSupportedException, KontoNichtExistiertException {
        // Kein Mockito benutzt. Bin nicht sicher, wie man MockKonto als Serializable setzen kann. (withExtraInterfaces klappt nicht)
        long nummer1 = b.kontoErstellen(new GirokontoFabrik(), new Kunde());

        // Default konten -> Konto1 hat den Kontostand von 10EUR und Konto2 20EUR.
        Bank kopie = b.clone(); // eine Kopie der Bank erstellen.

        assertTrue(bankenVergleichen(b, kopie));

        // Dann Geld auf konto1 einzahlen.
        b.geldEinzahlen(nummer1, 20);

        assertNotEquals(kopie, b);
        assertFalse(bankenVergleichen(b, kopie));
    }
}
