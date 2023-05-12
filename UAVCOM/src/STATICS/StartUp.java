package STATICS;

import com.fazecast.jSerialComm.*;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;

public class StartUp {

    public static String LatestVersion() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("src/INFO/config.properties")) {
            prop.load(input);
            return prop.getProperty("version");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "no version found";
    };

    // REQUIRED TO START COMMUNICATIONS
    public static void VerifyMyTeensy(OutputStream os) throws IOException, InterruptedException {    
        // Once here, LocalCom has already instructed ReaderSupervisor to hire a new SerialReader (verifier) to look for teensy's awake message 
        // Send a message to the serial port
        String message = "-java Teensy, are you awake?"; // -java = transmission's action for teensy to interpret, the rest will be read as a message
        try {
            WriteToOutput(os, message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    public static void VerifyPeripheralWorker(OutputStream os, String mvmt) throws IOException, InterruptedException {
        // moving servos via command, want to have the servos move in some ladder movement and send back the postitions in a concat string to verify
        String message = "-java TestServo" + mvmt;
        try {
            WriteToOutput(os, message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    public static void VerifyGeneralWorker(OutputStream os) {   
        String message = "-java Teensy, this is a test message.";
        try {
            WriteToOutput(os, message);
        } catch (IOException e) {
            e.printStackTrace();
        };
    };

    private static void WriteToOutput(OutputStream os, String msg) throws IOException {
        os.write(msg.getBytes());
        os.flush();
    };

    public static boolean CheckPeripheralResponse(String a, String b) {

        a = subString(a,6);
        b = subString(b, 6);

        a = removeAll(a,"0", "");
        b = removeAll(b,",", "");

        // Compare the two byte arrays
        return a.equals(b);
    };
    
    // helper for reader supervisor, why wouldn't their job be easy?!
    public static boolean CheckResponse_STRICT(String a, String b) {
        if(a.trim().equals(b)) return true;
        return false;
    };

    // loose checks
    public static boolean CheckResponse_LOOSE(String a, String b) {
        if(a.trim().equals(b)) return true;
        return false;
    };

    private static String subString(String str, int index) {
        return str.substring(index);
    };

    private static String removeAll(String str, String chr, String repl) {
        return str.replaceAll(chr, repl);
    }

    public static SerialPort MySerialPort() throws SerialPortIOException {
        Scanner scanner = new Scanner(System.in);
        // user will have to input their desired COM PORT
        System.out.print("Enter the serial port name (e.g. COM1, /dev/ttyUSB0): ");
        String name = scanner.nextLine();
        // create serialport object to access in VerifyMyTeensy (VMT)
        // VMT will then attempt to "Verify" some communcation is successful between the teensy and java
        // it will do so by sending a message to the teesny, which it is expecting, and the same for java from the teensy
        // once successful, java will output a go ahead message, from that point, teensy has set a bool to true for success and
        // will now look for desired input (verification message is not expected anymore).
        SerialPort sp = SerialPort.getCommPort(name);
        scanner.close();
        return sp;
    };
};