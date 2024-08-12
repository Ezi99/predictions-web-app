package dto.definition;

public class PropertyInfoDTO {
    private String name;
    private String type;
    private double upperBoundary;
    private double bottomBoundary;
    private boolean hasRange;
    private boolean isRandomlyInitialized;
    private String value;
    private Float consistency;

    public PropertyInfoDTO() {
        hasRange = false;
    }

    public Float getConsistency() {
        return consistency;
    }

    public void setConsistency(Float consistency) {
        this.consistency = consistency;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value != null ? value.trim() : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getUpperBoundary() {
        return upperBoundary;
    }

    public double getBottomBoundary() {
        return bottomBoundary;
    }

    public boolean isHasRange() {
        return hasRange;
    }

    public boolean isRandomlyInitialized() {
        return isRandomlyInitialized;
    }

    public void setRandomlyInitialized(boolean randomlyInitialized) {
        isRandomlyInitialized = randomlyInitialized;
    }

    public void setBoundary(double bottomBoundary, double upperBoundary) {
        this.bottomBoundary = bottomBoundary;
        this.upperBoundary = upperBoundary;
        hasRange = true;
    }

    @Override
    public String toString() {
        return name;
    }
}
