import java.io.IOException;

import com.fazecast.jSerialComm.SerialPortIOException;

import COMS.LOCALCOM;
import EXCEPTIONS.SerialException;
import STATICS.StartUp;

public class App {
    public static void main(String[] args) throws InterruptedException, SerialException {
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
        } catch (SerialException | IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        
        // attempt to wait here until the communication supervisor states the system is ready!
        while (!LocalCommunication.GET_CommunicationIsSuccessful()) {
            Thread.sleep(500); // let's wait!
        };

        System.out.println("Communication Supervisor states system is ready?: " + LocalCommunication.GET_CommunicationIsSuccessful());

        // now, send some test data to verify the general worker is working correctly!
        try {
            // the serial worker WILL expect what is in here, can be altered to test for separate areas of effort
            LocalCommunication.Verify_Peripheral("8,12,16,22,5");
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        while(LocalCommunication.GET_CommunicationIsSuccessful() && !LocalCommunication.GET_PeripheralIsSuccessful()) {
            Thread.sleep(2000); // waiting!
        }

        System.out.println("Communication Supervisor states peripherals are OK?: " + LocalCommunication.GET_PeripheralIsSuccessful());

        // // now, hire a general line worker, and they'll be persistent and look out for anything with -teen, from teensy via contains
        // try {
        //     LocalCommunication.General_Communication();
        // } catch (SerialException | IOException e) {
        //     System.out.println("ERROR: " + e.getMessage());
        // };
    };
};