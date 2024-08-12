package engine.execution.instance.environment.api;

import engine.execution.instance.property.PropertyInstance;

public interface ActiveEnvironment {
    PropertyInstance getProperty(String name);
    void addPropertyInstance(PropertyInstance propertyInstance);
}
