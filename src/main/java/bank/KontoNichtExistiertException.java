package bank;

/**
 * Exception wird geworfen, wenn man Operation mit Kontonummern führt, aber die Nummer in der Kontoliste nicht enthalten ist.
 */
public class KontoNichtExistiertException extends Exception {
    public KontoNichtExistiertException(String msg) {
        super(msg);
    }

    public KontoNichtExistiertException() {
        super();
    }
}
