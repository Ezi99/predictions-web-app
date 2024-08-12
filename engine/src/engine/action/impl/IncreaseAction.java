package engine.action.impl;

import dto.definition.action.ActionInfoDTO;
import engine.definition.property.api.PropertyType;
import engine.definition.property.impl.FloatPropertyDefinition;
import engine.execution.context.Context;
import engine.execution.instance.property.PropertyInstance;
import engine.action.api.AbstractAction;
import engine.action.api.ActionType;
import engine.definition.entity.EntityDefinition;
import engine.expression.ExpressionImpl;

public class IncreaseAction extends AbstractAction {

    private final String propertyName;
    private final String expression;
    private final ExpressionImpl expressionReader;

    public IncreaseAction(EntityDefinition entityDefinition, String propertyName, String expression) {
        super(ActionType.INCREASE, entityDefinition);
        this.propertyName = propertyName;
        this.expression = expression;
        expressionReader = new ExpressionImpl(ActionType.INCREASE);
    }

    @Override
    public void invoke(Context context) {
        setPrimaryInstance(context, expressionReader);
        if(getPrimaryEntityInstance() == null){
            return;
        }

        PropertyInstance propertyInstance = getPrimaryEntityInstance().getPropertyInstanceByName(propertyName);
        Object evaluatedExpression = expressionReader.readExpression(context, propertyInstance, expression);
        if (evaluatedExpression == null && context.isInstanceMissing()) {
            context.setInstanceMissing(false);
            return;
        }

        if (PropertyType.FLOAT.equals(propertyInstance.getPropertyDefinition().getType())) {
            invokeFloatAction(context, propertyInstance, evaluatedExpression);
        } else {
            throw new IllegalArgumentException("increase action can't operate on a none number property [" + propertyName);
        }
    }

    private void invokeFloatAction(Context context, PropertyInstance propertyInstance, Object evaluatedExpression) {
        Float propertyValue = PropertyType.FLOAT.convert(propertyInstance.getValue());
        Float x = (float) evaluatedExpression;
        float res = x + propertyValue;
        FloatPropertyDefinition floatPropertyDefinition = (FloatPropertyDefinition) propertyInstance.getPropertyDefinition();

        res = floatPropertyDefinition.validateValueToUpdate(res);
        propertyInstance.updateValue(res, context.getCurrentTick());
    }

    @Override
    public ActionInfoDTO getActionInfo() {
        ActionInfoDTO actionInfoDTO = new ActionInfoDTO();

        if(getSecondaryEntityContext() != null){
            actionInfoDTO.setSecondaryEntity(getSecondaryEntityContext().getName());
        }

        actionInfoDTO.setType(ActionType.INCREASE.name());
        actionInfoDTO.setExpression(expression);
        actionInfoDTO.setProperty(propertyName);
        actionInfoDTO.setMainEntity(getPrimaryEntityContext().getName());

        return actionInfoDTO;
    }

    @Override
    public String toString() {
        return "Increase Action {" + "increase " + propertyName + " by " + expression + "}";
    }
}
