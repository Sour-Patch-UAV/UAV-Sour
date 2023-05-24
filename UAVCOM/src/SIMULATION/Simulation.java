package SIMULATION;

import java.util.ArrayList;

import STATICS.GetClassName;

// this class is meant for running some automated simulation from provided coordinates!
// coordinates can be of any amount

// coordinates should be provided by user input and then recieved 1 by 1, then at the end, we ask for
// elevation and prefered speed!

// 1 coordinate = "hover"/circle around single coordinate
// 2 coordinate = "cycle"/fly to and from two coordinates in a slightly circular motion
// >= 3 coordinate = "fly around"/fly around all coordinates by point to point, 3 makes a triangle, more makes a closer to like circle

public class Simulation {

    private ArrayList<Double> coordinates = new ArrayList<>(); // desired coordinates to revolve around
    private double elevation; // desired elevation
    private int speed; // desired speed of the uav must be between 0 - 99

    // default constructor
    public Simulation() {
        System.out.println(GetClassName.THIS_CLASSNAME(this, "Simulation Object created!"));
        // get coordinates

        // get desired elevation to maintain

        // get speed
    };

    // query user for coordinates

    // query user for elevation

    // query user for speed
};