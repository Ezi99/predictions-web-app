package engine.execution.instance.entity.manager;

import engine.definition.entity.EntityDefinition;
import engine.execution.instance.entity.EntityInstance;
import engine.execution.instance.entity.EntityInstanceImpl;
import engine.execution.space.Coordinates;

import java.util.*;

public class EntityInstanceManagerImpl implements EntityInstanceManager {

    private final Random random;
    private List<EntityInstance> instances;
    private final List<EntityInstance> instancesToAdd;
    private final EntityDefinition entityDefinition;
    private int instanceCount;
    private int idCount;

    public EntityInstanceManagerImpl(EntityDefinition entityDefinition) {
        instanceCount = 0;
        idCount = 0;
        instances = new ArrayList<>();
        instancesToAdd = new ArrayList<>();
        this.entityDefinition = entityDefinition;
        random = new Random();
    }

    @Override
    public EntityInstance create(boolean addLater, Coordinates coordinates, Float currentTick) {
        EntityInstance newEntityInstance = new EntityInstanceImpl(entityDefinition, idCount);
        idCount++;

        if (addLater) {
            instancesToAdd.add(newEntityInstance);
        } else {
            instances.add(newEntityInstance);
        }

        if (coordinates != null) {
            newEntityInstance.setCoordinates(coordinates);
        }

        entityDefinition.instantiateProperties(newEntityInstance, currentTick);
        instanceCount++;
        return newEntityInstance;
    }

    @Override
    public void addInstanceLater(EntityInstance entityInstance) {
        entityInstance.setId(idCount);
        idCount++;
        instanceCount++;
        instancesToAdd.add(entityInstance);
    }


    @Override
    public List<EntityInstance> getInstances() {
        return instances;
    }

    @Override
    public void killEntity(int id) {
        for (EntityInstance entityInstance : instances) {
            if (entityInstance.getId() == id && entityInstance.isAlive()) {
                entityInstance.Die();
                instanceCount--;
                break;
            }
        }
    }

    @Override
    public EntityDefinition getEntityDefinition() {
        return entityDefinition;
    }

    @Override
    public int getInstanceCount() {
        return instanceCount;
    }

    @Override
    public Collection<EntityInstance> getRandomInstances(int count) {
        List<EntityInstance> livingInstances = new ArrayList<>();

        for (EntityInstance entityInstance : instances) {
            if (entityInstance.isAlive()) {
                livingInstances.add(entityInstance);
            }
        }

        Map<Integer, EntityInstance> randomInstances = new HashMap<>();

        if (livingInstances.size() != 0) {
            for (int i = 0; i < count; i++) {
                int index = random.nextInt(livingInstances.size());
                EntityInstance entityInstance = livingInstances.get(index);
                if (!randomInstances.containsKey(entityInstance.getId())) {
                    randomInstances.put(entityInstance.getId(), entityInstance);
                }
            }
        }

        return randomInstances.values();
    }

    @Override
    public void updateInstances() {
        List<EntityInstance> aliveInstances = new ArrayList<>();
        for(EntityInstance entityInstance : instances){
            if(entityInstance.isAlive()){
                aliveInstances.add(entityInstance);
            }
        }
        instances = aliveInstances;
        instances.addAll(instancesToAdd);
        instancesToAdd.clear();
    }
}
