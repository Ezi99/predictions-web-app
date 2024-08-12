package engine.action.api;

import dto.definition.action.ActionInfoDTO;
import engine.action.impl.condition.Condition;
import engine.execution.context.Context;
import engine.definition.entity.EntityDefinition;
import engine.expression.Expression;

public interface Action {
    void invoke(Context context);
    ActionType getActionType();
    EntityDefinition getPrimaryEntityContext();
    ActionInfoDTO getActionInfo();
    EntityDefinition getSecondaryEntityContext();

    void setSecondaryEntityContext(EntityDefinition entityDefinition);

    void setConditionToActivateSecondEntity(Condition condition);

    Condition getConditionsToActivate();

    boolean isConditionMet(Context context);

    void setCount(String count);

    int getSelectionCount();

    void setPrimaryInstance(Context context, Expression expression);

    void validate(EntityDefinition primaryEntity, EntityDefinition secondaryEntity);
}
