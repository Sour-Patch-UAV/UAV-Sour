package STATICS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import EXCEPTIONS.CommandException;

// this class will be necessary to take in instructions from the user or algorithm and convert it to the correct implementation for
// the microcontroller to interpret
public class InstructionManagement {

    // expected headers/cmd from use in shell
    private static ArrayList<String> headers = new ArrayList<String>(Arrays.asList(
        Definitions.QUIT, // quit within shell
        Definitions.HELP, // prints the cmds
        Definitions.TEST, // sends out sample instructions to the microcontroller
        Definitions.XAXIS, // needs instruction (angle)
        Definitions.YAXIS, // needs instruction (angle)
        Definitions.THRUST, // needs instruction (0 < pwr <= 99) greater than 0, or less than or equal to 99
        Definitions.TALK, // needs instruction (some message)
        Definitions.RESET
    ));

    // user typed "quit" to exit shell
    public static void QUIT() {
        System.out.println("Goodbye!");
        System.exit(0);
    }
    
    // use typed "help" to see all possible instruction
    public static void PRINTALL() {
        System.out.println("---------------------------");
        System.out.println("-----INSTRUCTIONS FOR SHELL-----");
        System.out.println("---------------------------");
        System.out.println("-----PLEASE DO NOT USE SHELL WHILE THE PLANE IS FLYING-----");
        System.out.println(
            "quit : exits the shell\nhelp : prints all commands\ntest : sends out sample instructions to the microcontroller\n---------------------------\nThe following commands require extra instructions\n---------------------------\nxaxis* : instruction to move aileron to angle (rads) -> xaxis 40\nyaxis* : instruction to move elevator to angle (rads) -> yaxis 30\nthrust* : instruction to set power of motor(s) to number -> thrust 45\ntalk : instruction to send a message to the microcontroller and expect it back -> talk -java \"this is a message\"\n**Commands followed by * can also accept multiple instructions -> thrust 30,40,50,60,90,0 OR xaxis 20,30,40,45,0"
        );
        System.out.println("---------------------------");
    };

    public static boolean SEND(Messenger messenger, String msg) throws IllegalArgumentException {
        if (msg == null || msg.trim().isEmpty()) throw new IllegalArgumentException("Input is null or empty.");

        String[] parts = msg.split(" ", 2);
        String command = parts[0]; // cmd

        if (!CHECK_CMD(command)) {
            System.out.println(command + " is not a command. Try again!");
            return true; // prompt user for input again
        }

        switch (command) {
            case Definitions.HELP:
                PRINTALL(); // print all commands 
                return true;
            case Definitions.TEST:
                try {
                    TEST(messenger);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case Definitions.XAXIS:
                return true;
            case Definitions.YAXIS:
                return true;
            case Definitions.THRUST:
                return true;
            case Definitions.TALK:
                return true;
            case Definitions.RESET:
                return true;
            default:
                System.out.println("not sure how you got here, but failed to see the command is not expected!");
                return false;
        }
    };

    // this instruction will send a random instruction to the microcontroller to verify it's working
    private static void TEST(Messenger stream) throws IOException {
        stream.WriteToOutput("a 8,12,16,22,6,10,6");
    };

    // checks our known commands with input, if not found, throw exception
    private static boolean CHECK_CMD(String cmd) {
        return headers.contains(cmd);
    }

    // checks the instruction paired with the cmd, should not continue if out of bounds
    // throw here, as the instruction MUST be within some bounds, and I don't want anyone getting hurt!
    private static void CHECK_INSTRUCTION(String cmd, String instruction) throws CommandException {

    }
};