package engine.world.loader;

import engine.action.api.Action;
import engine.action.api.ActionType;
import engine.action.api.ReplaceMode;
import engine.action.impl.*;
import engine.action.impl.condition.Condition;
import engine.action.impl.condition.ConditionAction;
import engine.action.impl.condition.MultipleCondition;
import engine.definition.entity.EntityDefinition;
import engine.definition.entity.EntityDefinitionImpl;
import engine.definition.environment.api.EnvVariablesManager;
import engine.definition.environment.impl.EnvVariableManagerImpl;
import engine.definition.property.api.PropertyDefinition;
import engine.definition.property.impl.BooleanPropertyDefinition;
import engine.definition.property.impl.FloatPropertyDefinition;
import engine.definition.property.impl.StringPropertyDefinition;
import engine.definition.value.generator.fixed.FixedValueGenerator;
import engine.definition.value.generator.random.impl.bool.RandomBooleanValueGenerator;
import engine.definition.value.generator.random.impl.numeric.RandomFloatGenerator;
import engine.definition.value.generator.random.impl.string.RandomStringGenerator;
import engine.execution.Termination;
import engine.execution.space.Grid;
import engine.expression.ExpressionImpl;
import engine.rule.Activation;
import engine.rule.ActivationImpl;
import engine.rule.Rule;
import engine.rule.RuleImpl;
import resource.generated.*;

import java.util.*;

public class PRDWorldLoader {
    ExpressionImpl expressionReader = new ExpressionImpl();
    private Map<String, EntityDefinition> entityDefinitionMap;
    private EnvVariablesManager envPropertiesDefinitions;

    public EnvVariablesManager loadEnvVariables(PRDWorld prdWorld) {
        Set<String> propertyNames = new HashSet<>();
        envPropertiesDefinitions = new EnvVariableManagerImpl();

        for (PRDEnvProperty prdEnvProperty : prdWorld.getPRDEnvironment().getPRDEnvProperty()) {
            PropertyDefinition propertyDefinition;
            String type = prdEnvProperty.getType().trim();
            PRDRange prdRange = prdEnvProperty.getPRDRange();
            String prdName = prdEnvProperty.getPRDName().trim();
            checkIfDuplicate(propertyNames, prdName, "environment properties contain " + prdName + " twice");

            propertyDefinition = loadRandValuePropertyDefinition(type, prdName, prdRange);
            envPropertiesDefinitions.addEnvironmentVariable(propertyDefinition);
        }

        return envPropertiesDefinitions;
    }

    public Map<String, EntityDefinition> loadEntities(PRDWorld prdWorld) {
        Set<String> propertyNames = new HashSet<>();
        entityDefinitionMap = new HashMap<>();

        for (PRDEntity prdEntity : prdWorld.getPRDEntities().getPRDEntity()) {
            EntityDefinition entityDefinition = new EntityDefinitionImpl(prdEntity.getName().trim());
            for (PRDProperty prdProperty : prdEntity.getPRDProperties().getPRDProperty()) {
                PropertyDefinition propertyDefinition;
                String type = prdProperty.getType().trim();
                PRDRange prdRange = prdProperty.getPRDRange();
                String prdName = prdProperty.getPRDName().trim();
                checkIfDuplicate(propertyNames, prdName, prdEntity.getName().trim() + " contains " + prdName + " property twice");
                PRDValue prdValue = prdProperty.getPRDValue();

                if (prdValue.isRandomInitialize()) {
                    propertyDefinition = loadRandValuePropertyDefinition(type, prdName, prdRange);
                } else {
                    propertyDefinition = loadFixedValuePropertyDefinition(type, prdName, prdRange, prdValue);
                }

                entityDefinition.addPropertyDefinition(propertyDefinition);
            }
            propertyNames.clear();
            entityDefinitionMap.put(entityDefinition.getName(), entityDefinition);
        }

        return entityDefinitionMap;
    }

    private PropertyDefinition loadFixedValuePropertyDefinition(String type, String prdName, PRDRange prdRange, PRDValue prdValue) {
        PropertyDefinition propertyDefinition;
        String value = prdValue.getInit();

        if (type.equals("string")) {
            propertyDefinition = new StringPropertyDefinition(prdName, new FixedValueGenerator<>(value));
        } else if (type.equals("boolean")) {
            propertyDefinition = new BooleanPropertyDefinition(prdName, new FixedValueGenerator<>(value.equals("true")));
        } else {
            propertyDefinition = loadFixedNumericPropertyDefinition(prdName, prdRange, prdValue);
        }

        return propertyDefinition;
    }

    private PropertyDefinition loadFixedNumericPropertyDefinition(String prdName, PRDRange prdRange, PRDValue prdValue) {
        double from;
        double to;
        PropertyDefinition propertyDefinition;
        Float value = Float.parseFloat(prdValue.getInit());
        FloatPropertyDefinition floatPropertyDefinition = new FloatPropertyDefinition(prdName, new FixedValueGenerator<>(value));

        if (prdRange != null) {
            from = prdRange.getFrom();
            to = prdRange.getTo();
            floatPropertyDefinition.setBoundary((float) from, (float) to);
        }

        propertyDefinition = floatPropertyDefinition;
        return propertyDefinition;
    }

    private PropertyDefinition loadRandValuePropertyDefinition(String type, String prdName, PRDRange prdRange) {
        PropertyDefinition propertyDefinition;

        if (type.equals("string")) {
            propertyDefinition = new StringPropertyDefinition(prdName, new RandomStringGenerator());
        } else if (type.equals("boolean")) {
            propertyDefinition = new BooleanPropertyDefinition(prdName, new RandomBooleanValueGenerator());
        } else {
            propertyDefinition = loadRandNumericPropertyDefinition(prdName, prdRange);
        }

        return propertyDefinition;
    }

    private PropertyDefinition loadRandNumericPropertyDefinition(String prdName, PRDRange prdRange) {
        double from;
        double to;
        PropertyDefinition propertyDefinition;
        RandomFloatGenerator randomFloatGenerator = new RandomFloatGenerator();
        FloatPropertyDefinition floatPropertyDefinition = new FloatPropertyDefinition(prdName, randomFloatGenerator);

        if (prdRange != null) {
            from = prdRange.getFrom();
            to = prdRange.getTo();
            randomFloatGenerator.setBoundary((float) from, (float) to);
            floatPropertyDefinition.setBoundary((float) from, (float) to);
        }

        propertyDefinition = floatPropertyDefinition;
        return propertyDefinition;
    }

    private void checkIfDuplicate(Set<String> propertyNames, String prdName, String exceptionMessage) {
        if (propertyNames.contains(prdName)) {
            throw new IllegalArgumentException(exceptionMessage);
        }
        propertyNames.add(prdName);
    }

    public List<Rule> loadRules(PRDWorld prdWorld) {
        List<Rule> rules = new ArrayList<>();

        for (PRDRule prdRule : prdWorld.getPRDRules().getPRDRule()) {
            Rule rule = new RuleImpl(prdRule.getName().trim());
            Activation activation = new ActivationImpl();

            if (prdRule.getPRDActivation() != null) {
                if (prdRule.getPRDActivation().getTicks() != null) {
                    activation.setTicks(prdRule.getPRDActivation().getTicks());
                }
                if (prdRule.getPRDActivation().getProbability() != null) {
                    activation.setProbability(prdRule.getPRDActivation().getProbability());
                }
            }

            for (PRDAction prdAction : prdRule.getPRDActions().getPRDAction()) {
                rule.addAction(loadAction(prdAction, null, null));
            }

            rule.setActivation(activation);
            rules.add(rule);
        }

        return rules;
    }

    private Action loadAction(PRDAction prdAction, EntityDefinition primaryDefinition, EntityDefinition secondaryDefinition) {
        Action action;

        switch (prdAction.getType().trim()) {
            case "increase":
            case "decrease":
                checkIfActionOrConditionValid(prdAction.getEntity(), prdAction.getProperty());
                action = loadDecreaseOrIncrease(prdAction, primaryDefinition, secondaryDefinition);
                break;
            case "calculation":
                checkIfActionOrConditionValid(prdAction.getEntity(), prdAction.getResultProp());
                action = loadCalculationAction(prdAction, primaryDefinition, secondaryDefinition);
                break;
            case "condition":
                action = loadConditionAction(prdAction);
                break;
            case "set":
                checkIfActionOrConditionValid(prdAction.getEntity(), prdAction.getProperty());
                action = new SetAction(entityDefinitionMap.get(prdAction.getEntity()),
                        prdAction.getProperty().trim(), prdAction.getValue().trim());
                break;
            case "replace":
                doesEntityExist(prdAction.getKill().trim());
                doesEntityExist(prdAction.getCreate().trim());
                action = loadReplaceAction(prdAction);
                break;
            case "proximity":
                doesEntityExist(prdAction.getPRDBetween().getSourceEntity().trim());
                doesEntityExist(prdAction.getPRDBetween().getTargetEntity().trim());
                action = loadProximityAction(prdAction);
                break;
            default:
                doesEntityExist(prdAction.getEntity().trim());
                action = new KillAction(entityDefinitionMap.get(prdAction.getEntity().trim()));
                break;
        }

        return action;
    }

    private Action loadDecreaseOrIncrease(PRDAction prdAction, EntityDefinition primaryContextDefinition, EntityDefinition secondaryContextDefinition) {
        Action action;
        EntityDefinition primaryEntityDefinition = entityDefinitionMap.get(prdAction.getEntity().trim());
        PropertyDefinition propertyDefinition = primaryEntityDefinition.getPropertyDefinition(prdAction.getProperty().trim());

        if (prdAction.getType().trim().equals("increase")) {
            action = new IncreaseAction(primaryEntityDefinition, prdAction.getProperty().trim(),
                    prdAction.getBy().trim());
        } else {
            action = new DecreaseAction(primaryEntityDefinition, prdAction.getProperty().trim(),
                    prdAction.getBy().trim());
        }

        if (primaryContextDefinition != null) {
            primaryEntityDefinition = primaryContextDefinition;
        }

        if (secondaryContextDefinition == null) {
            checkIfHasSecondaryEntity(prdAction, action);
            secondaryContextDefinition = action.getSecondaryEntityContext();
        }

        expressionReader.validateNumericAction(envPropertiesDefinitions, primaryEntityDefinition, secondaryContextDefinition, propertyDefinition.getType(), prdAction.getBy().trim());

        return action;
    }

    private void checkIfHasSecondaryEntity(PRDAction prdAction, Action action) {
        if (prdAction.getPRDSecondaryEntity() != null) {
            PRDAction.PRDSecondaryEntity secondaryEntity = prdAction.getPRDSecondaryEntity();
            PRDCondition prdCondition = secondaryEntity.getPRDSelection().getPRDCondition();
            doesEntityExist(secondaryEntity.getEntity());
            action.setSecondaryEntityContext(entityDefinitionMap.get(secondaryEntity.getEntity()));
            action.setCount(secondaryEntity.getPRDSelection().getCount());
            if (prdCondition != null) {
                action.setConditionToActivateSecondEntity(loadCondition(prdCondition));
            }
        }
    }

    private Action loadReplaceAction(PRDAction prdAction) {
        String kill = prdAction.getKill();
        String create = prdAction.getCreate();
        doesEntityExist(kill);
        doesEntityExist(create);
        String mode = prdAction.getMode();
        ReplaceMode replaceMode;

        if (mode.equals("scratch")) {
            replaceMode = ReplaceMode.SCRATCH;
        } else if (mode.equals("derived")) {
            replaceMode = ReplaceMode.DERIVED;
        } else {
            throw new IllegalArgumentException("invalid replace mode");
        }

        return new ReplaceAction(entityDefinitionMap.get(kill), create, replaceMode);
    }

    private Action loadProximityAction(PRDAction prdAction) {
        ProximityAction proximityAction;
        String sourceName = prdAction.getPRDBetween().getSourceEntity().trim();
        String targetName = prdAction.getPRDBetween().getTargetEntity().trim();
        doesEntityExist(sourceName);
        doesEntityExist(targetName);
        EntityDefinition source = entityDefinitionMap.get(sourceName);
        EntityDefinition target = entityDefinitionMap.get(targetName);
        String ofExpression = prdAction.getPRDEnvDepth().getOf();

        proximityAction = new ProximityAction(source, target, ofExpression);
        for (PRDAction prdActions : prdAction.getPRDActions().getPRDAction()) {
            Action action = loadAction(prdActions, source, target);
            action.validate(source, target);
            proximityAction.addAction(action);
        }

        return proximityAction;
    }

    private void checkIfActionOrConditionValid(String entityName, String entityProperty) {
        doesEntityExist(entityName);
        if (entityDefinitionMap.get(entityName).getPropertyDefinition(entityProperty.trim()) == null) {
            throw new IllegalArgumentException(entityName + " doesn't have the following property - " + entityProperty);
        }
    }

    private void doesEntityExist(String entityName) {
        if (!entityDefinitionMap.containsKey(entityName.trim())) {
            throw new IllegalArgumentException("such entity - '" + entityName + "' doesn't exist");
        }
    }

    private CalculationAction loadCalculationAction(PRDAction prdAction, EntityDefinition primaryContextDefinition, EntityDefinition secondaryContextDefinition) {
        String arg1, arg2;
        String result = prdAction.getResultProp();
        EntityDefinition primaryEntityDefinition = entityDefinitionMap.get(prdAction.getEntity().trim());

        CalculationAction calculationAction = new CalculationAction(primaryEntityDefinition, result);

        if (prdAction.getPRDDivide() != null) {
            arg1 = prdAction.getPRDDivide().getArg1().trim();
            arg2 = prdAction.getPRDDivide().getArg2().trim();
            calculationAction.setCalcType(ActionType.DIVIDE);
        } else {
            arg1 = prdAction.getPRDMultiply().getArg1().trim();
            arg2 = prdAction.getPRDMultiply().getArg2().trim();
            calculationAction.setCalcType(ActionType.MULTIPLY);
        }
        String entityName = prdAction.getEntity().trim();

        if (primaryContextDefinition != null) {
            primaryEntityDefinition = primaryContextDefinition;
        }

        if (secondaryContextDefinition == null) {
            checkIfHasSecondaryEntity(prdAction, calculationAction);
            secondaryContextDefinition = calculationAction.getSecondaryEntityContext();
        }
        PropertyDefinition propertyDefinition = entityDefinitionMap.get(entityName).getPropertyDefinition(result);
        expressionReader.validateNumericAction(envPropertiesDefinitions, primaryEntityDefinition,
                secondaryContextDefinition, propertyDefinition.getType(), arg1);
        expressionReader.validateNumericAction(envPropertiesDefinitions, primaryEntityDefinition,
                secondaryContextDefinition, propertyDefinition.getType(), arg2);
        calculationAction.addArgs(arg1, arg2);

        return calculationAction;
    }

    private ConditionAction loadConditionAction(PRDAction prdAction) {
        if (!entityDefinitionMap.containsKey(prdAction.getEntity().trim())) {
            throw new IllegalArgumentException("such entity - " + prdAction.getEntity().trim() + " doesn't exist");
        }

        ConditionAction conditionAction = new ConditionAction(entityDefinitionMap.get(prdAction.getEntity().trim()),
                loadCondition(prdAction.getPRDCondition()));

        checkIfHasSecondaryEntity(prdAction, conditionAction);

        for (PRDAction thenAction : prdAction.getPRDThen().getPRDAction()) {
            Action action = loadAction(thenAction, conditionAction.getPrimaryEntityContext(), conditionAction.getSecondaryEntityContext());
            action.validate(conditionAction.getPrimaryEntityContext(), conditionAction.getSecondaryEntityContext());
            conditionAction.addThenAction(action);
        }

        if (prdAction.getPRDElse() != null) {
            for (PRDAction elseAction : prdAction.getPRDElse().getPRDAction()) {
                Action action = loadAction(elseAction, conditionAction.getPrimaryEntityContext(), conditionAction.getSecondaryEntityContext());
                action.validate(conditionAction.getPrimaryEntityContext(), conditionAction.getSecondaryEntityContext());
                conditionAction.addElseAction(action);
            }
        }

        conditionAction.validateConditions();

        return conditionAction;
    }

    private Condition loadCondition(PRDCondition prdCondition) {
        Condition condition;

        if (prdCondition.getSingularity().trim().equals("multiple")) {
            condition = loadMultipleCondition(prdCondition);
        } else {
            condition = loadPrdConditionTree(prdCondition);
        }

        return condition;
    }

    private MultipleCondition loadMultipleCondition(PRDCondition prdConditions) {
        MultipleCondition multipleCondition = new MultipleCondition(prdConditions.getLogical().trim());
        for (PRDCondition prdCondition : prdConditions.getPRDCondition()) {
            multipleCondition.addCondition(loadPrdConditionTree(prdCondition));
        }
        return multipleCondition;
    }

    private Condition loadPrdConditionTree(PRDCondition root) {
        if (root == null) {
            return null;
        } else if (root.getSingularity().trim().equals("single")) {
            if (!entityDefinitionMap.containsKey(root.getEntity().trim())) {
                throw new IllegalArgumentException("such entity - " + root.getEntity().trim() + " doesn't exist");
            }
            return new Condition(entityDefinitionMap.get(root.getEntity().trim()),
                    root.getProperty(), root.getOperator().trim(), root.getValue().trim());
        } else {
            return loadMultipleCondition(root);
        }
    }

    public Grid loadGrid(PRDWorld prdWorld) {
        PRDWorld.PRDGrid prdGrid = prdWorld.getPRDGrid();
        int columns = prdGrid.getColumns();
        int rows = prdGrid.getRows();

        if (columns > 100 || columns < 10 || rows < 10 || rows > 100) {
            throw new IllegalArgumentException("illegal grid, make sure columns/rows are between 10-100");
        }

        return new Grid(rows, columns);
    }
}
