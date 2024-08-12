package engine.execution.instance.property;

import engine.definition.property.api.PropertyDefinition;

import java.util.List;

public interface PropertyInstance {
    PropertyDefinition getPropertyDefinition();

    Object getValue();

    void updateValue(Object val, Float currentTick);

    Float getLastValueUpdate();

    List<Float> getTicksUntilUpdate();

    float getConsistency();
}
