package SimulationApplication;

public class MovementAction {
    private int addition_X;
    private int addition_Y;

    public MovementAction(int addition_X, int addition_Y){
        this.addition_X = addition_X;
        this.addition_Y = addition_Y;
    }

    public int getAddition_X() {
        return addition_X;
    }

    public int getAddition_Y() {
        return addition_Y;
    }
}
