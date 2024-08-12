package engine.action.impl.condition;

import engine.action.api.ConditionOperator;
import engine.definition.entity.EntityDefinition;
import engine.execution.context.Context;

import java.util.ArrayList;
import java.util.List;

public class MultipleCondition extends Condition {
    private final ConditionOperator multipleConditionOperator;
    private final List<Condition> conditions;


    public MultipleCondition(String multipleConditionOperator) {
        conditions = new ArrayList<>();
        if (multipleConditionOperator.equals("or")) {
            this.multipleConditionOperator = ConditionOperator.OR;
        } else if (multipleConditionOperator.equals("and")) {
            this.multipleConditionOperator = ConditionOperator.AND;
        } else {
            throw new IllegalArgumentException("invalid logical operator in multiple condition");
        }
    }

    public void addCondition(Condition condition) {
        conditions.add(condition);
    }

    @Override
    public boolean isConditionTrue(Context context) {
        boolean res = false;

        if (multipleConditionOperator == ConditionOperator.OR) {
            for (Condition condition : conditions) {
                if(condition instanceof MultipleCondition){
                    if (condition.isConditionTrue(context)) {
                        res = true;
                        break;
                    }
                } else {
                    if (context.checkIfToInvoke(condition.getPrimaryEntity().getName())) {
                        if (condition.isConditionTrue(context)) {
                            res = true;
                            break;
                        }
                    }
                }
                context.setInstanceMissing(false);
            }
        } else {
            int count = 0;
            for (Condition condition : conditions) {
                if(condition instanceof MultipleCondition){
                    if (condition.isConditionTrue(context)) {
                        count++;
                    }
                } else {
                    if (context.checkIfToInvoke(condition.getPrimaryEntity().getName())) {
                        if (condition.isConditionTrue(context)) {
                            count++;
                        }
                    }
                }
                context.setInstanceMissing(false);
            }
            if (count == conditions.size()) {
                res = true;
            }
        }

        return res;
    }

    @Override
    public int getConditionCount() {
        return conditions.size();
    }

    @Override
    public ConditionOperator getConditionOperator() {
        return multipleConditionOperator;
    }

    @Override
    public void validate(EntityDefinition primaryEntity, EntityDefinition secondaryEntity) {
        for (Condition condition : conditions) {
            condition.validate(primaryEntity, secondaryEntity);
        }
    }


    @Override
    public String toString() {
        return "Condition(" + conditions + ")";
    }

}
