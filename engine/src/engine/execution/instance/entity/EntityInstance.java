package engine.execution.instance.entity;

import engine.definition.entity.EntityDefinition;
import engine.execution.instance.property.PropertyInstance;
import engine.execution.space.Coordinates;

import java.util.Collection;

public interface EntityInstance {
    void setCoordinates(Coordinates coordinates);

    Coordinates getCoordinates();

    void setId(int id);

    int getId();

    PropertyInstance getPropertyInstanceByName(String name);

    void addPropertyInstance(PropertyInstance propertyInstance);

    boolean isAlive();

    void Die();

    Collection<PropertyInstance> getPropertiesInstances();

    EntityDefinition getEntityDefinition();

}
