package FLIGHTCONTROL;
// flight controller class will be used for doing continuous calculations during flight!
// flight controller will be in charge of sending out instructions to send to the teensy if needed!

import java.util.ArrayList;
import java.util.List;

import SIMULATION.Simulation;
import STATICS.Formula.ConvexHullAlgorithm;

// parent to simulation class which handles the intitial query, and passage of information to and from the parent class
public class FlightController {

    private Simulation current_simulator; // pass spawn to reference
    protected List<ConvexHullAlgorithm.Point> coordinates = new ArrayList<>(); // desired coordinates to revolve around
    protected double elevation; // desired elevation
    protected int speed; // desired speed of the uav must be between 0 - 99
    protected List<List<ConvexHullAlgorithm.Point>> Flight_Path; // flight path should consist of some points where we know the plane should travel in respect to plane

    // properties from microcontroller

    public FlightController() {
        System.out.println("New Flight Controller started!");
    };

    public void Pass_Simulator(Simulation sim) {
        this.current_simulator = sim;
    };

    protected boolean Generate_FlightPath() {        
        int key = this.coordinates.size();
        if (key == 1) System.out.println("will ask for general radius around single point!");
        if (key == 2) System.out.println("will go between both points in a slightly oval path shape");
        else {
            this.Flight_Path = getConvexHullLines();
            Print_All_Lines();
        }
        
        // sending sample info for now!
        this.current_simulator.Relay_From_Parent("60,200,99");
        return true;
    };

    // calculate convex hull lines from the convex hull algorithm in Formulas/
    protected List<List<ConvexHullAlgorithm.Point>> getConvexHullLines() {
        System.out.println("Generating Convex...");
        List<ConvexHullAlgorithm.Point> convexHull = ConvexHullAlgorithm.computeConvexHull(coordinates);
        List<List<ConvexHullAlgorithm.Point>> lines = new ArrayList<>();

        for (int i = 0; i < convexHull.size(); i++) {
            List<ConvexHullAlgorithm.Point> line = new ArrayList<>();
            line.add(convexHull.get(i));
            line.add(convexHull.get((i + 1) % convexHull.size()));
            lines.add(line);
        }

        return lines;
    };
    
    // print all lines!
    protected void Print_All_Lines() {
        for (int i = 0; i < Flight_Path.size(); i++) {
            List<ConvexHullAlgorithm.Point> line = Flight_Path.get(i);
            ConvexHullAlgorithm.Point startPoint = line.get(0);
            ConvexHullAlgorithm.Point endPoint = line.get(1);

            System.out.println("Line " + (i + 1) + ":");
            System.out.println("Start Point: " + startPoint);
            System.out.println("End Point: " + endPoint);
            System.out.println();
        };
    };

    // overloaded method to get a specific line
    protected void Print_All_Lines(int i) {
        List<ConvexHullAlgorithm.Point> line = Flight_Path.get(i);
        ConvexHullAlgorithm.Point startPoint = line.get(0);
        ConvexHullAlgorithm.Point endPoint = line.get(1);

        System.out.println("Line " + (i + 1) + ":");
        System.out.println("Start Point: " + startPoint);
        System.out.println("End Point: " + endPoint);
        System.out.println();
    };
};