package engine.action.impl;

import dto.definition.action.ActionInfoDTO;
import engine.action.api.AbstractAction;
import engine.action.api.ActionType;
import engine.definition.entity.EntityDefinition;
import engine.execution.context.Context;

public class KillAction extends AbstractAction {

    public KillAction(EntityDefinition entityDefinition) {
        super(ActionType.KILL, entityDefinition);
    }

    @Override
    public void invoke(Context context) {
        setPrimaryInstance(context, null);
        if(getPrimaryEntityInstance() == null){
            return;
        }
        context.removeEntity(getPrimaryEntityInstance());
    }

    @Override
    public ActionInfoDTO getActionInfo() {
        ActionInfoDTO actionInfoDTO = new ActionInfoDTO();

        if(getSecondaryEntityContext() != null){
            actionInfoDTO.setSecondaryEntity(getSecondaryEntityContext().getName());
        }

        actionInfoDTO.setType(ActionType.KILL.name());
        actionInfoDTO.setMainEntity(getPrimaryEntityContext().getName());

        return actionInfoDTO;
    }

    @Override
    public String toString() {
        return "Kill Action (killing an instance of a certain entity)";
    }
}
