package EXCEPTIONS;

public class SerialException extends Exception {
    
    // overloaded const.
    public SerialException(String msg) {
        super(msg);
    }

    // default const.
    public SerialException() {
        this("Serial Port is currently in use or is not able to be accessed! Try again.");
    }
}