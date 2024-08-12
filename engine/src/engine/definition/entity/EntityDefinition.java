package engine.definition.entity;

import engine.definition.property.api.PropertyDefinition;
import engine.execution.instance.entity.EntityInstance;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface EntityDefinition {
    String getName();
    int getPopulation();
    void addPropertyDefinition(PropertyDefinition propertyDefinition);
    void instantiateProperties(EntityInstance newEntityInstance, Float currentTick);
    PropertyDefinition getPropertyDefinition(String name);
    Collection<PropertyDefinition> getPropertiesDefinition();

    void setPopulation(int population);

    }
