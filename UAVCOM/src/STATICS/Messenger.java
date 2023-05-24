package STATICS;

import java.io.IOException;
import java.io.OutputStream;

public class Messenger {

    // stale output stream
    private OutputStream os;

    public Messenger(OutputStream outputStream) {
        this.os = outputStream;
        System.out.println("Messenger Created Successfully!");
    };

    public void WriteToOutput(String msg) throws IOException {
        this.os.write(appendMessage(msg).getBytes());
        this.os.flush();
    };

    public static void WriteToOutput(OutputStream os, String msg) throws IOException {
        os.write(appendMessage(msg).getBytes());
        os.flush();
    };

    private static String appendMessage(String msg) {
        return Definitions.PRE_JAVA + msg;
    } 
};