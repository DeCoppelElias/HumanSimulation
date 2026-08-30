package SimulationApplication.GridContent.Entity.Human.HumanBehaviour;

import SimulationApplication.GridContent.Entity.Human.Human;

import java.util.Random;

public class FoodBehaviour extends Behaviour{
    private Human human;

    private double aggressive;
    private double peacefully;

    private Random random = new Random();

    public FoodBehaviour(Human human){
        this.human = human;

        double r = random.nextDouble();

        this.aggressive = r;
        this.peacefully = 1 - r;
    }

    public FoodBehaviour(Human human, double aggressive, double peacefully){
        this.human = human;

        double r = random.nextDouble();

        this.aggressive = aggressive;
        this.peacefully = peacefully;
    }

    public String getBehaviour(){
        double r = random.nextDouble();

        if(r < aggressive) return "aggressive";
        return "peacefully";
    }

    public void setHuman(Human human){
        this.human = human;
    }

    @Override
    public String toString() {
        return "FoodBehaviour{" + "\n" +
                "aggressive=" + aggressive + "\n" +
                ", peacefully=" + peacefully + "\n" +
                '}';
    }

    public FoodBehaviour createVariation() {
        double r = (double)Math.round(random.nextDouble(0,0.2d) * 100) / 100;
        double[] chances = new double[]{this.aggressive, this.peacefully};
        int index1 = random.nextInt(0,chances.length);
        int index2 = random.nextInt(0,chances.length);
        while(index1 == index2){
            index2 = random.nextInt(0,chances.length);
        }

        double distanceToOne = 1 - chances[index1];
        double distanceToZero = chances[index2];

        double maxR = Math.min(distanceToOne, distanceToZero);
        if(r > maxR) r = maxR;

        chances[index1] += r;
        chances[index2] -= r;

        for(int i = 0; i < chances.length; i++){
            chances[i] = clamp(chances[i],0,1);
        }

        double newAggressive = chances[0];
        double newPeacefully = chances[1];

        return new FoodBehaviour(this.human, newAggressive, newPeacefully);
    }
}
