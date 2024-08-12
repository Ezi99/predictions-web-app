package engine.expression;

import engine.definition.property.api.PropertyType;
import engine.execution.context.Context;
import engine.execution.instance.entity.EntityInstance;
import engine.execution.instance.property.PropertyInstance;

public interface Expression {

    Object readExpression(Context context, PropertyInstance propertyInstance, String expression);

    void setPrimaryEntity(EntityInstance primaryEntity);

    Boolean readBooleanExpression(String expression);

    Object getFreeValue(PropertyType instancePropertyType, String expression);

}
