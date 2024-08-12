package engine.action.impl;

import dto.definition.action.ActionInfoDTO;
import engine.action.api.AbstractAction;
import engine.action.api.ActionType;
import engine.definition.entity.EntityDefinition;
import engine.definition.property.api.PropertyType;
import engine.definition.property.impl.FloatPropertyDefinition;
import engine.execution.context.Context;
import engine.execution.instance.property.PropertyInstance;
import engine.expression.ExpressionImpl;

public class SetAction extends AbstractAction {
    private final String propertyName;
    private final String expression;
    private final ExpressionImpl expressionReader;

    public SetAction(EntityDefinition entityDefinition, String propertyName, String expression) {
        super(ActionType.SET, entityDefinition);
        this.propertyName = propertyName;
        this.expression = expression;
        expressionReader = new ExpressionImpl(ActionType.SET);
    }

    @Override
    public void invoke(Context context) {
        setPrimaryInstance(context, expressionReader);
        if(getPrimaryEntityInstance() == null){
            return;
        }

        PropertyInstance propertyInstance = getPrimaryEntityInstance().getPropertyInstanceByName(propertyName);
        Object evaluatedExpression = expressionReader.readExpression(context, propertyInstance, expression);
        if(evaluatedExpression == null && context.isInstanceMissing()){
            context.setInstanceMissing(false);
            return;
        }

        if (PropertyType.FLOAT.equals(propertyInstance.getPropertyDefinition().getType())) {
            invokeFloatAction(context, propertyInstance, evaluatedExpression);
        } else if (PropertyType.STRING.equals(propertyInstance.getPropertyDefinition().getType())) {
            invokeStringAction(context, propertyInstance, evaluatedExpression);
        } else if (PropertyType.BOOLEAN.equals(propertyInstance.getPropertyDefinition().getType())) {
            invokeBooleanAction(propertyInstance, context, evaluatedExpression);
        } else {
            throw new IllegalArgumentException("invalid input for set action");
        }
    }

    private void invokeStringAction(Context context, PropertyInstance propertyInstance, Object evaluatedExpression) {
        String res = (String) evaluatedExpression;
        propertyInstance.updateValue(res, context.getCurrentTick());
    }

    private void invokeBooleanAction(PropertyInstance propertyInstance, Context context, Object evaluatedExpression) {
        boolean res = (boolean) evaluatedExpression;
        propertyInstance.updateValue(res, context.getCurrentTick());
    }

    private void invokeFloatAction(Context context, PropertyInstance propertyInstance, Object evaluatedExpression) {
        Float res = (Float) evaluatedExpression;
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
        actionInfoDTO.setType(ActionType.SET.name());
        actionInfoDTO.setExpression(expression);
        actionInfoDTO.setProperty(propertyName);
        actionInfoDTO.setMainEntity(getPrimaryEntityContext().getName());

        return actionInfoDTO;
    }


    @Override
    public String toString() {
        return "Set Action {" + "set " + propertyName + " to " + expression + " }";
    }

}
