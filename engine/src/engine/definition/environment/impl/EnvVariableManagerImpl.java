package engine.definition.environment.impl;

import engine.definition.environment.api.EnvVariablesManager;
import engine.definition.property.api.PropertyDefinition;
import engine.execution.instance.environment.impl.ActiveEnvironmentImpl;
import engine.execution.instance.environment.api.ActiveEnvironment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EnvVariableManagerImpl implements EnvVariablesManager {

    private final Map<String, PropertyDefinition> propNameToPropDefinition;

    public EnvVariableManagerImpl() {
        propNameToPropDefinition = new HashMap<>();
    }

    @Override
    public void addEnvironmentVariable(PropertyDefinition propertyDefinition) {
        propNameToPropDefinition.put(propertyDefinition.getName(), propertyDefinition);
    }

    @Override
    public ActiveEnvironment createActiveEnvironment() {
        return new ActiveEnvironmentImpl();
    }

    @Override
    public PropertyDefinition getEnvVariable(String name){
        if (!propNameToPropDefinition.containsKey(name)) {
            throw new IllegalArgumentException("Can't find env variable with name " + name);
        }
        return propNameToPropDefinition.get(name);
    }

    @Override
    public Collection<PropertyDefinition> getEnvVariables() {
        return propNameToPropDefinition.values();
    }
}
