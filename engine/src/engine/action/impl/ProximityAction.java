package engine.action.impl;

import dto.definition.action.ActionInfoDTO;
import engine.action.api.AbstractAction;
import engine.action.api.Action;
import engine.action.api.ActionType;
import engine.definition.entity.EntityDefinition;
import engine.execution.context.Context;
import engine.execution.instance.entity.EntityInstance;
import engine.execution.space.Coordinates;
import engine.expression.Expression;
import engine.expression.ExpressionImpl;

import java.util.ArrayList;
import java.util.List;

public class ProximityAction extends AbstractAction {

    private final EntityDefinition targetEntity;
    private final String depthExpression;
    private final List<Action> actions;
    private final Expression expressionReader;
    private Integer sourceEntityID;

    public ProximityAction(EntityDefinition primaryEntity, EntityDefinition targetEntity, String depthExpression) {
        super(ActionType.PROXIMITY, primaryEntity);
        this.targetEntity = targetEntity;
        this.depthExpression = depthExpression;
        expressionReader = new ExpressionImpl(ActionType.PROXIMITY);
        actions = new ArrayList<>();
    }

    @Override
    public void invoke(Context context) {
        setPrimaryInstance(context, expressionReader);
        if (getPrimaryEntityInstance() == null) {
            return;
        }
        context.setSecondaryEntityDefinition(targetEntity);
        Object res = expressionReader.readExpression(context, null, depthExpression);

        double depth;
        if ((res instanceof Float)) {
            depth = Math.floor((Float) res);
        } else {
            try {
                depth = Math.floor(Float.parseFloat(depthExpression));
            } catch (Exception e) {
                throw new IllegalArgumentException(depthExpression + " is not valid for depth");
            }
        }

        sourceEntityID = context.getPrimaryEntityInstance().getId();
        EntityInstance targetInstance = lookForTarget(context.getPrimaryEntityInstance().getCoordinates(), depth);
        if (targetInstance != null) {
            context.setSecondaryEntityInstance(targetInstance);
            for (Action action : actions) {
                action.invoke(context);
            }
        }
    }

    private EntityInstance lookForTarget(Coordinates root, double depth) {
        EntityInstance entityInstance = null;

        if (depth >= 0 && checkIfTargetFound(root.getEntityInstance())) {
            entityInstance = root.getEntityInstance();
        } else if (depth > 0) {
            for (Coordinates currentRoot : root.getNearCoordinates()) {
                entityInstance = lookForTarget(currentRoot, depth - 1);
                if (checkIfTargetFound(entityInstance)) {
                    break;
                }
            }
        }

        return entityInstance;
    }

    private boolean checkIfTargetFound(EntityInstance entityInstance) {
        return entityInstance != null && entityInstance.getEntityDefinition().getName()
                .equals(targetEntity.getName()) && entityInstance.getId() != sourceEntityID;
    }

    @Override
    public ActionInfoDTO getActionInfo() {
        ActionInfoDTO actionInfoDTO = new ActionInfoDTO();
        actionInfoDTO.setDepth(depthExpression);
        actionInfoDTO.setActionsCount(actions.size());
        actionInfoDTO.setTargetEntity(targetEntity.getName());
        if(getSecondaryEntityContext() != null){
            actionInfoDTO.setSecondaryEntity(getSecondaryEntityContext().getName());
        }
        actionInfoDTO.setType(ActionType.PROXIMITY.name());
        actionInfoDTO.setMainEntity(getPrimaryEntityContext().getName());
        return actionInfoDTO;
    }

    public void addAction(Action action) {
        actions.add(action);
    }
}
