package csu.standard.simplePartition;

public enum Direction {
    North(0,1),
    East(1,0),
    South(0,-1),
    West(-1,0),
    ;
    private int x;
    private int y;
    /**
     * Construct a direction object
     * @param x
     * @param y
     */
    private Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public char toChar(){
        return toString().charAt(0);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
}
