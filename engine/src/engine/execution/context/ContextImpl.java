package engine.execution.context;

import engine.definition.entity.EntityDefinition;
import engine.execution.instance.entity.EntityInstance;
import engine.execution.instance.entity.manager.EntityInstanceManager;
import engine.execution.instance.property.PropertyInstance;
import engine.execution.instance.environment.api.ActiveEnvironment;
import engine.execution.space.Grid;
import java.util.Map;

public class ContextImpl implements Context {

    private final ActiveEnvironment activeEnvironment;
    private final Grid grid;
    private EntityInstance primaryEntityInstance;
    private EntityInstance secondaryEntityInstance;
    private EntityDefinition secondaryEntityDefinition;
    private EntityInstanceManager primaryEntityInstanceManager;
    private EntityInstanceManager secondaryEntityInstanceManager;
    private final Map<String, EntityInstanceManager> entityInstanceManageMap;
    private Float currentTick;
    private boolean instanceMissing;


    public ContextImpl(ActiveEnvironment activeEnvironment, Grid grid, Map<String, EntityInstanceManager> entityInstanceManageMap) {
        this.activeEnvironment = activeEnvironment;
        this.grid = grid;
        instanceMissing = false;
        this.entityInstanceManageMap = entityInstanceManageMap;
    }

    @Override
    public EntityInstance getPrimaryEntityInstance() {
        return primaryEntityInstance;
    }

    @Override
    public void setPrimaryEntityInstance(EntityInstance primaryEntityInstance) {
        this.primaryEntityInstance = primaryEntityInstance;
    }

    @Override
    public void removeEntity(EntityInstance entityInstance) {
        primaryEntityInstanceManager.killEntity(entityInstance.getId());
    }

    @Override
    public EntityInstance getSecondaryEntityInstance() {
        return secondaryEntityInstance;
    }

    @Override
    public void setSecondaryEntityInstance(EntityInstance secondaryEntityInstance) {
        this.secondaryEntityInstance = secondaryEntityInstance;
    }

    @Override
    public PropertyInstance getEnvironmentVariable(String name) {
        return activeEnvironment.getProperty(name);
    }

    @Override
    public Float getCurrentTick() {
        return currentTick;
    }

    @Override
    public void setCurrentTick(Integer currentTick) {
        this.currentTick = Float.valueOf(currentTick);
    }

    @Override
    public EntityInstanceManager getPrimaryEntityInstanceManager() {
        return primaryEntityInstanceManager;
    }

    @Override
    public void setPrimaryEntityInstanceManager(EntityInstanceManager primaryEntityInstanceManager) {
        this.primaryEntityInstanceManager = primaryEntityInstanceManager;
    }

    @Override
    public EntityInstanceManager getSecondaryEntityInstanceManager() {
        return secondaryEntityInstanceManager;
    }

    @Override
    public void setSecondaryEntityInstanceManager(EntityInstanceManager secondaryEntityInstanceManager) {
        this.secondaryEntityInstanceManager = secondaryEntityInstanceManager;
    }

    @Override
    public EntityInstanceManager getEntityInstanceManager(String name) {
        return entityInstanceManageMap.get(name);
    }

    @Override
    public Grid getGrid() {
        return grid;
    }

    @Override
    public boolean isInstanceMissing() {
        return instanceMissing;
    }

    @Override
    public void setInstanceMissing(boolean instanceMissing) {
        this.instanceMissing = instanceMissing;
    }

    @Override
    public EntityDefinition getSecondaryEntityDefinition() {
        return secondaryEntityDefinition;
    }

    @Override
    public void setSecondaryEntityDefinition(EntityDefinition secondaryEntityDefinition) {
        this.secondaryEntityDefinition = secondaryEntityDefinition;
    }

    @Override
    public boolean checkIfToInvoke(String primaryEntity) {
        boolean condition1 = primaryEntityInstance.getEntityDefinition().getName()
                .equals(primaryEntity);
        boolean condition2 = secondaryEntityInstance != null &&
                secondaryEntityInstance.getEntityDefinition().getName().equals(primaryEntity);

        return condition1 || condition2;
    }

    @Override
    public int getTotalInstancesCount() {
        int res = 0;

        for (EntityInstanceManager entityInstanceManager : entityInstanceManageMap.values()) {
            res = res + entityInstanceManager.getInstanceCount();
        }

        return res;
    }
}
