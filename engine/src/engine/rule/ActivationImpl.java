package engine.rule;

import java.util.Random;

public class ActivationImpl implements Activation {

    private int tickToActivate;
    private double probability;
    private int counter;
    private final Random random = new Random();

    public ActivationImpl() {
        this(1, 1);
    }

    public ActivationImpl(int tickToActivate, float probability) {
        this.tickToActivate = tickToActivate;
        this.probability = probability;
        counter = 0;
    }

    @Override
    public void setTicks(int tickToActivate){
        this.tickToActivate = tickToActivate;
    }

    @Override
    public int getTicks() {
        return tickToActivate;
    }

    @Override
    public void setProbability(double probability){
        this.probability = probability;
    }

    @Override
    public double getProbability() {
        return probability;
    }

    @Override
    public boolean isActive() {
        boolean res = false;
        counter++;

        if (counter == tickToActivate) {
            res = checkProbability();
            counter = 0;
        }

        return res;
    }

    private boolean checkProbability() {
        boolean res = false;
        double num = random.nextDouble();

        if (probability != 0 && probability >= num) {
            res = true;
        }

        return res;
    }
}
