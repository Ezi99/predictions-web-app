package engine.definition.property.impl;

import engine.definition.property.api.AbstractPropertyDefinition;
import engine.definition.property.api.PropertyType;
import engine.definition.value.generator.api.ValueGenerator;

public class FloatPropertyDefinition extends AbstractPropertyDefinition<Float>{
    private boolean hasBounds;
    private Float upperBoundary;
    private Float bottomBoundary;

    public FloatPropertyDefinition(String name, ValueGenerator<Float> valueGenerator) {
        super(name, PropertyType.FLOAT, valueGenerator);
        hasBounds = false;
    }

    public void setBoundary(Float bottomBoundary, Float upperBoundary) {
        hasBounds = true;
        this.upperBoundary = upperBoundary;
        this.bottomBoundary = bottomBoundary;
    }

    public boolean doesHaveBoundaries() {
        return hasBounds;
    }

    public Float getUpperBoundary() {
        return upperBoundary;
    }

    public Float getBottomBoundary() {
        return bottomBoundary;
    }

    public boolean isInRange(Float number) {
        return number >= bottomBoundary && number <= upperBoundary;
    }

    public Float validateValueToUpdate(Float res) {

        if (hasBounds) {
            if (bottomBoundary > res) {
                res = bottomBoundary;
            } else if (upperBoundary < res) {
                res = upperBoundary;
            }
        }

        return res;
    }
}
