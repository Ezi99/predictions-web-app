package engine.expression;

import engine.action.api.ActionType;
import engine.definition.entity.EntityDefinition;
import engine.definition.environment.api.EnvVariablesManager;
import engine.definition.property.api.PropertyDefinition;
import engine.definition.property.api.PropertyType;
import engine.execution.context.Context;
import engine.execution.instance.entity.EntityInstance;
import engine.execution.instance.property.PropertyInstance;

import java.util.Random;
import java.util.regex.Pattern;

public class ExpressionImpl implements Expression {

    private final Random random = new Random();
    private final ActionType actionType;
    private EntityInstance primaryEntity;

    public ExpressionImpl() {
        actionType = null;
    }

    public ExpressionImpl(ActionType actionType) {
        this.actionType = actionType;
    }

    @Override
    public Object readExpression(Context context, PropertyInstance propertyInstance, String expression) {
        Object res;
        PropertyType instancePropertyType = null;

        if (propertyInstance != null) {
            instancePropertyType = propertyInstance.getPropertyDefinition().getType();
        }

        res = getFunctionValue(context, propertyInstance, expression);
        if (res == null && !context.isInstanceMissing()) {
            res = getEntityProperty(primaryEntity, instancePropertyType, expression);
            if (res == null) {
                res = getFreeValue(instancePropertyType, expression);
                if (res == null && !context.isInstanceMissing()) {
                    throwExceptionMessage("invalid input");
                }
            }
        }

        return res;
    }

    @Override
    public void setPrimaryEntity(EntityInstance primaryEntity) {
        this.primaryEntity = primaryEntity;
    }

    @Override
    public Boolean readBooleanExpression(String expression) {
        Boolean res = null;

        if (expression.equalsIgnoreCase("true")) {
            res = true;
        } else if (expression.equalsIgnoreCase("false")) {
            res = false;
        } else {
            throwExceptionMessage(expression + " is an invalid value for boolean property");
        }

        return res;
    }

    @Override
    public Object getFreeValue(PropertyType instancePropertyType, String expression) {
        try {
            if (instancePropertyType == null || instancePropertyType == PropertyType.STRING) {
                return expression;
            }

            if (PropertyType.INTEGER.equals(instancePropertyType)) {
                return Integer.parseInt(expression);
            } else if (PropertyType.FLOAT.equals(instancePropertyType)) {
                return Float.parseFloat(expression);
            } else if (PropertyType.BOOLEAN.equals(instancePropertyType)) {
                if (expression.equals("true")) {
                    return true;
                } else if (expression.equals("false")) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            throwExceptionMessage(expression + " is invalid to work with " + instancePropertyType + " property");
        }

        return null;
    }

    public Object getEntityProperty(EntityInstance entityInstance, PropertyType instancePropertyType, String expression) {
        Object res = null;
        PropertyInstance otherPropertyInstance;

        try {
            otherPropertyInstance = entityInstance.getPropertyInstanceByName(expression);
        } catch (IllegalArgumentException e) {
            otherPropertyInstance = null;
        }

        if (otherPropertyInstance != null) {
            if (instancePropertyType == PropertyType.INTEGER && otherPropertyInstance.getPropertyDefinition().getType().equals(PropertyType.FLOAT)) {
                throwExceptionMessage("cannot convert float to int");
            } else if (instancePropertyType != null && instancePropertyType != otherPropertyInstance.getPropertyDefinition().getType()) {
                throwExceptionMessage("arguments not same type");
            }

            res = otherPropertyInstance.getValue();
        }

        return res;
    }

    public Object getFunctionValue(Context context, PropertyInstance propertyInstance, String expression) {
        String functionInput = extractInput(expression);
        Object res = null;
        PropertyType instancePropertyType = null;

        if (propertyInstance != null) {
            instancePropertyType = propertyInstance.getPropertyDefinition().getType();
        }

        if (functionInput != null) {
            if (expression.contains("percent")) {
                res = handlePercentFunction(context, propertyInstance, functionInput);
            } else if (expression.contains("environment")) {
                res = context.getEnvironmentVariable(functionInput).getValue();
                if (instancePropertyType != null) {
                    checkIfFunctionValueValid(res, instancePropertyType);
                }
            } else if (expression.contains("random")) {
                try {
                    res = Integer.parseInt(functionInput);
                    res = (float) random.nextInt((int) res + 1);
                    if (instancePropertyType != null) {
                        checkIfFunctionValueValid(res, instancePropertyType);
                    }
                } catch (NumberFormatException e) {
                    throwExceptionMessage("didn't get number in random function");
                }
            } else if (expression.contains("evaluate")) {
                res = handleEvaluate(context, instancePropertyType, functionInput);
            } else if (expression.contains("ticks")) {
                res = handleTicksFunction(context, functionInput);
            }
        }

        return res;
    }

    private Object handleTicksFunction(Context context, String functionInput) {
        String[] parts = functionInput.split("\\.");
        Object res = null;
        PropertyInstance propertyInstance;

        if (parts.length == 2) {
            String entityName = parts[0];
            String propertyName = parts[1];
            if (entityName.equals(context.getPrimaryEntityInstance().getEntityDefinition().getName())) {
                propertyInstance = context.getPrimaryEntityInstance().getPropertyInstanceByName(propertyName);
                res = context.getCurrentTick() - propertyInstance.getLastValueUpdate();
            } else if (context.getSecondaryEntityDefinition() != null && context.getSecondaryEntityDefinition().getName().equals(entityName)) {
                if (context.getSecondaryEntityInstance() != null) {
                    propertyInstance = context.getSecondaryEntityInstance().getPropertyInstanceByName(propertyName);
                    res = context.getCurrentTick() - propertyInstance.getLastValueUpdate();
                } else {
                    context.setInstanceMissing(true);
                }

            } else {
                throwExceptionMessage(entityName + " in ticks function not in context");
            }
        }

        if (res == null && !context.isInstanceMissing()) {
            throwExceptionMessage("invalid ticks function");
        }

        return res;
    }

    private Object handlePercentFunction(Context context, PropertyInstance propertyInstance, String functionInput) {
        String[] parts = functionInput.split(",");
        Object res = null;
        if (parts.length == 2) {
            Object arg1 = readExpression(context, propertyInstance, parts[0]);
            Object arg2 = readExpression(context, propertyInstance, parts[1]);
            if (arg1 instanceof Float && arg2 instanceof Float) {
                Float number = (Float) arg1;
                Float percentage = (Float) arg2;
                res = number * (percentage / 100);
            }
        }

        if (res == null) {
            throwExceptionMessage("invalid percent function");
        }

        return res;
    }

    void checkIfFunctionValueValid(Object res, PropertyType instancePropertyType) {
        if (res instanceof Float && (instancePropertyType != PropertyType.FLOAT)) {
            throwExceptionMessage("cannot perform action with " + res + " value on " + instancePropertyType + " type property");
        }
        if (res instanceof String && (instancePropertyType != PropertyType.STRING)) {
            throwExceptionMessage("cannot perform action with " + res + " on " + instancePropertyType + " type property");
        }
        if (res instanceof Boolean && (instancePropertyType != PropertyType.BOOLEAN)) {
            throwExceptionMessage("cannot perform action with " + res + " value on " + instancePropertyType + " type property");
        }
        if (res instanceof Integer && (instancePropertyType != PropertyType.FLOAT && instancePropertyType != PropertyType.INTEGER)) {
            throwExceptionMessage("cannot perform action with " + res + " value on " + instancePropertyType + " type");
        }
    }

    private Object handleEvaluate(Context context, PropertyType instancePropertyType, String functionInput) {
        String[] parts = functionInput.split("\\.");
        Object res = null;
        if (parts.length == 2) {
            String entityName = parts[0];
            String propertyName = parts[1];

            if (entityName.equals(context.getPrimaryEntityInstance().getEntityDefinition().getName())) {
                res = getEntityProperty(context.getPrimaryEntityInstance(), instancePropertyType, propertyName);
            } else if (context.getSecondaryEntityDefinition() != null && context.getSecondaryEntityDefinition().getName().equals(entityName)) {
                if (context.getSecondaryEntityInstance() != null) {
                    res = getEntityProperty(context.getSecondaryEntityInstance(), instancePropertyType, propertyName);
                } else {
                    context.setInstanceMissing(true);
                }
            } else {
                throwExceptionMessage(entityName + " in evaluate function not in context");
            }
        }

        if (res == null && !context.isInstanceMissing()) {
            throwExceptionMessage("invalid evaluate function");
        }

        return res;
    }

    public String extractInput(String input) {
        int startIndex = input.indexOf('(');
        int endIndex = input.lastIndexOf(')');
        String pattern1 = "percent\\((.*)\\)";
        String pattern2 = "environment\\((.*)\\)";
        String pattern3 = "random\\((.*)\\)";
        String pattern4 = "evaluate\\((.*)\\)";
        String pattern5 = "ticks\\((.*)\\)";

        if (Pattern.matches(pattern1, input)) {
            return input.substring(startIndex + 1, endIndex);
        }
        if (Pattern.matches(pattern2, input)) {
            return input.substring(startIndex + 1, endIndex);
        }
        if (Pattern.matches(pattern3, input)) {
            return input.substring(startIndex + 1, endIndex);
        }
        if (Pattern.matches(pattern4, input)) {
            return input.substring(startIndex + 1, endIndex);
        }
        if (Pattern.matches(pattern5, input)) {
            return input.substring(startIndex + 1, endIndex);
        }

        return null;
    }

    public void validateNumericAction(EnvVariablesManager envVariablesManager, EntityDefinition primaryEntity, EntityDefinition secondaryEntity, PropertyType propertyType, String value) {
        if (propertyType == PropertyType.INTEGER || propertyType == PropertyType.FLOAT) {
            if (isfValidFunction(envVariablesManager, value, primaryEntity, secondaryEntity) ||
                    isValidEntityProperty(primaryEntity, value) || isValidEntityProperty(secondaryEntity, value) || isValidFreeValue(value)) {

            } else throwExceptionMessage("invalid numeric expression with '" + value + "' value");
        } else {
            throwExceptionMessage("can't perform numeric action on " + propertyType + " type");
        }
    }


    private void throwExceptionMessage(String message) {
        if (actionType != null) {
            message = "{" + actionType.name().toLowerCase() + " Action}" + message;
        }

        throw new IllegalArgumentException(message);
    }

    private boolean isfValidFunction(EnvVariablesManager envVariablesManager, String value, EntityDefinition primaryEntity, EntityDefinition secondaryEntity) {
        String functionInput = extractInput(value);
        PropertyType propertyType;
        boolean res = false;

        if (functionInput != null) {
            res = true;
            if (value.contains("percent")) {
                String[] parts = functionInput.split(",");
                if (parts.length == 2) {
                    boolean arg1 = isfValidFunction(envVariablesManager, parts[0], primaryEntity, secondaryEntity);
                    boolean arg2 = isfValidFunction(envVariablesManager, parts[1], primaryEntity, secondaryEntity);
                    res = arg1 && arg2;
                } else res = false;
            } else if (value.contains("environment")) {
                propertyType = envVariablesManager.getEnvVariable(functionInput).getType();

                if ((propertyType != PropertyType.INTEGER) && (propertyType != PropertyType.FLOAT)) {
                    res = false;
                }
            } else if (value.contains("evaluate")) {
                String[] parts = functionInput.split("\\.");
                if (parts.length == 2) {
                    if(parts[0].equals(primaryEntity.getName())){
                        res = isValidEntityProperty(primaryEntity, parts[1]);
                    } else if (secondaryEntity != null && parts[0].equals(secondaryEntity.getName())){
                        res = isValidEntityProperty(secondaryEntity, parts[1]);
                    } else {
                        throw new IllegalArgumentException(value + " not in context");
                    }
                }
            }
        }

        return res;
    }

    private boolean isValidEntityProperty(EntityDefinition entityDefinition, String value) {
        boolean res = true;
        PropertyDefinition propertyDefinition = null;

        if(entityDefinition != null) {
            propertyDefinition = entityDefinition.getPropertyDefinition(value);
        }

        if (propertyDefinition != null) {
            PropertyType propertyType = propertyDefinition.getType();
            if (propertyType != PropertyType.INTEGER && propertyType != PropertyType.FLOAT) {
                res = false;
            }
        } else {
            res = false;
        }

        return res;
    }

    private boolean isValidFreeValue(String value) {

        try {
            Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}

