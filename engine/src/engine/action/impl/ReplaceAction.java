package engine.action.impl;

import dto.definition.action.ActionInfoDTO;
import engine.action.api.AbstractAction;
import engine.action.api.ActionType;
import engine.action.api.ReplaceMode;
import engine.definition.entity.EntityDefinition;
import engine.definition.property.api.PropertyDefinition;
import engine.definition.property.api.PropertyType;
import engine.execution.context.Context;
import engine.execution.instance.entity.EntityInstance;
import engine.execution.instance.entity.EntityInstanceImpl;
import engine.execution.instance.entity.manager.EntityInstanceManager;
import engine.execution.instance.property.PropertyInstance;
import engine.execution.instance.property.PropertyInstanceImpl;
import engine.execution.space.Coordinates;

public class ReplaceAction extends AbstractAction {

    private final String entityToCreateName;
    private final ReplaceMode mode;
    private EntityInstance entityInstanceToKill;
    private Coordinates toKillCoordinates;

    public ReplaceAction(EntityDefinition entityToKillDefinition, String entityToCreateName, ReplaceMode mode) {
        super(ActionType.REPLACE, entityToKillDefinition);
        this.entityToCreateName = entityToCreateName;
        this.mode = mode;
    }

    @Override
    public void invoke(Context context) {
        setPrimaryInstance(context, null);
        if (getPrimaryEntityInstance() == null) {
            return;
        }

        EntityInstanceManager creatorManager = context.getEntityInstanceManager(entityToCreateName);
        killEntityInstance(context);

        if (context.getGrid().getSize() <= context.getTotalInstancesCount()) {
            throw new IllegalArgumentException("this replace action will result more instances than grid size");
        }

        if (mode == ReplaceMode.SCRATCH && entityInstanceToKill != null) {
            creatorManager.create(true, toKillCoordinates, context.getCurrentTick());
        } else {
            makeDerivedEntity(creatorManager, context);
        }
    }

    private void makeDerivedEntity(EntityInstanceManager creatorManager, Context context) {
        EntityDefinition entityToCreateDefinition = creatorManager.getEntityDefinition();
        EntityInstance entityInstance = new EntityInstanceImpl(entityToCreateDefinition);

        for (PropertyDefinition propertyDefinition : entityToCreateDefinition.getPropertiesDefinition()) {
            Object value;
            if (checkIfSameProperty(propertyDefinition)) {
                value = entityInstanceToKill.getPropertyInstanceByName(propertyDefinition.getName()).getValue();
            } else {
                value = propertyDefinition.generateValue();
            }
            PropertyInstance newPropertyInstance = new PropertyInstanceImpl(propertyDefinition, value, context.getCurrentTick());
            entityInstance.addPropertyInstance(newPropertyInstance);
        }

        entityInstance.setCoordinates(toKillCoordinates);
        creatorManager.addInstanceLater(entityInstance);
    }

    private boolean checkIfSameProperty(PropertyDefinition instanceToCreateProperty) {
        String propertyName = instanceToCreateProperty.getName();
        PropertyType propertyType = instanceToCreateProperty.getType();
        PropertyDefinition propertyDefinition = entityInstanceToKill.getEntityDefinition().getPropertyDefinition(propertyName);

        return propertyDefinition != null && propertyDefinition.getType() == propertyType;
    }

    private void killEntityInstance(Context context) {
        EntityInstanceManager primaryManager = context.getPrimaryEntityInstanceManager();
        EntityInstanceManager secondaryManager = context.getSecondaryEntityInstanceManager();
        EntityInstanceManager instanceManager;

        if (primaryManager.getEntityDefinition().getName().equals(getPrimaryEntityContext().getName())) {
            instanceManager = primaryManager;
            entityInstanceToKill = context.getPrimaryEntityInstance();
        } else if (secondaryManager != null && secondaryManager.getEntityDefinition().getName().equals(getPrimaryEntityContext().getName())) {
            instanceManager = secondaryManager;
            entityInstanceToKill = context.getSecondaryEntityInstance();
        }
        else return;

        toKillCoordinates = entityInstanceToKill.getCoordinates();
        if (!entityInstanceToKill.isAlive() && toKillCoordinates.getEntityInstance() != null) {
            throw new IllegalArgumentException("trying to create instance in a taken coordinate in replace action");
        }
        instanceManager.killEntity(entityInstanceToKill.getId());
    }

    @Override
    public ActionInfoDTO getActionInfo() {
        ActionInfoDTO actionInfoDTO = new ActionInfoDTO();

        actionInfoDTO.setCreate(entityToCreateName);
        actionInfoDTO.setMode(mode.name().toLowerCase());
        if (getSecondaryEntityContext() != null) {
            actionInfoDTO.setSecondaryEntity(getSecondaryEntityContext().getName());
        }

        actionInfoDTO.setMainEntity(getPrimaryEntityContext().getName());
        actionInfoDTO.setType(ActionType.REPLACE.name());

        return actionInfoDTO;
    }
}
