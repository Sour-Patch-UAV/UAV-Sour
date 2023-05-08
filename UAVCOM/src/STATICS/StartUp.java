package STATICS;

import com.fazecast.jSerialComm.*;

import EXCEPTIONS.SerialException;
import READERS.SerialReader;
import READERS.SerialResponseInitialize;

import java.io.*;
import java.time.Duration;
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
    public static boolean VerifyMyTeensy(SerialPort sp, InputStream is, OutputStream os) throws IOException, InterruptedException, SerialException {    
        // Create a SerialReader to wait for the expected response
        SerialReader reader = new SerialReader(is, "-teen Java, Im awake", new SerialResponseInitialize() {
            private boolean isTeensyResponded = false;
            @Override
            public void onSetupCommunicationWithTeensy(boolean isTeensyResponded) {
                this.isTeensyResponded = isTeensyResponded;
            }
        });

        sp.addDataListener(reader);

        // Send a message to the serial port
        String message = "-java Teensy, are you awake?"; // -java = transmission's action for teensy to interpret, the rest will be read as a message
        os.write(message.getBytes()); // send bytes of message, as Teensy will read it this way
        os.flush(); // flush these out! Basically, make sure all is clear from buffer

        // Wait for the expected response or a timeout to occur
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        while (elapsedTime < Duration.ofMillis(5000).toMillis()) {
            if (reader.isTeensyResponded()) {
                System.out.println("Teensy has responded, Comms on java side is ready!");
                sp.removeDataListener();
                return true;
            }

            // Wait for a short period before checking again
            Thread.sleep(Duration.ofMillis(1000).toMillis());
            elapsedTime = System.currentTimeMillis() - startTime;
        }

        // Timeout occurred
        System.out.println("Timeout occurred waiting for response from Teensy");
        sp.removeDataListener();
        return false;
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
    }
}