package EXCEPTIONS;

public class CommandException extends Exception {
    
    // overloaded const.
    public CommandException(String msg) {
        super(msg);
    }

    // default const.
    public CommandException() {
        this("That command was not found! Try again.");
    }
}