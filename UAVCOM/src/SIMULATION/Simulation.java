package SIMULATION;

import java.util.List;
import java.util.Scanner;

import FLIGHTCONTROL.FlightController;
import STATICS.Definitions;
import STATICS.GetClassName;
import STATICS.InstructionManagement;
import STATICS.Messenger;
import STATICS.Formula.ConvexHullAlgorithm;
import STATICS.Formula.ConvexHullAlgorithm.Point;

public class Simulation extends FlightController {
    
    private final int DEFAULT = 0;
    private Scanner scanner;
    private Messenger messenger;

    // default constructor, pass scanner from App.java to this!
    public Simulation(Scanner recycledScanner, Messenger msgr) {
        super();
        System.out.println(GetClassName.THIS_CLASSNAME(this, "Simulation Object created!"));
        Pass_Simulator(this); // pass THIS to the parent for referal
        this.scanner = recycledScanner;
        this.messenger = msgr;
        Query_User(); // ask user for 
        if (startFlight()) Relay_To_Parent();
    };

    private void Query_User() {
        setCoordinates();
        setElevation();
        setSpeed();
        print_Simulation_Stats(); // print stats to user
    };

    // method to query user for coordinates
    private void setCoordinates() {
        System.out.print("Enter coordinates (x,y) - separate values by space: ");
        String input = this.scanner.nextLine();
        String[] values = input.split(" ");
        for (String value : values) {
            String[] coordinate = value.split(",");
            double x = Double.parseDouble(coordinate[0].trim());
            double y = Double.parseDouble(coordinate[1].trim());
            this.coordinates.add(new ConvexHullAlgorithm.Point(x, y));
        };
    };

    // method to query user for elevation
    private void setElevation() {
        System.out.print("Enter desired elevation: ");
        elevation = this.scanner.nextDouble();
    };

    // method to query user for speed
    private void setSpeed() {
        System.out.print("Enter desired speed (0-99): ");
        speed = this.scanner.nextInt();
        while (speed < DEFAULT || speed > 99) {
            System.out.println("Invalid speed. Speed must be between 0 and 99.");
            System.out.print("Enter desired speed (0-99): ");
            speed = this.scanner.nextInt();
        }
        this.scanner.nextLine(); // Consume the newline character
    };

    // method to get coordinates
    public List<Point> getCoordinates() {
        return coordinates;
    };

    // method to get elevation
    public double getElevation() {
        return elevation;
    };

    // method to get speed
    public int getSpeed() {
        return speed;
    };

    private void Reset_State() {
        this.coordinates.clear();
        this.elevation = DEFAULT;
        this.speed = DEFAULT;
        GetClassName.THIS_CLASSNAME(this, "Cleared State");
    };

    public void print_Simulation_Stats() {
        System.out.println("Coordinates: " + this.getCoordinates());
        System.out.println("Elevation: " + this.getElevation());
        System.out.println("Speed: " + this.getSpeed());
    };

    private void Relay_To_Parent() {
        Generate_FlightPath();
    };

    public void Relay_From_Parent(String msg) {
        // should expect msg to be a state command: ex -> 40,500,98
        InstructionManagement.SEND(messenger, (Definitions.STATE + " " + msg));
    };

    private boolean startFlight() {
        System.out.print("Start flight? (y/n/edit): ");
        String input = this.scanner.nextLine().toLowerCase();
        switch (input) {
            case "y":
                return true;
                case "n":
                return false;
            case "edit":
                Reset_State(); // reset state
                Query_User();
                return startFlight();
            default:
                System.out.println("Invalid input. Please enter 'y', 'n', or 'edit'.");
                return startFlight();
        }
    };
};