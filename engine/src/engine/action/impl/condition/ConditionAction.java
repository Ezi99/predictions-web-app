package engine.action.impl.condition;

import dto.definition.action.ActionInfoDTO;
import engine.action.api.AbstractAction;
import engine.action.api.Action;
import engine.action.api.ActionType;
import engine.definition.entity.EntityDefinition;
import engine.execution.context.Context;
import java.util.ArrayList;
import java.util.List;

public class ConditionAction extends AbstractAction {
    private final Condition condition;
    private final List<Action> thenActions;
    private final List<Action> elseActions;

    private boolean hasElse;

    public ConditionAction(EntityDefinition primaryEntityDefinition, Condition condition) {
        super(ActionType.CONDITION, primaryEntityDefinition);
        this.condition = condition;
        thenActions = new ArrayList<>();
        elseActions = new ArrayList<>();
        hasElse = false;
    }

    public void addThenAction(Action action) {
        thenActions.add(action);
    }

    public void addElseAction(Action action) {
        elseActions.add(action);
        hasElse = true;
    }

    @Override
    public void invoke(Context context) {

        if (condition.isConditionTrue(context)) {
            for (Action thenAction : thenActions) {
                if (context.checkIfToInvoke(thenAction.getPrimaryEntityContext().getName())) {
                    thenAction.invoke(context);
                }
            }
        } else {
            if (hasElse) {
                for (Action elseAction : elseActions) {
                    if (context.checkIfToInvoke(elseAction.getPrimaryEntityContext().getName())) {
                        elseAction.invoke(context);
                    }
                }
            }
        }

        context.setInstanceMissing(false);
    }

    @Override
    public ActionInfoDTO getActionInfo() {
        ActionInfoDTO actionInfoDTO = new ActionInfoDTO();

        if(getSecondaryEntityContext() != null){
            actionInfoDTO.setSecondaryEntity(getSecondaryEntityContext().getName());
        }
        actionInfoDTO.setConditionOperator(condition.getConditionOperator().name());
        actionInfoDTO.setConditionsCount(condition.getConditionCount());
        actionInfoDTO.setValue(condition.getValueExpression());
        actionInfoDTO.setType(ActionType.CONDITION.name());
        actionInfoDTO.setConditionProperty(condition.getPropertyNameExpression());
        actionInfoDTO.setMainEntity(getPrimaryEntityContext().getName());
        actionInfoDTO.setThenActionCount(thenActions.size());
        actionInfoDTO.setElseActionCount(elseActions.size());

        return actionInfoDTO;
    }

    @Override
    public String toString() {
        return "Condition Action";
    }

    public void validateConditions() {
        condition.validate(getPrimaryEntityContext(), getSecondaryEntityContext());
    }
}
