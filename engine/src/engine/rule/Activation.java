package engine.rule;

public interface Activation {
    boolean isActive();

    void setTicks(int tickToActivate);

    int getTicks();

    void setProbability(double probability);

    double getProbability();
}
