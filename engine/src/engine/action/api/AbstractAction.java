package engine.action.api;

import engine.action.impl.condition.Condition;
import engine.definition.entity.EntityDefinition;
import engine.execution.context.Context;
import engine.execution.instance.entity.EntityInstance;
import engine.expression.Expression;

public abstract class AbstractAction implements Action {

    private final ActionType actionType;
    private final EntityDefinition primaryEntity;
    private EntityDefinition secondaryEntity;
    private Condition conditionsToActivate;
    private int selectionCount;
    private EntityInstance primaryEntityInstance;


    protected AbstractAction(ActionType actionType, EntityDefinition entityDefinition) {
        this.actionType = actionType;
        this.primaryEntity = entityDefinition;
        secondaryEntity = null;
        conditionsToActivate = null;
        primaryEntityInstance = null;
    }

    @Override
    public ActionType getActionType() {
        return actionType;
    }

    @Override
    public EntityDefinition getPrimaryEntityContext() {
        return primaryEntity;
    }

    @Override
    public EntityDefinition getSecondaryEntityContext() {
        return secondaryEntity;
    }

    @Override
    public void setSecondaryEntityContext(EntityDefinition entityDefinition) {
        secondaryEntity = entityDefinition;
    }

    @Override
    public void setConditionToActivateSecondEntity(Condition condition) {
        condition.validate(primaryEntity, secondaryEntity);
        conditionsToActivate = condition;
    }

    @Override
    public Condition getConditionsToActivate() {
        return conditionsToActivate;
    }

    @Override
    public boolean isConditionMet(Context context) {
        return conditionsToActivate.isConditionTrue(context);
    }

    @Override
    public void setCount(String count) {
        if (count.equals("ALL")) {
        } else {
            try {
                selectionCount = Integer.parseInt(count);
            } catch (Exception e) {
                throw new IllegalArgumentException(count + " is invalid for selection count");
            }
        }
    }

    @Override
    public int getSelectionCount() {
        return selectionCount;
    }

    @Override
    public void setPrimaryInstance(Context context, Expression expression) {
        if (context.getPrimaryEntityInstance().getEntityDefinition().getName().equals(primaryEntity.getName())) {
            primaryEntityInstance = context.getPrimaryEntityInstance();
        } else {
            primaryEntityInstance = context.getSecondaryEntityInstance();
        }

        if(secondaryEntity != null){
            context.setSecondaryEntityDefinition(secondaryEntity);
        }

        if (expression != null) {
            expression.setPrimaryEntity(primaryEntityInstance);
        }
    }

    public EntityInstance getPrimaryEntityInstance() {
        return primaryEntityInstance;
    }

    @Override
    public void validate(EntityDefinition primaryEntity, EntityDefinition secondaryEntity) {
        boolean term1 = !this.primaryEntity.getName().equals(primaryEntity.getName());
        boolean term2 = ((secondaryEntity == null || (!this.primaryEntity.getName().equals(secondaryEntity.getName()))));
        if (term1 && term2) {
            throw new IllegalArgumentException("entity in action not in context");
        }
    }
}
