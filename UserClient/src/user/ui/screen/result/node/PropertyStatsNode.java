package user.ui.screen.result.node;

import java.util.Map;

public class PropertyStatsNode {
    private String entityName;
    private String propertyName;
    private String type;
    private Map<Object, Integer> propertyHistogram;
    private Float consistency;

    public Float getConsistency() {
        return consistency;
    }

    public void setConsistency(Float consistency) {
        this.consistency = consistency;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Map<Object, Integer> getPropertyHistogram() {
        return propertyHistogram;
    }

    public void setPropertyHistogram(Map<Object, Integer> propertyHistogram) {
        this.propertyHistogram = propertyHistogram;
    }

    @Override
    public String toString() {
        if (entityName != null) {
            return entityName;
        } else if(propertyName != null) {
            return propertyName;
        } else {
            return "Explore Properties";
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
