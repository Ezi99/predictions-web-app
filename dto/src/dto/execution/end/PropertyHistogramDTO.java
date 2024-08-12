package dto.execution.end;

import java.util.Map;

public class PropertyHistogramDTO {
    private  String entityName;
    private  Map<String, Map<Object, Integer>> propertyHistogram;

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public void setPropertyHistogram(Map<String, Map<Object, Integer>> propertyHistogram) {
        this.propertyHistogram = propertyHistogram;
    }

    public String getEntityName() {
        return entityName;
    }

    public Map<String, Map<Object, Integer>> getPropertyHistogram() {
        return propertyHistogram;
    }
}
