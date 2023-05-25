package STATICS;

import java.util.ArrayList;
import java.util.List;

public class Formula {

    public class ConvexHullAlgorithm {
        public static class Point implements Comparable<Point> {
            private double x; // x-axis var
            private double y; // y-axis var

            public Point(double x2, double y2) {
                this.x = x2;
                this.y = y2;
            };
            
            public int compareTo(Point p) {
                if (this.x == p.x) {
                    return (int)(this.y - p.y);
                } else {
                    return (int)(this.x - p.x);
                }
            };

            public String toString() {
                return "(" + x + "," + y + ")";
            };

            public double get_Point_X() {
                return this.x;
            };
            
            public double get_Point_Y() {
                return this.y;
            };
        };

        // time complexity of O(n^2), where n is the number of points.
        // space complexity of O(n), where n is the number of points in the convex hull.
        public static List<Point> computeConvexHull(List<Point> points) {
            if (points.size() < 3) {
                throw new IllegalArgumentException("At least 3 points are required to compute the convex hull!");
            };

            List<Point> hull = new ArrayList<>();
            int leftmostPointIndex = getLeftmostPointIndex(points);

            int currentPointIndex = leftmostPointIndex;
            int nextPointIndex;
            do {
                hull.add(points.get(currentPointIndex));
                nextPointIndex = (currentPointIndex + 1) % points.size();

                for (int i = 0; i < points.size(); i++) {
                    if (isCounterClockwise(points.get(currentPointIndex), points.get(i), points.get(nextPointIndex))) {
                        nextPointIndex = i;
                    }
                }

                currentPointIndex = nextPointIndex;
            } while (currentPointIndex != leftmostPointIndex);

            return hull;
        }

        private static int getLeftmostPointIndex(List<Point> points) {
            int leftmostIndex = 0;
            for (int i = 1; i < points.size(); i++) {
                if (points.get(i).x < points.get(leftmostIndex).x) {
                    leftmostIndex = i;
                }
            }
            return leftmostIndex;
        };

        private static boolean isCounterClockwise(Point p1, Point p2, Point p3) {
            int crossProduct = (int)((p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x));
            return crossProduct > 0;
        };
    };
};