package STATICS;

import java.io.IOException;
import java.io.OutputStream;

public class Messenger {
    public static void WriteToOutput(OutputStream os, String msg) throws IOException {
        os.write(msg.getBytes());
        os.flush();
    };
}