package engine.definition.entity;

import engine.definition.property.api.PropertyDefinition;
import engine.execution.instance.entity.EntityInstance;
import engine.execution.instance.property.PropertyInstance;
import engine.execution.instance.property.PropertyInstanceImpl;

import java.util.*;

public class EntityDefinitionImpl implements EntityDefinition {

    private final String name;
    private final Map<String, PropertyDefinition> properties;
    private int population;

    public EntityDefinitionImpl(String name) {
        this.name = name;
        population = 0;
        properties = new HashMap<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPopulation() {
        return population;
    }

    @Override
    public void setPopulation(int population) {
        this.population = population;
    }

    @Override
    public void addPropertyDefinition(PropertyDefinition propertyDefinition) {
        properties.put(propertyDefinition.getName(), propertyDefinition);
    }

    @Override
    public void instantiateProperties(EntityInstance newEntityInstance , Float currentTick) {
        for (PropertyDefinition propertyDefinition : properties.values()) {
            Object value = propertyDefinition.generateValue();
            PropertyInstance newPropertyInstance = new PropertyInstanceImpl(propertyDefinition, value, currentTick);
            newEntityInstance.addPropertyInstance(newPropertyInstance);
        }
    }

    @Override
    public PropertyDefinition getPropertyDefinition(String name) { return properties.get(name); }

    @Override
    public Collection<PropertyDefinition> getPropertiesDefinition() {
        return properties.values();
    }
}
