package engine.definition.value.generator.random.impl.numeric;

import engine.definition.value.generator.random.api.AbstractRandomValueGenerator;

public abstract class AbstractNumericRandomGenerator<T> extends AbstractRandomValueGenerator<T> {

    protected T from;
    protected T to;
    protected boolean hasRange;

    public AbstractNumericRandomGenerator(){
        hasRange = false;
    }

    public AbstractNumericRandomGenerator(T from, T to) {
        hasRange = true;
        this.from = from;
        this.to = to;
    }

    public void setBoundary(T from, T to){
        hasRange = true;
        this.from = from;
        this.to = to;
    }

}
