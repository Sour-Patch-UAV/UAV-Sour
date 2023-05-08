package EXCEPTIONS;

public class SerialException extends Exception {
    
    // overloaded const.
    public SerialException(String msg) {
        super(msg);
    }

    // default const.
    public SerialException() {
        this("Error with Serial Port");
    }
}