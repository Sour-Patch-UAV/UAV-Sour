import java.io.IOException;

import com.fazecast.jSerialComm.SerialPortIOException;

import COMS.LOCALCOM;
import EXCEPTIONS.SerialException;
import STATICS.StartUp;

public class App {
    public static void main(String[] args) {
        // Welcome user
        System.out.println("Welcome to my UAV Communcation Software! - Current Version: " + StartUp.LatestVersion());

        LOCALCOM LocalCommunication = new LOCALCOM();

        // initial setup for communcation
        try {
            LocalCommunication.SET_SerialPortFromUser();
        } catch (SerialPortIOException e) {
            e.printStackTrace();
        }

        try {
            LocalCommunication.Verify_Communication();
        } catch (SerialException e) {
            System.out.println("ERROR: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}