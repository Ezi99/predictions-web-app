package engine.definition.environment.api;

import engine.definition.property.api.PropertyDefinition;
import engine.execution.instance.environment.api.ActiveEnvironment;

import java.util.Collection;

public interface EnvVariablesManager {
    void addEnvironmentVariable(PropertyDefinition propertyDefinition);
    ActiveEnvironment createActiveEnvironment();
    PropertyDefinition getEnvVariable(String name);
    Collection<PropertyDefinition> getEnvVariables();
}
