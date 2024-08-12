package engine.action.impl.condition;

import engine.action.api.ActionType;
import engine.action.api.ConditionOperator;
import engine.definition.entity.EntityDefinition;
import engine.definition.property.api.PropertyDefinition;
import engine.definition.property.api.PropertyType;
import engine.definition.property.impl.BooleanPropertyDefinition;
import engine.definition.property.impl.FloatPropertyDefinition;
import engine.definition.property.impl.StringPropertyDefinition;
import engine.definition.value.generator.fixed.FixedValueGenerator;
import engine.execution.context.Context;
import engine.execution.instance.entity.EntityInstance;
import engine.execution.instance.property.PropertyInstance;
import engine.execution.instance.property.PropertyInstanceImpl;
import engine.expression.ExpressionImpl;

public class Condition {
    private ExpressionImpl expressionReader;
    private String valueExpression;
    private String propertyNameExpression;
    private ConditionOperator conditionOperator;
    private EntityDefinition primaryEntity;
    private EntityInstance primaryEntityInstance;


    public Condition() {
    }

    public Condition(EntityDefinition primaryEntity, String propertyName, String conditionOperator, String value) {
        this.primaryEntity = primaryEntity;
        this.valueExpression = value;
        this.propertyNameExpression = propertyName;
        this.conditionOperator = stringToOperator(conditionOperator);
        expressionReader = new ExpressionImpl(ActionType.CONDITION);
    }

    private ConditionOperator stringToOperator(String operator) {
        ConditionOperator res;

        switch (operator) {
            case "lt":
                res = ConditionOperator.LT;
                break;
            case "bt":
                res = ConditionOperator.BT;
                break;
            case "=":
                res = ConditionOperator.EQUAL;
                break;
            case "!=":
                res = ConditionOperator.UNEQUAL;
                break;
            default:
                throw new IllegalStateException("invalid condition operator: " + operator);
        }

        return res;
    }


    public boolean isConditionTrue(Context context) {
        Object res;
        PropertyInstance propertyInstance;
        if(context.getPrimaryEntityInstance().getEntityDefinition().getName().equals(primaryEntity.getName())){
            primaryEntityInstance = context.getPrimaryEntityInstance();
        } else if(context.getPrimaryEntityInstance() != null && context.getSecondaryEntityInstance().getEntityDefinition().getName().equals(primaryEntity.getName())){
            primaryEntityInstance = context.getSecondaryEntityInstance();
        }
        expressionReader.setPrimaryEntity(primaryEntityInstance);
        res = expressionReader.getFunctionValue(context, null, propertyNameExpression);


        if (res == null && !context.isInstanceMissing()) {
            res = expressionReader.getEntityProperty(primaryEntityInstance, null, propertyNameExpression);
            if (res == null) {
                propertyInstance = createPropertyInstance(propertyNameExpression);
            } else {
                propertyInstance = primaryEntityInstance.getPropertyInstanceByName(propertyNameExpression);
            }
        } else {
           propertyInstance = createPropertyInstance(res);
        }

        if(!context.isInstanceMissing()) {
            return handlePropertyValue(context, propertyInstance);
        } else {
            return false;
        }
    }

    private PropertyInstance createPropertyInstance(Object res){
        PropertyInstance propertyInstance;

        if (res instanceof String) {
            FixedValueGenerator<String> stringFixedValueGenerator = new FixedValueGenerator<>((String) res);
            PropertyDefinition propertyDefinition = new StringPropertyDefinition("string", stringFixedValueGenerator);
            propertyInstance = new PropertyInstanceImpl(propertyDefinition, stringFixedValueGenerator.generateValue());
        } else if (res instanceof Float) {
            FixedValueGenerator<Float> floatFixedValueGenerator = new FixedValueGenerator<>((Float) res);
            PropertyDefinition propertyDefinition = new FloatPropertyDefinition("float", floatFixedValueGenerator);
            propertyInstance = new PropertyInstanceImpl(propertyDefinition, floatFixedValueGenerator.generateValue());

        } else {
            FixedValueGenerator<Boolean> booleanFixedValueGenerator = new FixedValueGenerator<>((Boolean) res);
            PropertyDefinition propertyDefinition = new BooleanPropertyDefinition("boolean", booleanFixedValueGenerator);
            propertyInstance = new PropertyInstanceImpl(propertyDefinition, booleanFixedValueGenerator.generateValue());
        }

        return propertyInstance;
    }

    private boolean handlePropertyValue(Context context, PropertyInstance propertyInstance) {
        boolean res;


        if (PropertyType.FLOAT.equals(propertyInstance.getPropertyDefinition().getType())) {
            res = invokeFloatAction(context, propertyInstance);
        } else if (PropertyType.STRING.equals(propertyInstance.getPropertyDefinition().getType())) {
            res = invokeStringAction(propertyInstance, context);
        } else if (PropertyType.BOOLEAN.equals(propertyInstance.getPropertyDefinition().getType())) {
            res = invokeBooleanAction(propertyInstance, context);
        } else {
            throw new IllegalArgumentException("invalid input for set action");
        }

        return res;
    }

    private boolean invokeStringAction(PropertyInstance propertyInstance, Context context) {
        boolean res;
        String exp = (String) expressionReader.readExpression(context, propertyInstance, valueExpression);

        if (conditionOperator == ConditionOperator.EQUAL) {
            res = propertyInstance.getValue().equals(exp);
        } else if (conditionOperator == ConditionOperator.UNEQUAL) {
            res = !propertyInstance.getValue().equals(exp);
        } else {
            throw new IllegalArgumentException("cannot perform with " + conditionOperator + " operator on string type");
        }

        return res;
    }

    private boolean invokeBooleanAction(PropertyInstance propertyInstance, Context context) {
        boolean value = (boolean) expressionReader.readExpression(context, propertyInstance, valueExpression);
        boolean res;

        if (conditionOperator == ConditionOperator.EQUAL) {
            res = propertyInstance.getValue().equals(value);
        } else if (conditionOperator == ConditionOperator.UNEQUAL) {
            res = !propertyInstance.getValue().equals(value);
        } else {
            throw new IllegalArgumentException("cannot perform with " + conditionOperator + " operator on boolean type");
        }

        return res;
    }

    private boolean invokeFloatAction(Context context, PropertyInstance propertyInstance) {
        boolean res = false;
        Float propertyValue = PropertyType.FLOAT.convert(propertyInstance.getValue());
        Float x = (Float) expressionReader.readExpression(context, propertyInstance, valueExpression);

        switch (conditionOperator) {
            case BT:
                res = propertyValue > x;
                break;
            case LT:
                res = propertyValue < x;
                break;
            case EQUAL:
                res = propertyValue.equals(x);
                break;
            case UNEQUAL:
                res = !propertyValue.equals(x);
                break;
        }

        return res;
    }

    public String getValueExpression() {
        return valueExpression;
    }

    public String getPropertyNameExpression() {
        return propertyNameExpression;
    }

    public ConditionOperator getConditionOperator() {
        return conditionOperator;
    }

    public int getConditionCount() {
        return 1;
    }

    public EntityDefinition getPrimaryEntity() {
        return primaryEntity;
    }

    @Override
    public String toString() {
        return "Condition(" + propertyNameExpression + " " + conditionOperator + " " + valueExpression + ")";
    }

    public void validate(EntityDefinition primaryEntity, EntityDefinition secondaryEntity) {
        boolean term1 = !this.primaryEntity.getName().equals(primaryEntity.getName());
        boolean term2 = ((secondaryEntity == null || (!this.primaryEntity.getName().equals(secondaryEntity.getName()))));
        if (term1 && term2) {
            throw new IllegalArgumentException(this.primaryEntity.getName() + " in condition not in context");
        }
    }

}
