package bank;

/**
 * Exception wird geworfen, wenn etwas bei der Überweisung schiefgeht
 */
public class UeberweisungException extends Exception {
    public UeberweisungException(String msg) {
        super(msg);
    }

    public UeberweisungException() {
        super();
    }
}
