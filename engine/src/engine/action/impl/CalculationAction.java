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

public class CalculationAction extends AbstractAction {
    private final String propertyName;
    private final ExpressionImpl expressionReader;
    private ActionType calcType;
    private String arg1;
    private String arg2;

    public CalculationAction(EntityDefinition entityDefinition, String propertyName) {
        super(ActionType.CALCULATION, entityDefinition);
        this.propertyName = propertyName;
        expressionReader = new ExpressionImpl(ActionType.CALCULATION);
    }

    public void setCalcType(ActionType actionType) {
        calcType = actionType;
    }

    public void addArgs(String arg1, String arg2) {
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    @Override
    public void invoke(Context context) {
        setPrimaryInstance(context, expressionReader);
        if(getPrimaryEntityInstance() == null){
            return;
        }

        PropertyInstance propertyInstance = getPrimaryEntityInstance().getPropertyInstanceByName(propertyName);
        Object evaluatedArg1 = expressionReader.readExpression(context, propertyInstance, arg1);
        Object evaluatedArg2 = expressionReader.readExpression(context, propertyInstance, arg2);
        if ((evaluatedArg1 == null || evaluatedArg2 == null) && context.isInstanceMissing()) {
            context.setInstanceMissing(false);
            return;
        }

        if (PropertyType.FLOAT.equals(propertyInstance.getPropertyDefinition().getType())) {
            invokeFloatAction(context, evaluatedArg1, evaluatedArg2);
        } else {
            throw new IllegalArgumentException("calculation action can't operate on non-number property " + propertyName);
        }

    }

    private void invokeFloatAction(Context context, Object evaluatedArg1, Object evaluatedArg2) {
        PropertyInstance propertyInstance = context.getPrimaryEntityInstance().getPropertyInstanceByName(propertyName);
        FloatPropertyDefinition floatPropertyDefinition = (FloatPropertyDefinition) propertyInstance.getPropertyDefinition();
        Float x = (Float) evaluatedArg1;
        Float y = (Float) evaluatedArg2;
        Float res;

        if (calcType == ActionType.DIVIDE) {
            if (y == 0) {
                throw new ArithmeticException("can't divide by zero in " + getActionType().name().toLowerCase() + " action type");
            }
            res = x / y;

        } else {
            res = x * y;
        }

        res = floatPropertyDefinition.validateValueToUpdate(res);
        propertyInstance.updateValue(res, context.getCurrentTick());
    }

    @Override
    public ActionInfoDTO getActionInfo() {
        ActionInfoDTO actionInfoDTO = new ActionInfoDTO();

        if(getSecondaryEntityContext() != null){
            actionInfoDTO.setSecondaryEntity(getSecondaryEntityContext().getName());
        }
        actionInfoDTO.setMainEntity(getPrimaryEntityContext().getName());
        actionInfoDTO.setArg1(arg1);
        actionInfoDTO.setArg2(arg2);
        actionInfoDTO.setCalcType(calcType.name());
        actionInfoDTO.setProperty(propertyName);
        actionInfoDTO.setType(ActionType.CALCULATION.name());

        return actionInfoDTO;
    }

    @Override
    public String toString() {
        return "Calculation Action {for property " + propertyName + ", arg1 = " + arg1 + ", arg2 = " + arg2 + " => calculate : " + propertyName + " = arg1 " + calcType + " arg2}";
    }
}
