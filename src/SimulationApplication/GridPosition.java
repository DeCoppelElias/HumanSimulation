package SimulationApplication;

public class GridPosition {
    private final int x;
    private final int y;

    public GridPosition(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public static double distance(GridPosition other, GridPosition other2){
        double first = Math.pow(other2.getX() - other.getX(), 2);
        double second = Math.pow(other2.getY() - other.getY(), 2);
        return Math.sqrt(first+ second);
    }

    public static GridPosition maxGridPosition(){
        return new GridPosition(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public Object clone(){
        return new GridPosition(this.x, this.y);
    }

    @Override
    public String toString() {
        return "("+x+","+y+")";
    }
}
