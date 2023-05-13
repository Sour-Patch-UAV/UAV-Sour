import java.io.IOException;
import java.util.Scanner;

import com.fazecast.jSerialComm.SerialPortIOException;

import COMS.LOCALCOM;
import EXCEPTIONS.CommandException;
import EXCEPTIONS.SerialException;
import SHELL.Shell;
import STATICS.Definitions;
import STATICS.StartUp;

public class App {
    public static void main(String[] args) throws InterruptedException, SerialException, IllegalArgumentException, CommandException {
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

        System.out.println("Communication Supervisor states communications are OK?: " + LocalCommunication.GET_CommunicationIsSuccessful());

        // now, send some test data to verify the general worker is working correctly!
        try {
            // the serial worker WILL expect what is in here, can be altered to test for separate areas of effort
            LocalCommunication.Verify_Peripheral("8,12,16,22,6");
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        while(LocalCommunication.GET_CommunicationIsSuccessful() && !LocalCommunication.GET_PeripheralIsSuccessful()) {
            Thread.sleep(500); // waiting!
        }

        System.out.println("Communication Supervisor states peripherals are OK?: " + LocalCommunication.GET_PeripheralIsSuccessful());

        // now, hire a general line worker, and they'll be persistent and look out for anything with -teen, from teensy via contains
        try {
            LocalCommunication.General_Communication();
        } catch (SerialException | IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        };

        // loop until the user enters either "SIMULATION" or "SHELL"
        String mode = Definitions.MODE_SHELL; // default is shell
        Scanner scan = new Scanner(System.in);
        while (true) {
            while(true) {
                System.out.println("Please select an option:");
                System.out.println("1. EXIT");
                System.out.println("2. Continue with " + Definitions.MODE_SHELL.toUpperCase());
                System.out.println("3. Begin " + Definitions.MODE_SIMULATION.toUpperCase());
        
                String option = scan.nextLine();
        
                if ("1".equals(option)) {
                    System.out.println("Thank you for using my software!\n - Cristian Turbeville");
                    System.exit(0);
                } else if ("2".equals(option)) {
                    mode = Definitions.MODE_SHELL;
                    break; // exit inner loop and continue with shell
                } else if ("3".equals(option)) {
                    mode = Definitions.MODE_SIMULATION;
                    break; // exit inner loop and start simulation
                }
        
                System.out.println(option + " was not a correct input, please try again!");
            };
            
            switch(mode) {
                case Definitions.MODE_SHELL:
                    new Shell(scan); // start new shell! and recycle scanner
                    break;
                case Definitions.MODE_SIMULATION:
                    System.out.println("Unfortunately, our machine is down! Try again tomorrow!");
                    break;
                default:
                    continue;
            }
        }
    };
};