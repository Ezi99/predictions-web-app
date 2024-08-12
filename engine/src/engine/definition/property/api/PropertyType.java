package engine.definition.property.api;

public enum PropertyType {
    INTEGER {
        public Integer convert(Object value) {
            if (!(value instanceof Integer)) {
                throw new IllegalArgumentException("value " + value + " is not of a DECIMAL type (expected Integer class)");
            }
            return (Integer) value;
        }
    }, BOOLEAN {
        public Boolean convert(Object value) {
            if(!(value instanceof Boolean)){
                throw new IllegalArgumentException("value " + value + " is not of a BOOLEAN type (expected Integer class)");
            }
            return (Boolean) value;
        }
    }, FLOAT {
        public Float convert(Object value) {
            if(!(value instanceof Float)){
                throw new IllegalArgumentException("value " + value + " is not of a FLOAT type (expected Integer class)");
            }
            return (Float) value;
        }
    }, STRING {
        public String convert(Object value) {
            if(!(value instanceof String)){
                throw new IllegalArgumentException("value " + value + " is not of a STRING type (expected Integer class)");
            }
            return (String) value;
        }
    };

    public abstract <T> T convert(Object value);
}