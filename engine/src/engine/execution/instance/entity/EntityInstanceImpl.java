package engine.execution.instance.entity;

import engine.execution.instance.property.PropertyInstance;
import engine.definition.entity.EntityDefinition;
import engine.execution.space.Coordinates;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EntityInstanceImpl implements EntityInstance {

    private final EntityDefinition entityDefinition;
    private int id;
    private Coordinates coordinates;
    private final Map<String, PropertyInstance> properties;
    private boolean alive;

    public EntityInstanceImpl(EntityDefinition entityDefinition) {
        this(entityDefinition, -1);
    }

    public EntityInstanceImpl(EntityDefinition entityDefinition, int id) {
        this.entityDefinition = entityDefinition;
        this.id = id;
        alive = true;
        properties = new HashMap<>();
    }

    @Override
    public void setCoordinates(Coordinates coordinates){
        coordinates.setEntityInstance(this);
        this.coordinates = coordinates;
    }

    @Override
    public Coordinates getCoordinates() {
        return coordinates;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public PropertyInstance getPropertyInstanceByName(String name) {
        if (!properties.containsKey(name)) {
            throw new IllegalArgumentException("for entity of type " + entityDefinition.getName() + " has no property named " + name);
        }

        return properties.get(name);
    }

    @Override
    public void addPropertyInstance(PropertyInstance propertyInstance) {
        properties.put(propertyInstance.getPropertyDefinition().getName(), propertyInstance);
    }

    @Override
    public boolean isAlive() { return alive; }

    @Override
    public void Die() {
        coordinates.setEntityInstance(null);
        alive = false;
    }

    public Collection<PropertyInstance> getPropertiesInstances(){
        return properties.values();
    }

    @Override
    public EntityDefinition getEntityDefinition() {
        return entityDefinition;
    }

}
