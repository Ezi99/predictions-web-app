package engine.execution.instance.entity.manager;

import engine.definition.entity.EntityDefinition;
import engine.execution.instance.entity.EntityInstance;
import engine.execution.space.Coordinates;

import java.util.Collection;
import java.util.List;

public interface EntityInstanceManager {


    EntityInstance create(boolean addLater, Coordinates coordinates, Float currentTick);

    void addInstanceLater(EntityInstance entityInstance);

    List<EntityInstance> getInstances();
    void killEntity(int id);
    int getInstanceCount();
    EntityDefinition getEntityDefinition();

    Collection<EntityInstance> getRandomInstances(int count);

    void updateInstances();
}
