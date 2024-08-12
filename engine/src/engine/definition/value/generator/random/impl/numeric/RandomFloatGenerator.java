package engine.definition.value.generator.random.impl.numeric;

public class RandomFloatGenerator extends AbstractNumericRandomGenerator<Float>{

    public RandomFloatGenerator() {}

    @Override
    public Float generateValue() {
        if(hasRange){
            return from + random.nextFloat() * (to - from);
        } else {
            return random.nextFloat() * 100;
        }

    }
}
