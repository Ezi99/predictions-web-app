package engine.definition.property.impl;

import engine.definition.property.api.AbstractPropertyDefinition;
import engine.definition.property.api.PropertyType;
import engine.definition.value.generator.api.ValueGenerator;

public class BooleanPropertyDefinition extends AbstractPropertyDefinition<Boolean> {
    public BooleanPropertyDefinition(String name, ValueGenerator<Boolean> valueGenerator) {
        super(name, PropertyType.BOOLEAN, valueGenerator);
    }
}
