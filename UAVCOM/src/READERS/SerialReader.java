package READERS;

import com.fazecast.jSerialComm.*;

import java.io.IOException;
import java.io.InputStream;

public class SerialReader implements SerialPortDataListenerWithExceptions {
    private InputStream inputStream;
    private String expectedResponse;
    private SerialResponseInitialize listener;
    private boolean responseReceived = false;

    public SerialReader(InputStream inputStream, String expectedResponse, SerialResponseInitialize serialResponseListener) {
        this.inputStream = inputStream;
        this.expectedResponse = expectedResponse;
        this.listener = serialResponseListener;
    }

    // this way, I can setup several listeners with their own separate unique requirements for the strings
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == getListeningEvents()) {
            try {
                byte[] buffer = new byte[inputStream.available()];
                int length = inputStream.read(buffer);
                String message = new String(buffer, 0, length);
                System.out.println("(JAVA) Received message: " + message);
                if (message.trim().equals(expectedResponse)) {
                    responseReceived = true;
                    listener.onSetupCommunicationWithTeensy(this.responseReceived); // Pass true to indicate that Teensy has responded
                } else {
                    System.out.println("This listener did not see the intended response.");
                }
            } catch (IOException e) {
                System.err.println("Error reading from serial port: " + e);
            }
        }
    }
    
    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void catchException(Exception arg0) {
        System.out.println("ERROR: " + arg0);
    }

    // return true if the response was received
    public boolean isTeensyResponded() {
        return this.responseReceived;
    }
}