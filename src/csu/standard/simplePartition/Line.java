package csu.standard.simplePartition;

import java.util.ArrayList;
import java.util.List;


public class Line {
    public static final int MAX_M = 1000;
    public static final double TOLERANCE_ANGLE = Math.PI/8;
    public static final double TOLERANCE_C = 0.5E7;
    double m;
    double c;
    /**
     * Contruct line object
     * @param m
     * @param c
     */
    public Line(double m, double c) {
        this.m = m;
        this.c = c;
    }

    /**
     * Contstruct line object.
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public Line(double x1, double y1, double x2, double y2) {
        if ( x1 == x2){
            m = Line.MAX_M;
        }else{
            m = ((double)y1-y2)/(x1-x2);
            if(m > Line.MAX_M){
                m = Line.MAX_M;
            }
        }
        c = y1 - m * x1;
    }
    /**
     * 
     * @param other
     * @return
     */

    public boolean isClose(Line other){
        if(hasSimilarM(other)){
            return Math.abs(c-other.c)<TOLERANCE_C;
        }
        return false;
    }
    /**
     * 
     * @param other
     * @return
     */

    public boolean hasSimilarM(Line other){
        double a = Math.atan(m);
        double b = Math.atan(other.m);
        return Math.abs(a-b)<TOLERANCE_ANGLE;
    }


    public boolean isOnLeft(int x, int y){
        return y - m * x - c > 0;
    }
    /**
     * Removes similar lines in the list.
     * @param lines
     * @return
     */
    public static List<Line> removeSimilarLines(List<Line> lines){
        List<Line> newLines = new ArrayList<Line>();
        for(Line line : lines){
            boolean found = false;
            for(Line acceptedLine : newLines){
                if(acceptedLine.isClose(line)){
                    found = true;
                    break;
                }
            }
            if(!found){
                newLines.add(line);
            }
        }
        return newLines;
    }
    /**
     * 
     * @return
     */

    @Override
    public String toString(){
        return "Line: "+ m + ", " + c;
    }
}
