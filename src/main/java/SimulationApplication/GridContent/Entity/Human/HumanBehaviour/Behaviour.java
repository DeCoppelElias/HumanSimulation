package SimulationApplication.GridContent.Entity.Human.HumanBehaviour;

public abstract class Behaviour {
    public abstract Behaviour createVariation() throws Exception ;

    public double clamp(double value, double lowerbound, double upperbound){
        if(value < lowerbound) return lowerbound;
        if(value > upperbound) return upperbound;
        return value;
    }
}
