package engine.execution.instance.property;

import engine.definition.property.api.PropertyDefinition;
import engine.definition.property.api.PropertyType;

import java.util.ArrayList;
import java.util.List;

public class PropertyInstanceImpl implements PropertyInstance {

    private final PropertyDefinition propertyDefinition;
    private Object value;
    private Float lastValueUpdate;
    private List<Float> ticksUntilUpdate;

    public PropertyInstanceImpl(PropertyDefinition propertyDefinition, Object value, Float currentTick) {
        this.propertyDefinition = propertyDefinition;
        this.value = value;
        lastValueUpdate = currentTick;
        if(propertyDefinition.getType() != PropertyType.FLOAT){
            ticksUntilUpdate = new ArrayList<>();
        }
    }


    public PropertyInstanceImpl(PropertyDefinition propertyDefinition, Object value) {
        this(propertyDefinition, value, 0f);
    }

    @Override
    public PropertyDefinition getPropertyDefinition() {
        return propertyDefinition;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void updateValue(Object val, Float currentTick) {
        if (!this.value.equals(val)) {
            if(propertyDefinition.getType() != PropertyType.FLOAT){
                ticksUntilUpdate.add(currentTick - lastValueUpdate);
            }
            lastValueUpdate = currentTick;
        }
        this.value = val;
    }

    @Override
    public Float getLastValueUpdate() {
        return lastValueUpdate;
    }

    @Override
    public List<Float> getTicksUntilUpdate() {
        return ticksUntilUpdate;
    }

    @Override
    public float getConsistency(){
        float currentSum = 0;

        for (Float tickUntilUpdate : ticksUntilUpdate) {
            currentSum += tickUntilUpdate;
        }

        return  currentSum / ticksUntilUpdate.size();
    }
}
