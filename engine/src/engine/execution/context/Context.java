package engine.execution.context;

import engine.definition.entity.EntityDefinition;
import engine.execution.instance.entity.manager.EntityInstanceManager;
import engine.execution.instance.property.PropertyInstance;
import engine.execution.instance.entity.EntityInstance;
import engine.execution.space.Grid;

public interface Context {
    EntityInstance getPrimaryEntityInstance();
    void setPrimaryEntityInstance(EntityInstance primaryEntityInstance);
    EntityInstance getSecondaryEntityInstance();
    void setSecondaryEntityInstance(EntityInstance primaryEntityInstance);
    void removeEntity(EntityInstance entityInstance);
    PropertyInstance getEnvironmentVariable(String name);

    void setCurrentTick(Integer currentTick);

    Float getCurrentTick();

    void setSecondaryEntityInstanceManager(EntityInstanceManager secondaryEntityInstanceManager);

    void setPrimaryEntityInstanceManager(EntityInstanceManager primaryEntityInstanceManager);

    EntityInstanceManager getPrimaryEntityInstanceManager();

    EntityInstanceManager getSecondaryEntityInstanceManager();

    EntityInstanceManager getEntityInstanceManager(String name);

    Grid getGrid();

    boolean isInstanceMissing();

    void setInstanceMissing(boolean instanceMissing);

    EntityDefinition getSecondaryEntityDefinition();

    void setSecondaryEntityDefinition(EntityDefinition secondaryEntityDefinition);

    boolean checkIfToInvoke(String primaryEntity);

    int getTotalInstancesCount();
}
