package admin.ui.management;

import dto.definition.action.ActionInfoDTO;

public class RuleTreeNode {
    private ActionInfoDTO actionInfoDTO;
    private String ruleName;
    private Double probabilityToActivate;
    private Integer ticksToActivate;
    private Boolean isActivation = false;

    public RuleTreeNode() {}

    public RuleTreeNode(String ruleName) {
        this.ruleName = ruleName;
    }

    public RuleTreeNode(ActionInfoDTO actionInfoDTO) {
        this.actionInfoDTO = actionInfoDTO;
    }

    public RuleTreeNode(Double probabilityToActivate, Integer ticksToActivate) {
        this.probabilityToActivate = probabilityToActivate;
        this.ticksToActivate = ticksToActivate;
        isActivation = true;
    }

    public Boolean isActivation() {
        return isActivation;
    }

    public Double getProbabilityToActivate() {
        return probabilityToActivate;
    }

    public Integer getTicksToActivate() {
        return ticksToActivate;
    }

    public ActionInfoDTO getActionInfoDTO() {
        return actionInfoDTO;
    }

    @Override
    public String toString() {
        if (actionInfoDTO != null) {
            return actionInfoDTO.getType();
        } else if (ruleName != null) {
            return ruleName;
        } else if (ticksToActivate != null || probabilityToActivate != null) {
            return "activation";
        } else {
            return "actions";
        }
    }
}
