package dto.definition;

import dto.definition.action.ActionInfoDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RuleInfoDTO {
    private String name;
    private double probabilityToActivate;
    private int ticksToActivate;
    private final List<ActionInfoDTO> actions;

    public RuleInfoDTO() {
        actions = new ArrayList<>();
        probabilityToActivate = 1;
        ticksToActivate = 1;
    }

    public void addAction(ActionInfoDTO actionInfoDTO) {
        actions.add(actionInfoDTO);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getProbabilityToActivate() {
        return probabilityToActivate;
    }

    public void setProbabilityToActivate(double probabilityToActivate) {
        this.probabilityToActivate = probabilityToActivate;
    }

    public int getTicksToActivate() {
        return ticksToActivate;
    }

    public void setTicksToActivate(int ticksToActivate) {
        this.ticksToActivate = ticksToActivate;
    }

    public List<ActionInfoDTO> getActions() {
        return actions;
    }
}
