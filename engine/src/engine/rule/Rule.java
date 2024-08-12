package engine.rule;

import engine.action.api.Action;

import java.util.List;

public interface Rule {
    String getName();
    Activation getActivation();
    List<Action> getActionsToPerform();
    void addAction(Action action);
    void setActivation(Activation activation);
}
